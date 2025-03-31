package com.excel.shift.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.util.StringUtils;
import com.excel.shift.config.*;
import com.excel.shift.result.ExtractionResult;
import com.excel.shift.result.SheetExtractionResult;
import com.excel.shift.util.ExcelCoordConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.BooleanUtil;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class ExcelExtractor {
    private final String excelPath;
    private ExcelMappingConfig mappingConfig;
    private final List<Class<?>> classList;
    private final int totalSheetCount;
    private Map<Integer, Map<Integer, String>> currentSheetData;
    private final ExcelMappingConfig originalMappingConfig;

    public ExcelExtractor(String excelPath, String configPath, List<Class<?>> classList) {
        this.excelPath = excelPath;
        this.classList = classList;
        this.totalSheetCount = this.getSheetNames().size();
        this.originalMappingConfig = new ExcelMappingConfig(configPath);
        this.mappingConfig = this.originalMappingConfig.deepClone();
    }
    public ExcelExtractor(String excelPath, ExcelMappingConfig mappingConfig, List<Class<?>> classList) {
        this.excelPath = excelPath;
        this.classList = classList;
        this.totalSheetCount = this.getSheetNames().size();
        this.originalMappingConfig = mappingConfig;
        this.mappingConfig = this.originalMappingConfig.deepClone();
    }

    private boolean loadSheetData(int sheetIndex) {
        try {
            ExcelDataListener excelDataListener = new ExcelDataListener(mappingConfig);
            // 根据文件名判断Excel类型
            boolean isXls = excelPath.toLowerCase().endsWith(".xls");
            boolean isXlsx = excelPath.toLowerCase().endsWith(".xlsx");
            if (!isXls && !isXlsx) {
                throw new IllegalArgumentException("文件类型不支持，仅支持 .xls 和 .xlsx 文件");
            }

            // 读取数据
            EasyExcel.read(excelPath)
                    .excelType(isXls ? com.alibaba.excel.support.ExcelTypeEnum.XLS : com.alibaba.excel.support.ExcelTypeEnum.XLSX)
                    .sheet(sheetIndex)
                    .headRowNumber(0)
                    .registerReadListener(excelDataListener)
                    .doRead();
            this.currentSheetData = excelDataListener.getData();
            if (currentSheetData.isEmpty()) {
                return false;
            }
        } catch (Exception e) {
            log.error("读取sheet[{}]数据失败: {}", sheetIndex, e.getMessage());
            this.currentSheetData = new HashMap<>();
            return false;
        }
        return true;
    }

    /**
     * 获取Excel文件中的所有Sheet名称列表
     *
     * @return Sheet名称列表
     */
    public List<String> getSheetNames() {
        try {
            return EasyExcel.read(excelPath)
                    .build()
                    .excelExecutor()
                    .sheetList()
                    .stream()
                    .map(ReadSheet::getSheetName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 提取指定sheet的数据到指定类型对象
     *
     * @param sheetIndex  sheet索引
     * @param targetClass 目标类型
     * @return 提取的对象或对象列表
     */
    @SuppressWarnings("unchecked")
    public <T> Object extractData(int sheetIndex, Class<T> targetClass) {
        try {
            // 根据类型查找对应的提取器配置
            ExtractorConfig extractor = findExtractorForClass(targetClass.getName());
            if (extractor == null) {
                log.warn("未找到类 {} 的提取器配置", targetClass.getName());
                return null;
            }

            // 使用统一的数据提取方法，直接传递结果类型枚举
            return extractObject(extractor, targetClass, extractor.getResultType());
        } catch (Exception e) {
            log.error("数据提取失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据提取失败", e);
        }
    }

    /**
     * 统一的数据提取方法，基于结果类型选择对应的处理逻辑
     *
     * @param extractor   提取器配置
     * @param targetClass 目标类型
     * @param resultType  结果类型枚举
     * @return 提取的对象或对象列表
     */
    private <T> Object extractObject(ExtractorConfig extractor, Class<T> targetClass,
                                    ExtractorConfig.ResultType resultType) throws Exception {
        switch (resultType) {
            case SINGLE:
                // 单个对象类型
                T instance = targetClass.getDeclaredConstructor().newInstance();
                // 提取基本字段
                extractBasicFields(instance, extractor);
                return instance;
            case LIST:
                // 普通列表类型
                return extractObjectList(extractor, targetClass);
            case GROUP_LIST:
                // 行组列表类型
                return extractGroupObjectList(extractor, targetClass);
            case VERTICAL_LIST:
                // 垂直列表类型 - 每行代表一个字段，每列代表一个对象
                return extractVerticalObjectList(extractor, targetClass);
            default:
                log.error("不支持的结果类型: {}", resultType);
                return null;
        }
    }

    /**
     * 提取对象列表
     */
    private <T> List<T> extractObjectList(ExtractorConfig extractor, Class<T> elementType) throws Exception {
        List<T> resultList = new ArrayList<>();
        // 确定表格范围
        int startRow = resolveRowIndex(extractor.getStartRow());
        String startColumn = extractor.getStartColumn();
        int startCol = startColumn != null ? ExcelCoordConverter.columnNameToIndex(startColumn) : 0;
        int endRow = extractor.getEndRow() != null ? resolveRowIndex(extractor.getEndRow()) : findLastDataRow(startRow, startCol);

        // 提取表格数据，表格现在是单个对象而非数组
        if (extractor.getTable() != null) {
            TableConfig tableConfig = extractor.getTable(); // 直接获取表格配置
            // 普通行数据提取
            for (int row = startRow; row <= endRow; row++) {
                if (!currentSheetData.containsKey(row)) continue;
                T rowInstance = elementType.getDeclaredConstructor().newInstance();
                // 取表格行数据   里面有可能有合并单元格
                boolean hasTableData = extractTableRowToInstance(tableConfig, rowInstance, row, startCol, startRow);
                // 如果行有效，添加到结果集
                if (hasTableData) {
                    resultList.add(rowInstance);
                }
            }
        }
        return resultList;
    }

    /**
     * 提取表格行数据到指定对象实例
     * 返回是否提取到有效数据
     */
    private <T> boolean extractTableRowToInstance(TableConfig tableConfig, T instance, int row, int startCol, int startRow) {
        int success=0;
        int cntMerge=0;
        try {
            Map<Integer, String> rowData = currentSheetData.get(row);
            if (rowData == null || rowData.isEmpty()) {
                return false;
            }
            for (Map.Entry<String, ColumnConfig> entry : tableConfig.getColumns().entrySet()) {
                ColumnConfig column = entry.getValue();
                try {
                    // 获取字段并设置可访问
                    Field field = instance.getClass().getDeclaredField(column.getJavaFieldName());
                    field.setAccessible(true);
                    // 计算列索引
                    int col = ExcelCoordConverter.columnNameToIndex(column.getColumnCell());
                    // 获取单元格值
                    String cellValue = rowData.get(col);
                    if (StringUtils.isEmpty(cellValue) && BooleanUtil.isTrue(column.getIsMergeType())) {
                        // 如果是合并单元格类型并且为null，则尝试从向上查找获取值
                        for (int i = row - 1; i >= startRow; i--) {
                            Map<Integer, String> rData = currentSheetData.get(i);
                            if (rData != null && rData.containsKey(col)) {
                                String cValue = rData.get(col);
                                if (StrUtil.isNotEmpty(cValue)) {
                                    cellValue = cValue;
                                    cntMerge++;
                                    break;
                                }
                            }
                        }
                    }
                    if (StrUtil.isNotEmpty(cellValue)) {
                        if (StrUtil.isNotEmpty(column.getExtractPattern())) {
                            String extractedValue = extractValueByPattern(cellValue.trim(), column.getExtractPattern());
                            if (extractedValue != null) {
                                cellValue = extractedValue;
                            }
                        }
                        // 转换值并设置到对象字段
                        Object convertedValue = convertValue(cellValue.trim(), column.getJavaFieldType(), null);
                        if (convertedValue != null) {
                            field.set(instance, convertedValue);
                            success++;
                        }
                    }
                } catch (Exception e) {
                    log.error("提取字段[{}]失败: {}", column.getJavaFieldName(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("提取行数据失败: {}", e.getMessage(), e);
        }
        return cntMerge<success;
    }

    /**
     * 提取基本字段到对象
     */
    private <T> void extractBasicFields(T targetObject, ExtractorConfig extractor) {
        Map<String, FieldConfig> fields = extractor.getFields();
        if (fields == null || fields.isEmpty()) {
            return;
        }

        for (Map.Entry<String, FieldConfig> entry : fields.entrySet()) {
            try {
                FieldConfig field = entry.getValue();
                // 获取字段并设置可访问
                Field javaField = targetObject.getClass().getDeclaredField(field.getJavaFieldName());
                javaField.setAccessible(true);

                // 获取单元格坐标 - 使用工具类直接转换
                com.excel.shift.util.ExcelCoordConverter.CellPosition cellPos =
                        com.excel.shift.util.ExcelCoordConverter.excelCoordToPosition(field.getExcelCell());

                // 获取单元格值
                String cellValue = getCellValue(cellPos.row, cellPos.column);

                // 应用提取模式
                String extractedValue = cellValue;
                if (cellValue != null && field.getExtractPattern() != null) {
                    extractedValue = extractValueByPattern(cellValue, field.getExtractPattern());
                    // 如果提取失败，使用原始值
                    if (extractedValue == null) {
                        extractedValue = cellValue;
                    }
                }

                // 转换值
                Object convertedValue = null;
                if (extractedValue != null && !extractedValue.trim().isEmpty()) {
                    convertedValue = convertValue(extractedValue.trim(), field.getJavaFieldType(), field.getExtractPattern());
                }

                // 如果值为空且有默认值，使用默认值
                if (convertedValue == null && field.getDefaultValue() != null) {
                    convertedValue = convertValue(field.getDefaultValue(), field.getJavaFieldType(), field.getExtractPattern());
                }

                // 设置值
                if (convertedValue != null) {
                    javaField.set(targetObject, convertedValue);
                }
            } catch (Exception e) {
                log.error("提取字段[{}]失败: {}", entry.getKey(), e.getMessage(), e);
            }
        }
    }

    /**
     * 使用正则表达式提取值
     */
    private String extractValueByPattern(String input, String pattern) {
        if (input == null || pattern == null) {
            return null;
        }
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(input);
            if (m.find()) {
                if (m.groupCount() > 0) {
                    return m.group(1);
                } else {
                    return m.group();
                }
            }
        } catch (Exception e) {
            log.error("使用正则表达式[{}]提取值[{}]失败: {}", pattern, input, e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(int row, int col) {
        // 先检查当前sheet数据
        Map<Integer, String> rowData = currentSheetData.get(row);
        if (rowData != null) {
            return rowData.get(col);
        }
        return null;
    }

    /**
     * 查找最后一行数据
     */
    private int findLastDataRow(int startRow, int startCol) {
        int lastRow = startRow;
        // 从开始行向下搜索
        for (int row = startRow; row < Integer.MAX_VALUE; row++) {
            if (!currentSheetData.containsKey(row)) {
                if (row > startRow + 3) { // 连续3行没数据认为结束
                    break;
                }
                continue;
            }

            Map<Integer, String> rowData = currentSheetData.get(row);
            boolean hasData = false;

            // 检查行中是否有数据
            for (Map.Entry<Integer, String> cell : rowData.entrySet()) {
                if (cell.getKey() >= startCol && cell.getValue() != null && !cell.getValue().trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }

            if (hasData) {
                lastRow = row;
            } else if (row > lastRow + 3) { // 连续3行没数据认为结束
                break;
            }
        }
        return lastRow;
    }

    /**
     * 解析行索引
     * Excel中行从1开始，但程序中从0开始，所以需要减1
     * 配置文件中标注的是实际Excel行号，不需要考虑表头
     */
    private int resolveRowIndex(String rowStr) {
        // 尝试直接解析数字
        int rowNum = Integer.parseInt(rowStr);
        // Excel行从1开始，程序中行索引从0开始，减1进行转换
        return rowNum - 1;
    }

    /**
     * 转换值为指定类型
     *
     * @param value   要转换的字符串值
     * @param type    目标Java类型
     * @param pattern 格式模式（如日期格式）
     * @return 转换后的值
     */
    private Object convertValue(String value, String type, String pattern) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String cleanValue = value.trim();
        try {
            switch (type.toLowerCase()) {
                case "string":
                    return cleanValue;
                case "int":
                case "integer":
                    // 处理可能包含非数字字符的情况
                    String intStr = cleanValue.replaceAll("[^\\d-]", "");
                    if (intStr.isEmpty()) return null;
                    return Integer.parseInt(intStr);
                case "long":
                    String longStr = cleanValue.replaceAll("[^\\d-]", "");
                    if (longStr.isEmpty()) return null;
                    return Long.parseLong(longStr);
                case "float":
                    // 处理中文逗号和其他可能影响解析的字符
                    String floatStr = cleanValue.replace(",", "").replace("，", "");
                    if (floatStr.isEmpty() || floatStr.equals("-")) return null;
                    return Float.parseFloat(floatStr);
                case "double":
                    String doubleStr = cleanValue.replace(",", "").replace("，", "");
                    if (doubleStr.isEmpty() || doubleStr.equals("-")) return null;
                    return Double.parseDouble(doubleStr);
                case "boolean":
                    return Boolean.parseBoolean(cleanValue) ||
                            "是".equals(cleanValue) ||
                            "yes".equalsIgnoreCase(cleanValue) ||
                            "true".equalsIgnoreCase(cleanValue) ||
                            "1".equals(cleanValue);
                case "date":
                    // 日期格式处理
//                    return parseDate(cleanValue, pattern);
                case "enum":
                    // 枚举处理（需要额外的类型信息）
                    // 这里可以扩展为从模式中提取枚举类名
                    return cleanValue;
                default:
                    // 默认作为字符串处理
                    return cleanValue;
            }
        } catch (Exception e) {
//            log.error("转换值 [{}] 到类型 [{}] 失败: {}", cleanValue, type, e.getMessage());
            return null;
        }
    }

    /**
     * 解析日期字符串
     */
    private Date parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // 如果没有指定模式，尝试常见的日期格式
        if (pattern == null || pattern.isEmpty()) {
            // 尝试多种常见日期格式
            String[] commonPatterns = {
                    "yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd",
                    "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss",
                    "MM/dd/yyyy", "dd/MM/yyyy"
            };

            for (String fmt : commonPatterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(fmt);
                    return sdf.parse(dateStr.trim());
                } catch (Exception e) {
                    // 忽略解析错误，尝试下一个格式
                }
            }

            // 所有格式都尝试失败，记录错误
            log.warn("无法解析日期 [{}]，尝试的所有格式均失败", dateStr);
            return null;
        }

        // 使用指定的格式解析
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr.trim());
        } catch (ParseException e) {
            log.error("使用格式 [{}] 解析日期 [{}] 失败: {}", pattern, dateStr, e.getMessage());
            return null;
        }
    }

    /**
     * 提取指定sheet中的所有配置类型数据
     *
     * @param sheetIndex sheet索引
     * @return 包含所有提取结果的sheet结果对象
     */
    private SheetExtractionResult extractAllFromSheet(int sheetIndex) {
        // 获取sheet名称
        List<String> sheetNames = getSheetNames();
        String sheetName = sheetIndex < sheetNames.size() ? sheetNames.get(sheetIndex) : "Sheet" + (sheetIndex + 1);

        // 创建结果对象
        SheetExtractionResult result = new SheetExtractionResult(sheetIndex, sheetName);
        // 加载sheet数据
        if (BooleanUtil.isFalse(loadSheetData(sheetIndex))) {
            log.error("加载解析Sheet[{}]数据失败", sheetIndex);
            result.setSuccess(false);
            return result;
        }

        // 对每个配置的类型进行提取
        for (Class<?> clazz : classList) {
            try {
                // 查找此类型的提取器配置
                ExtractorConfig config = findExtractorForClass(clazz.getName());
                if (config == null) {
                    log.warn("未找到类 {} 的提取器配置", clazz.getName());
                    continue;
                }

                // 提取数据
                Object extractedData = extractData(sheetIndex, clazz);
                if (extractedData != null) {
                    result.addResult(clazz, extractedData);
                    log.info("成功提取类型 {} 的数据", clazz.getName());
                }
            } catch (Exception e) {
                log.error("提取类型 {} 的数据时发生错误: {}", clazz.getName(), e.getMessage(), e);
            }
        }
        log.info("提取Sheet[{}]数据完毕", sheetIndex);
        return result;
    }

    /**
     * 从Excel中提取所有配置的类型数据
     *
     * @return 包含所有sheet所有类型提取结果的对象
     */
    public ExtractionResult extractAllSheet() {
        // 获取所有sheet
        List<String> sheetNames = getSheetNames();
        ExtractionResult result = new ExtractionResult(this.totalSheetCount);
        // 对每个sheet进行处理
        int cnt = 0;
        for (int i = 0; i < sheetNames.size(); i++) {
            SheetExtractionResult sheetResult = extractSheetByIndex(i);
            if (!sheetResult.isSuccess()) {
                log.error("sheet:{}解析失败", sheetNames.get(i));
            } else {
                log.info("sheet:{}解析成功", sheetNames.get(i));
                cnt++;
            }
            result.addSheetResult(i, sheetResult);
            this.mappingConfig=this.originalMappingConfig.deepClone();
        }
        log.info("共提取: {}个sheet 成功: {}个 失败: {}个", totalSheetCount, cnt, totalSheetCount - cnt);
        return result;
    }

    /**
     * 从指定sheet中提取所有已配置类型的数据（推荐使用的主要方法）
     * 这是一个简洁的接口，是extractAllFromSheet的别名
     *
     * @param sheetIndex 要提取的sheet索引
     * @return 包含所有类型提取结果的Sheet结果对象
     */
    public SheetExtractionResult extractSheetByIndex(int sheetIndex) {
        return extractAllFromSheet(sheetIndex);
    }

    /**
     * 根据类名查找对应的提取器配置
     */
    private ExtractorConfig findExtractorForClass(String className) {
        for (ExtractorConfig extractor : mappingConfig.getAllExtractors()) {
            if (className.equals(extractor.getTargetClass())) {
                return extractor;
            }
        }
        return null;
    }

    /**
     * 根据sheet名称查找sheet索引
     *
     * @param sheetName sheet名称
     * @return sheet索引，如果未找到则返回-1
     */
    public int findSheetIndexByName(String sheetName) {
        List<String> sheetNames = getSheetNames();
        for (int i = 0; i < sheetNames.size(); i++) {
            if (sheetNames.get(i).contains(sheetName)) {
                return i;
            }
        }
        return -1; // 未找到
    }

    /**
     * 根据sheet名称提取所有配置的数据类型
     *
     * @param sheetName sheet名称
     * @return 提取结果
     */
    private SheetExtractionResult extractAllFromSheetName(String sheetName) {
        int sheetIndex = findSheetIndexByName(sheetName);
        if (sheetIndex == -1) {
            log.warn("未找到名称为 [{}] 的sheet", sheetName);
            return new SheetExtractionResult(-1, sheetName);
        }
        return extractAllFromSheet(sheetIndex);
    }

    /**
     * 根据sheet名称提取数据（简化接口）
     *
     * @param sheetName sheet名称
     * @return 提取结果
     */
    public SheetExtractionResult extractBySheetName(String sheetName) {
        return extractAllFromSheetName(sheetName);
    }

    // 添加方法来获取指定sheet的数据
    private Map<Integer, Map<Integer, String>> getSheetData(int sheetIndex) {
        try {
            ExcelDataListener excelDataListener = new ExcelDataListener(mappingConfig);
            boolean isXls = excelPath.toLowerCase().endsWith(".xls");
            EasyExcel.read(excelPath)
                    .excelType(isXls ? com.alibaba.excel.support.ExcelTypeEnum.XLS : com.alibaba.excel.support.ExcelTypeEnum.XLSX)
                    .sheet(sheetIndex)
                    .headRowNumber(0)
                    .registerReadListener(excelDataListener)
                    .doRead();
            return excelDataListener.getData();
        } catch (Exception e) {
            log.error("获取sheet[{}]数据失败: {}", sheetIndex, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 提取对象组列表 - 专用于GROUP_LIST类型
     * 该方法将多行数据组合成一个对象   此方法用于提取标准形式的行组数据
     */
    private <T> List<T> extractGroupObjectList(ExtractorConfig extractor, Class<T> elementType) throws Exception {
        List<T> resultList = new ArrayList<>();

        // 确定表格范围
        int startRow = resolveRowIndex(extractor.getStartRow());
        int endRow = extractor.getEndRow() != null ? resolveRowIndex(extractor.getEndRow()) :
                    findLastDataRow(startRow, 0); // 如果未指定结束行，自动查找

        // 获取行组计数（每组包含多少行）
        int groupRowCount = extractor.getGroupRowCount();
        if(groupRowCount <= 0)log.error("GROUP_LIST提取错误: 未指定每组行数!");
        log.info("GROUP_LIST提取: 开始行={}, 结束行={}, 每组行数={}", startRow, endRow, groupRowCount);


        // 计算组数
        int groupCount = (endRow - startRow + 1) / groupRowCount;
        // 提取表格数据
        if (extractor.getTable() != null) {
            TableConfig tableConfig = extractor.getTable();

            // 逐组处理数据
            for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
                int groupStartRow = startRow + groupIndex * groupRowCount;

                // 检查是否有足够的数据行
                if (!currentSheetData.containsKey(groupStartRow)) continue;

                // 创建当前组的对象实例
                T groupInstance = elementType.getDeclaredConstructor().newInstance();
                boolean hasData = false;

                // 处理组内每个列的数据
                for (Map.Entry<String, ColumnConfig> entry : tableConfig.getColumns().entrySet()) {
                    ColumnConfig column = entry.getValue();
                    try {
                        // 获取字段并设置可访问
                        Field field = groupInstance.getClass().getDeclaredField(column.getJavaFieldName());
                        field.setAccessible(true);

                        // 确定从组内哪一行取值（默认第一行）
                        int groupRowIndex = column.getGroupRowIndex() != null ? column.getGroupRowIndex() : 1;
                        // 计算实际行号（groupRowIndex是从1开始的）
                        int actualRow = groupStartRow + groupRowIndex - 1;

                        // 检查行是否存在
                        if (!currentSheetData.containsKey(actualRow)) continue;

                        // 计算列索引
                        int col = ExcelCoordConverter.columnNameToIndex(column.getColumnCell());

                        // 获取单元格值
                        String cellValue = currentSheetData.get(actualRow).get(col);
                        if (StringUtils.isEmpty(cellValue)) {
                            // 对于合并单元格，可能需要特殊处理
                            if (BooleanUtil.isTrue(column.getIsMergeType())) {
                                // 尝试从组内其他行查找值
                                for (int i = 0; i < groupRowCount; i++) {
                                    int searchRow = groupStartRow + i;
                                    if (currentSheetData.containsKey(searchRow) &&
                                        currentSheetData.get(searchRow).containsKey(col)) {
                                        String value = currentSheetData.get(searchRow).get(col);
                                        if (!StringUtils.isEmpty(value)) {
                                            cellValue = value;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        // 处理单元格值并设置到对象字段
                        if (!StringUtils.isEmpty(cellValue)) {
                            // 应用提取模式（如果有）
                            if (!StringUtils.isEmpty(column.getExtractPattern())) {
                                String extractedValue = extractValueByPattern(cellValue.trim(), column.getExtractPattern());
                                if (extractedValue != null) {
                                    cellValue = extractedValue;
                                }
                            }

                            // 转换值并设置到对象字段
                            Object convertedValue = convertValue(cellValue.trim(), column.getJavaFieldType(), null);
                            if (convertedValue != null) {
                                field.set(groupInstance, convertedValue);
                                hasData = true;
                            }
                        }
                    } catch (Exception e) {
                        log.error("提取字段[{}]失败: {}", column.getJavaFieldName(), e.getMessage(), e);
                    }
                }

                // 如果组有有效数据，添加到结果集
                if (hasData) {
                    resultList.add(groupInstance);
                }
            }
        }

        log.info("成功提取GROUP_LIST数据，共{}条记录", resultList.size());
        return resultList;
    }

    /**
     * 提取垂直对象列表 - 专用于VERTICAL_LIST类型
     * 该方法将垂直排列的数据转换为对象列表，每行代表一个字段，每列代表一个对象
     */
    private <T> List<T> extractVerticalObjectList(ExtractorConfig extractor, Class<T> elementType) throws Exception {
        List<T> resultList = new ArrayList<>();

        // 确定表格范围
        int startRow;
        if (extractor.getStartRow() != null) {
            startRow = resolveRowIndex(extractor.getStartRow());
        } else {
            startRow = 0;
        }
        String startColumnStr = extractor.getStartColumn();
        int startCol = startColumnStr != null ? ExcelCoordConverter.columnNameToIndex(startColumnStr) : 0;

        // 如果有endRow则使用，否则使用endFlag或自动查找最后一行
        int endRow;
        if (extractor.getEndRow() != null) {
            endRow = resolveRowIndex(extractor.getEndRow());
        } else {
            endRow = findLastDataRow(startRow, startCol);
        }
        // 查找最后一列（根据数据存在情况自动判断）
        int endCol = findLastDataColumn(startRow, startCol, endRow);

        log.info("VERTICAL_LIST: "+elementType.getSimpleName()+"提取: 开始行={}, 结束行={}, 开始列={}, 结束列={}",
                startRow+1, endRow+1, ExcelCoordConverter.indexToColumnName(startCol), ExcelCoordConverter.indexToColumnName(endCol));

        // 提取表格数据
        if (extractor.getTable() != null) {
            TableConfig tableConfig = extractor.getTable();

            // 对每列数据创建一个对象（每列代表一个完整的对象）
            for (int col = startCol; col <= endCol; col++) {
                // 检查该列是否有数据
                boolean hasData = false;
                for (int row = startRow; row <= endRow; row++) {
                    if (currentSheetData.containsKey(row) &&
                        currentSheetData.get(row).containsKey(col) &&
                        !StringUtils.isEmpty(currentSheetData.get(row).get(col))) {
                        hasData = true;
                        break;
                    }
                }

                // 如果该列没有数据，跳过
                if (!hasData) {
                    continue;
                }

                // 为当前列创建一个新对象
                T columnInstance = elementType.getDeclaredConstructor().newInstance();
                boolean validInstance = false;

                // 处理配置的每个字段
                for (Map.Entry<String, ColumnConfig> entry : tableConfig.getColumns().entrySet()) {
                    ColumnConfig column = entry.getValue();
                    try {
                        // 获取字段并设置可访问
                        Field field = columnInstance.getClass().getDeclaredField(column.getJavaFieldName());
                        field.setAccessible(true);

                        // 获取字段所在行（通过rowCell指定）
                        if (column.getRowCell() == null) {
                            log.warn("垂直列表配置错误：字段 {} 未指定rowCell", column.getJavaFieldName());
                            continue;
                        }

                        // 计算行索引
                        int fieldRow = resolveRowIndex(column.getRowCell());

                        // 检查行是否存在
                        if (!currentSheetData.containsKey(fieldRow)) {
                            continue;
                        }

                        // 获取单元格值
                        String cellValue = currentSheetData.get(fieldRow).get(col);

                        if (StrUtil.isNotEmpty(cellValue)) {
                            // 应用提取模式
                            if (StrUtil.isNotEmpty(column.getExtractPattern())) {
                                String extractedValue = extractValueByPattern(cellValue.trim(), column.getExtractPattern());
                                if (extractedValue != null) {
                                    cellValue = extractedValue;
                                }
                            }

                            // 转换值并设置到对象字段
                            Object convertedValue = convertValue(cellValue.trim(), column.getJavaFieldType(), null);
                            if (convertedValue != null) {
                                field.set(columnInstance, convertedValue);
                                validInstance = true;
                            }
                        }
                    } catch (Exception e) {
                        log.error("提取垂直列表字段[{}]失败: {}", column.getJavaFieldName(), e.getMessage(), e);
                    }
                }

                // 如果对象有效，添加到结果列表
                if (validInstance) {
                    resultList.add(columnInstance);
                }
            }
        }

        return resultList;
    }
    
    /**
     * 查找最后一列数据
     * 通过检查数据存在情况自动判断最后一列
     */
    private int findLastDataColumn(int startRow, int startCol, int endRow) {
        int lastCol = startCol;
        
        // 遍历所有行，找出最大的列索引
        for (int row = startRow; row <= endRow; row++) {
            if (!currentSheetData.containsKey(row)) {
                continue;
            }
            
            // 获取行数据
            Map<Integer, String> rowData = currentSheetData.get(row);
            if (rowData == null || rowData.isEmpty()) {
                continue;
            }
            
            // 查找该行中最大的列索引
            int maxColIndex = rowData.keySet().stream()
                    .filter(col -> col >= startCol && !StringUtils.isEmpty(rowData.get(col)))
                    .max(Integer::compareTo)
                    .orElse(startCol);
            
            // 更新最后一列
            lastCol = Math.max(lastCol, maxColIndex);
        }
        
        return lastCol;
    }

}

