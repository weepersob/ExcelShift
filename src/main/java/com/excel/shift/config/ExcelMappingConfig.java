package com.excel.shift.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.dromara.hutool.core.collection.CollUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Excel映射配置处理类
 */
@Slf4j
public class ExcelMappingConfig {

    /**
     * 所有提取器配置
     */
    private List<ExtractorConfig> extractors;

    /**
     * 使用指定配置文件构造
     *
     * @param configPath 配置文件路径
     */
    public ExcelMappingConfig(String configPath) {
        loadConfig(configPath);
    }


    /**
     * 无参构造函数，用于克隆
     */
    public ExcelMappingConfig() {
        this.extractors = new ArrayList<>();
    }

    // 通过request构建ExcelMappingConfig对象
    public static ExcelMappingConfig buildExcelMappingConfig(Request request) {
        ExcelMappingConfig excelMappingConfig = new ExcelMappingConfig();
        List<ExtractorConfig> extractors =new ArrayList<>();
        // 构建extractors列表
        AtomicReference<Integer> i= new AtomicReference<>(1);
        AtomicReference<Integer> j= new AtomicReference<>(1);
        request.getClassInfoList().forEach(classInfo -> {
            j.set(1);
            ExtractorConfig extractorConfig = new ExtractorConfig();
            extractorConfig.setId(classInfo.getSimpleName());
            extractorConfig.setOrder(i.getAndSet(i.get() + 1));
            extractorConfig.setResultType(ExtractorConfig.ResultType.LIST);  // 默认就是简单的list
            extractorConfig.setStartRow(String.valueOf(request.getStartRow()));
            extractorConfig.setEndRow(String.valueOf(request.getEndRow()));
            extractorConfig.setTargetClass(classInfo.getName());
            TableConfig tableConfig = new TableConfig();
            Map<String, ColumnConfig> columns= new HashMap<>();
            request.getColumnInfoList().forEach(columnInfo -> {
                ColumnConfig columnConfig = new ColumnConfig();
                columnConfig.setOrder(j.getAndSet(j.get() + 1));
                columnConfig.setColumnCell(columnInfo.getColumnSeq().toUpperCase());
                columnConfig.setJavaFieldName(columnInfo.getColumnName());
                columnConfig.setUnit(columnInfo.getColumnName_unit());
                Field field = null;
                try {
                    field = classInfo.getDeclaredField(columnInfo.getColumnName());
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                field.setAccessible(true);
                columnConfig.setJavaFieldType(field.getType().getName());
                columns.put(field.getName(), columnConfig);
            });
            tableConfig.setColumns(columns);
            extractorConfig.setTable(tableConfig);
            extractors.add(extractorConfig);
        });
        excelMappingConfig.extractors=extractors;
        return excelMappingConfig;
    }

    /**
     * 加载配置文件
     *
     * @param configPath 配置文件路径（可以是类路径或全路径）
     */
    public void loadConfig(String configPath) {
        try {
            String jsonContent;

            // 尝试作为全路径加载
            File file = new File(configPath);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    jsonContent = IOUtils.toString(fis, StandardCharsets.UTF_8);
                    log.info("加载配置文件: {}", configPath);
                }
            } else {
                // 尝试从类路径加载
                String resourcePath = configPath;
                if (!configPath.endsWith(".json")) {
                    resourcePath = configPath + ".json";
                }

                InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (is == null) {
                    log.error("找不到配置文件: " + configPath);
                    return;
                }

                jsonContent = IOUtils.toString(is, StandardCharsets.UTF_8);
                is.close();
                log.info("从类路径加载配置文件: {}", resourcePath);
            }

            JSONObject config = JSON.parseObject(jsonContent);
            List<ExtractorConfig> tempExtractors = new ArrayList<>();

            // 处理所有配置项
            for (String id : config.keySet()) {
                JSONObject extractorConfig = config.getJSONObject(id);
                extractorConfig.put("id", id);
                ExtractorConfig extractor = processExtractorConfig(extractorConfig);
                tempExtractors.add(extractor);
            }

            // 按order排序
            this.extractors = tempExtractors.stream()
                    .sorted(Comparator.comparing(ExtractorConfig::getOrder))
                    .collect(Collectors.toList());
            log.info("成功加载配置文件, 共加载了{}个提取类配置", extractors.size());

        } catch (Exception e) {
            log.error("加载配置文件失败: {}", configPath, e);
            throw new RuntimeException("加载配置文件失败", e);
        }
    }

    /**
     * 获取所有提取器配置（已按order排序）
     */
    public List<ExtractorConfig> getAllExtractors() {
        return extractors;
    }

    /**
     * 获取指定ID的提取器配置
     */
    public ExtractorConfig getExtractor(String id) {
        return extractors.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 处理提取器配置
     */
    private ExtractorConfig processExtractorConfig(JSONObject config) {
        String id = config.getString("id");
        log.info("处理提取类配置: {}", id);

        ExtractorConfig extractorConfig = new ExtractorConfig();
        extractorConfig.setId(id);
        extractorConfig.setTargetClass(config.getString("targetClass"));
        extractorConfig.setDescription(config.getString("description"));
        extractorConfig.setOrder(config.getInteger("order"));
        extractorConfig.setResultType(ExtractorConfig.ResultType.valueOf(config.getString("resultType")));

        // 处理行组计数参数（用于GROUP_LIST类型）
        if (extractorConfig.getResultType() == ExtractorConfig.ResultType.GROUP_LIST) {
            Integer groupRowCount = config.getInteger("groupRowCount");
            extractorConfig.setGroupRowCount(groupRowCount);
        }

        // 处理顶层表格配置
        if (config.containsKey("startRow")) {
            extractorConfig.setStartRow(config.getString("startRow"));
        }
        if (config.containsKey("startColumn")) {
            extractorConfig.setStartColumn(config.getString("startColumn"));
        }
        if (config.containsKey("endRow")) {
            extractorConfig.setEndRow(config.getString("endRow"));
        }
        if (config.containsKey("isDynamic")) {
            extractorConfig.setIsDynamic(config.getBoolean("isDynamic"));
        }
        if (config.containsKey("isDynamicRows")) {
            extractorConfig.setIsDynamicRows(config.getBoolean("isDynamicRows"));
        }
        if (config.containsKey("endFlag")) {
            JSONObject endFlagObj = config.getJSONObject("endFlag");
            EndFlagConfig endFlag = new EndFlagConfig();
            endFlag.setText(endFlagObj.getString("text"));
            endFlag.setColumnCell(endFlagObj.getString("columnCell"));
            extractorConfig.setEndFlag(endFlag);
        }

        // 处理startFlag配置
        if (config.containsKey("startFlag")) {
            JSONObject startFlagObj = config.getJSONObject("startFlag");
            EndFlagConfig startFlag = new EndFlagConfig();
            startFlag.setText(startFlagObj.getString("text"));
            startFlag.setColumnCell(startFlagObj.getString("columnCell"));
            extractorConfig.setStartFlag(startFlag);
        }

        // 处理字段映射配置
        if (config.containsKey("fields")) {
            JSONObject fieldsConfig = config.getJSONObject("fields");
            Map<String, FieldConfig> fields = fieldsConfig.entrySet().stream()
                    .map(entry -> {
                        JSONObject fieldObj = fieldsConfig.getJSONObject(entry.getKey());
                        FieldConfig field = new FieldConfig();
                        field.setJavaFieldName(fieldObj.getString("javaFieldName"));
                        field.setExcelCell(fieldObj.getString("excelCell"));
                        field.setJavaFieldType(fieldObj.getString("javaFieldType"));
                        field.setDescription(fieldObj.getString("description"));
                        field.setUnit(fieldObj.getString("unit"));
                        field.setExtractPattern(fieldObj.getString("extractPattern"));
                        field.setDefaultValue(fieldObj.getString("defaultValue"));
                        field.setIsDynamic(fieldObj.getBoolean("isDynamic"));
                        field.setOrder(fieldObj.getInteger("order"));
                        return new AbstractMap.SimpleEntry<>(entry.getKey(), field);
                    })
                    .sorted(Map.Entry.comparingByValue(Comparator.comparing(FieldConfig::getOrder)))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            extractorConfig.setFields(fields);
        }

        // 处理表格配置
        if (config.containsKey("table")) {
            JSONObject tableJson = config.getJSONObject("table");
            TableConfig tableConfig = new TableConfig();

            // 设置表格基本属性
            tableConfig.setStartColumn(tableJson.getString("startColumn"));

            // 处理列配置
            if (tableJson.containsKey("columns")) {
                JSONObject columnsConfig = tableJson.getJSONObject("columns");
                Map<String, ColumnConfig> columns = columnsConfig.entrySet().stream()
                        .map(entry -> {
                            JSONObject columnObj = columnsConfig.getJSONObject(entry.getKey());
                            ColumnConfig column = new ColumnConfig();
                            column.setJavaFieldName(columnObj.getString("javaFieldName"));
                            column.setColumnCell(columnObj.getString("columnCell"));
                            column.setJavaFieldType(columnObj.getString("javaFieldType"));
                            column.setDescription(columnObj.getString("description"));

                            // 添加对rowCell的解析（用于VERTICAL_LIST类型）
                            if (columnObj.containsKey("rowCell")) {
                                column.setRowCell(columnObj.getString("rowCell"));
                            }

                            // 安全获取可能为null的值
                            if (columnObj.containsKey("unit")) {
                                column.setUnit(columnObj.getString("unit"));
                            }
                            if (columnObj.containsKey("isMergeType")) {
                                column.setIsMergeType(columnObj.getBoolean("isMergeType"));
                            }
                            if (columnObj.containsKey("isDynamic")) {
                                column.setIsDynamic(columnObj.getBoolean("isDynamic"));
                            }
                            if (columnObj.containsKey("extractPattern")) {
                                column.setExtractPattern(columnObj.getString("extractPattern"));
                            }

                            column.setOrder(columnObj.getInteger("order"));

                            // 添加对行组索引的处理
                            Integer groupRowIndex = columnObj.getInteger("groupRowIndex");
                            if (groupRowIndex != null) {
                                column.setGroupRowIndex(groupRowIndex);
                            }
                            if (columnObj.containsKey("alternativeColumnCell")) {
                                column.setAlternativeColumnCell(columnObj.getJSONArray("alternativeColumnCell").toJavaList(String.class));
                                column.setAlternativeStrategy(columnObj.getString(("alternativeStrategy")));
                            }

                            return new AbstractMap.SimpleEntry<>(entry.getKey(), column);
                        })
                        .sorted(Map.Entry.comparingByValue(Comparator.comparing(ColumnConfig::getOrder)))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ));

                tableConfig.setColumns(columns);
            }

            extractorConfig.setTable(tableConfig);
        }

        return extractorConfig;
    }

    /**
     * 修改提取器配置
     *
     * @param extractorConfig 修改后的提取器配置
     * @return 是否修改成功
     */
    public boolean updateExtractor(ExtractorConfig extractorConfig) {
        if (extractorConfig == null || extractorConfig.getId() == null) {
            log.error("提取器配置或ID为空，无法更新");
            return false;
        }

        // 查找并替换提取器
        for (int i = 0; i < extractors.size(); i++) {
            if (extractors.get(i).getId().equals(extractorConfig.getId())) {
                extractors.set(i, extractorConfig);
//                log.info("成功更新提取器配置: {}", extractorConfig.getId());

                // 重新排序
                this.extractors = extractors.stream()
                        .sorted(Comparator.comparing(ExtractorConfig::getOrder))
                        .collect(Collectors.toList());
                return true;
            }
        }

        log.error("未找到ID为{}的提取器配置，更新失败", extractorConfig.getId());
        return false;
    }

    /**
     * 添加新的提取器配置
     *
     * @param extractorConfig 新的提取器配置
     * @return 是否添加成功
     */
    public boolean addExtractor(ExtractorConfig extractorConfig) {
        if (extractorConfig == null || extractorConfig.getId() == null) {
            log.error("提取器配置或ID为空，无法添加");
            return false;
        }

        // 检查ID是否已存在
        if (extractors.stream().anyMatch(e -> e.getId().equals(extractorConfig.getId()))) {
            log.error("ID为{}的提取器配置已存在，添加失败", extractorConfig.getId());
            return false;
        }

        extractors.add(extractorConfig);
        log.info("成功添加提取器配置: {}", extractorConfig.getId());

        // 重新排序
        this.extractors = extractors.stream()
                .sorted(Comparator.comparing(ExtractorConfig::getOrder))
                .collect(Collectors.toList());
        return true;
    }

    /**
     * 删除提取器配置
     *
     * @param id 提取器配置ID
     * @return 是否删除成功
     */
    public boolean deleteExtractor(String id) {
        if (id == null) {
            log.error("提取器ID为空，无法删除");
            return false;
        }

        int originalSize = extractors.size();
        extractors = extractors.stream()
                .filter(e -> !e.getId().equals(id))
                .collect(Collectors.toList());

        boolean success = extractors.size() < originalSize;
        if (success) {
            log.info("成功删除提取器配置: {}", id);
        } else {
            log.error("未找到ID为{}的提取器配置，删除失败", id);
        }

        return success;
    }

    /**
     * 将提取器配置转换为JSON对象
     *
     * @param extractor 提取器配置
     * @return JSON对象
     */
    private JSONObject convertExtractorToJson(ExtractorConfig extractor) {
        JSONObject json = new JSONObject();

        // 基本属性
        json.put("targetClass", extractor.getTargetClass());
        json.put("description", extractor.getDescription());
        json.put("order", extractor.getOrder());
        json.put("resultType", extractor.getResultType().name());

        // 表格基本配置
        if (extractor.getStartRow() != null) {
            json.put("startRow", extractor.getStartRow());
        }
        if (extractor.getStartColumn() != null) {
            json.put("startColumn", extractor.getStartColumn());
        }
        if (extractor.getEndRow() != null) {
            json.put("endRow", extractor.getEndRow());
        }
        if (extractor.getIsDynamic() != null) {
            json.put("isDynamic", extractor.getIsDynamic());
        }
        if (extractor.getIsDynamicRows() != null) {
            json.put("isDynamicRows", extractor.getIsDynamicRows());
        }

        // 结束标志
        if (extractor.getEndFlag() != null) {
            JSONObject endFlagJson = new JSONObject();
            endFlagJson.put("text", extractor.getEndFlag().getText());
            endFlagJson.put("columnCell", extractor.getEndFlag().getColumnCell());
            json.put("endFlag", endFlagJson);
        }
        if (extractor.getStartFlag() != null) {
            JSONObject startFlagJson = new JSONObject();
            startFlagJson.put("text", extractor.getStartFlag().getText());
            startFlagJson.put("columnCell", extractor.getStartFlag().getColumnCell());
            json.put("startFlag", startFlagJson);
        }

        // 字段配置
        if (extractor.getFields() != null && !extractor.getFields().isEmpty()) {
            JSONObject fieldsJson = new JSONObject();
            for (Map.Entry<String, FieldConfig> entry : extractor.getFields().entrySet()) {
                FieldConfig field = entry.getValue();
                JSONObject fieldJson = new JSONObject();
                fieldJson.put("order", field.getOrder());
                fieldJson.put("javaFieldName", field.getJavaFieldName());
                fieldJson.put("javaFieldType", field.getJavaFieldType());
                fieldJson.put("excelCell", field.getExcelCell());
                fieldJson.put("description", field.getDescription());

                if (field.getUnit() != null) {
                    fieldJson.put("unit", field.getUnit());
                }
                if (field.getExtractPattern() != null) {
                    fieldJson.put("extractPattern", field.getExtractPattern());
                }
                if (field.getDefaultValue() != null) {
                    fieldJson.put("defaultValue", field.getDefaultValue());
                }
                if (field.getIsDynamic() != null) {
                    fieldJson.put("isDynamic", field.getIsDynamic());
                }
                fieldsJson.put(entry.getKey(), fieldJson);
            }
            json.put("fields", fieldsJson);
        }

        // 表格配置
        if (extractor.getTable() != null) {
            JSONObject tableJson = new JSONObject();
            TableConfig table = extractor.getTable();

            tableJson.put("startColumn", table.getStartColumn());


            if (table.getColumns() != null && !table.getColumns().isEmpty()) {
                JSONObject columnsJson = new JSONObject();
                for (Map.Entry<String, ColumnConfig> entry : table.getColumns().entrySet()) {
                    ColumnConfig column = entry.getValue();
                    JSONObject columnJson = new JSONObject();
                    columnJson.put("order", column.getOrder());
                    columnJson.put("javaFieldName", column.getJavaFieldName());
                    columnJson.put("columnCell", column.getColumnCell());
                    columnJson.put("javaFieldType", column.getJavaFieldType());
                    columnJson.put("description", column.getDescription());

                    if (column.getUnit() != null) {
                        columnJson.put("unit", column.getUnit());
                    }
                    if (column.getExtractPattern() != null) {
                        columnJson.put("extractPattern", column.getExtractPattern());
                    }
                    if (column.getIsDynamic() != null) {
                        columnJson.put("isDynamic", column.getIsDynamic());
                    }
                    if (column.getIsMergeType() != null) {
                        columnJson.put("isMergeType", column.getIsMergeType());
                    }
                    if (CollUtil.isNotEmpty(column.getAlternativeColumnCell())) {
                        columnJson.put("alternativeColumnCell", column.getAlternativeColumnCell());
                        columnJson.put("alternativeStrategy", column.getAlternativeStrategy());
                    }
                    // 添加对rowCell的解析（用于VERTICAL_LIST类型）
                    if (column.getRowCell() != null) {
                        columnJson.put("rowCell", column.getRowCell());
                    }

                    columnsJson.put(entry.getKey(), columnJson);
                }
                tableJson.put("columns", columnsJson);
            }

            json.put("table", tableJson);
        }

        return json;
    }

    /**
     * 获取配置的JSON字符串表示
     *
     * @return JSON字符串
     */
    public String getConfigAsJsonString() {
        JSONObject config = new JSONObject();
        for (ExtractorConfig extractor : extractors) {
            JSONObject extractorJson = convertExtractorToJson(extractor);
            config.put(extractor.getId(), extractorJson);
        }
        return JSON.toJSONString(config, true);
    }

    /**
     * 从JSON字符串加载配置
     *
     * @param jsonContent JSON字符串
     * @return 是否加载成功
     */
    public boolean loadFromJsonString(String jsonContent) {
        try {
            JSONObject config = JSON.parseObject(jsonContent);
            List<ExtractorConfig> tempExtractors = new ArrayList<>();

            // 处理所有配置项
            for (String id : config.keySet()) {
                JSONObject extractorConfig = config.getJSONObject(id);
                extractorConfig.put("id", id);
                ExtractorConfig extractor = processExtractorConfig(extractorConfig);
                tempExtractors.add(extractor);
            }

            // 按order排序
            this.extractors = tempExtractors.stream()
                    .sorted(Comparator.comparing(ExtractorConfig::getOrder))
                    .collect(Collectors.toList());

            log.info("成功从JSON字符串加载配置, 共加载了{}个提取器配置", extractors.size());
            return true;
        } catch (Exception e) {
            log.error("从JSON字符串加载配置失败", e);
            return false;
        }
    }

    /**
     * 创建当前配置的深度克隆
     *
     * @return 当前配置的深度克隆副本
     */
    public ExcelMappingConfig deepClone() {
        // 创建新的配置对象
        ExcelMappingConfig cloned = new ExcelMappingConfig();

        // 克隆所有提取器配置
        if (this.extractors != null) {
            List<ExtractorConfig> clonedExtractors = new ArrayList<>();

            for (ExtractorConfig extractor : this.extractors) {
                ExtractorConfig clonedExtractor = new ExtractorConfig();

                // 复制基本属性
                clonedExtractor.setId(extractor.getId());
                clonedExtractor.setTargetClass(extractor.getTargetClass());
                clonedExtractor.setDescription(extractor.getDescription());
                clonedExtractor.setOrder(extractor.getOrder());
                clonedExtractor.setResultType(extractor.getResultType());
                clonedExtractor.setStartRow(extractor.getStartRow());
                clonedExtractor.setStartColumn(extractor.getStartColumn());
                clonedExtractor.setEndRow(extractor.getEndRow());
                clonedExtractor.setIsDynamic(extractor.getIsDynamic());
                clonedExtractor.setIsDynamicRows(extractor.getIsDynamicRows());

                // 复制GROUP_LIST类型相关的属性
                clonedExtractor.setGroupRowCount(extractor.getGroupRowCount());

                // 克隆endFlag
                if (extractor.getEndFlag() != null) {
                    EndFlagConfig clonedEndFlag = new EndFlagConfig();
                    clonedEndFlag.setText(extractor.getEndFlag().getText());
                    clonedEndFlag.setColumnCell(extractor.getEndFlag().getColumnCell());
                    clonedExtractor.setEndFlag(clonedEndFlag);
                }

                // 克隆startFlag
                if (extractor.getStartFlag() != null) {
                    EndFlagConfig clonedStartFlag = new EndFlagConfig();
                    clonedStartFlag.setText(extractor.getStartFlag().getText());
                    clonedStartFlag.setColumnCell(extractor.getStartFlag().getColumnCell());
                    clonedExtractor.setStartFlag(clonedStartFlag);
                }

                // 克隆fields映射
                if (extractor.getFields() != null) {
                    Map<String, FieldConfig> clonedFields = new LinkedHashMap<>();

                    for (Map.Entry<String, FieldConfig> entry : extractor.getFields().entrySet()) {
                        FieldConfig originalField = entry.getValue();
                        FieldConfig clonedField = new FieldConfig();

                        // 复制FieldConfig的属性
                        clonedField.setOrder(originalField.getOrder());
                        clonedField.setJavaFieldName(originalField.getJavaFieldName());
                        clonedField.setJavaFieldType(originalField.getJavaFieldType());
                        clonedField.setExcelCell(originalField.getExcelCell());
                        clonedField.setDescription(originalField.getDescription());
                        clonedField.setExtractPattern(originalField.getExtractPattern());
                        clonedField.setDefaultValue(originalField.getDefaultValue());
                        clonedField.setUnit(originalField.getUnit());
                        clonedField.setIsDynamic(originalField.getIsDynamic());

                        clonedFields.put(entry.getKey(), clonedField);
                    }

                    clonedExtractor.setFields(clonedFields);
                }

                // 克隆table配置
                if (extractor.getTable() != null) {
                    TableConfig originalTable = extractor.getTable();
                    TableConfig clonedTable = new TableConfig();

                    // 复制TableConfig的基本属性
                    clonedTable.setStartColumn(originalTable.getStartColumn());

                    // 克隆columns
                    if (originalTable.getColumns() != null) {
                        Map<String, ColumnConfig> clonedColumns = new LinkedHashMap<>();

                        for (Map.Entry<String, ColumnConfig> entry : originalTable.getColumns().entrySet()) {
                            ColumnConfig originalColumn = entry.getValue();
                            ColumnConfig clonedColumn = new ColumnConfig();

                            // 复制ColumnConfig的属性
                            clonedColumn.setOrder(originalColumn.getOrder());
                            clonedColumn.setJavaFieldName(originalColumn.getJavaFieldName());
                            clonedColumn.setColumnCell(originalColumn.getColumnCell());
                            clonedColumn.setJavaFieldType(originalColumn.getJavaFieldType());
                            clonedColumn.setDescription(originalColumn.getDescription());
                            clonedColumn.setUnit(originalColumn.getUnit());
                            clonedColumn.setIsMergeType(originalColumn.getIsMergeType());
                            clonedColumn.setIsDynamic(originalColumn.getIsDynamic());
                            clonedColumn.setExtractPattern(originalColumn.getExtractPattern());

                            // 添加对rowCell的解析（用于VERTICAL_LIST类型）
                           clonedColumn.setRowCell(originalColumn.getRowCell());
                            // 复制GROUP_LIST类型相关的属性
                            clonedColumn.setGroupRowIndex(originalColumn.getGroupRowIndex());

                            if(originalColumn.getAlternativeColumnCell() != null)
                            clonedColumn.setAlternativeColumnCell(new ArrayList<>(originalColumn.getAlternativeColumnCell()));
                            clonedColumn.setAlternativeStrategy(originalColumn.getAlternativeStrategy());

                            clonedColumns.put(entry.getKey(), clonedColumn);
                        }
                        clonedTable.setColumns(clonedColumns);
                    }
                    clonedExtractor.setTable(clonedTable);
                }

                clonedExtractors.add(clonedExtractor);
            }

            cloned.extractors = clonedExtractors;
        }

        return cloned;
    }
} 