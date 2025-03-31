package com.excel.shift.util;


import com.alibaba.excel.util.StringUtils;
import com.excel.shift.config.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.dromara.hutool.core.util.BooleanUtil;
import org.dromara.hutool.core.util.ObjUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 动态表达式解析器
 * 用于解析和计算配置中的动态表达式
 */
@Slf4j
public class DynamicExpressionResolver {

    private final String MERGE_FIXED = "fixed";
    // listener提取的所有数据
    @Setter
//    private TreeMap<Integer, Map<Integer, String>> data = new TreeMap<>();
    // Excel所有配置
    private ExcelMappingConfig mappingConfig;
    // 带有endFlag的表格
    private final Set<ExtractorConfig> dynamicEndRowTableClass = new HashSet();
    // 创建jexl上下文并设置变量  // 表达式和值的映射关系  例如gasTestDataList.endRow 对应一个值，存的时候要存integer  不然jexl无法计算
    private final JexlContext context = new MapContext();
    // 所有的动态表格 是随着上面动态行的变化而导致坐标变化的
    private final Set<ExtractorConfig> dynamicTableClass = new HashSet();
    // 类里面只有字段是需要动态计算的那种，如果又有表格和字段都需要动态计算，那么就存到上面一个hashmap
    private final Set<ExtractorConfig> dynamicFieldClass = new HashSet();
    // 带有startFlag的表格
    private final Set<ExtractorConfig> dynamicStartRowTableClass = new HashSet<>();

    /**
     * 构造函数
     *
     * @param mappingConfig Excel配置
     */
    public DynamicExpressionResolver(ExcelMappingConfig mappingConfig) {
        // Excel配置
        this.mappingConfig = mappingConfig;
        findAllDynamicComputeClass();

    }

    /**
     * 找出配置里面需要所有动态解析的字段和表格，表格里面某些字段也可能需要动态解析
     */
    private void findAllDynamicComputeClass() {
        for (ExtractorConfig extractorConfig : mappingConfig.getAllExtractors()) {
            Map<String, FieldConfig> fields = extractorConfig.getFields();
            TableConfig table = extractorConfig.getTable();
            if (!Objects.isNull(fields)) {
                for (Map.Entry<String, FieldConfig> entry : fields.entrySet()) {
                    if (null != entry.getValue().getIsDynamic() && entry.getValue().getIsDynamic()) {
                        dynamicFieldClass.add(extractorConfig);
                    }
                }
            }
            if (BooleanUtil.isTrue(extractorConfig.getIsDynamic())) {
                dynamicTableClass.add(extractorConfig);
            }
            if (BooleanUtil.isTrue(extractorConfig.getIsDynamicRows())){
                if(ObjUtil.isNotNull(extractorConfig.getEndFlag()))dynamicEndRowTableClass.add(extractorConfig);
                if (ObjUtil.isNotNull(extractorConfig.getStartFlag()))dynamicStartRowTableClass.add(extractorConfig);
            }

            if (!Objects.isNull(table)) {
                for (Map.Entry<String, ColumnConfig> columnConfigEntry : table.getColumns().entrySet()) {
                    if (BooleanUtil.isTrue(columnConfigEntry.getValue().getIsDynamic())) {
                        dynamicTableClass.add(extractorConfig);
                        break;
                    }
                }
            }
        }
//        log.info("所有动态解析endFlag的表格：");
//        dynamicEndRowTableClass.forEach(System.out::println);
//        log.info("所有动态解析startFlag的表格：");
//        dynamicStartRowTableClass.forEach(System.out::println);
//        System.out.println("====================================");
//        exit(0);
    }


    /**
     * 更新所有动态位置
     * 使用 CompletableFuture 的链式调用来保证执行顺序：
     * 1. 先执行 updateDynamicRowTablePosition
     * 2. 然后执行 updateDynamicTablePosition
     * 3. 最后执行 updateDynamicFieldPosition
     *
     * @return 更新是否成功
     */
    public boolean updateDynamicPosition() {
        try {
            AtomicBoolean success = new java.util.concurrent.atomic.AtomicBoolean(true);
            // 构建异步任务链，并正确传递结果状态
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // 第二步：更新动态表格位置
                try {
                    success.set(updateDynamicTablePosition());
                } catch (Exception e) {
                    log.error("更新动态表格位置失败", e);
                    success.set(false);
                }
            }).thenRunAsync(() -> {
                // 第三步：更新动态字段位置
                try {
                    updateDynamicFieldPosition();
                } catch (Exception e) {
                    log.error("更新动态字段位置失败", e);
                    success.set(false);
                }
            });

            // 等待任务完成并处理整体异常
            future.exceptionally(throwable -> {
                log.error("动态位置更新过程中发生错误", throwable);
                success.set(false);
                return null;
            }).join();
            // 返回最终结果
            return success.get();
        } catch (Exception e) {
            log.error("动态位置更新过程中发生未预期错误", e);
            return false;
        }
    }


    public void updateDynamicRowTablePosition(int rowIndex, Map<Integer, String> rowData) {
        if (dynamicEndRowTableClass.isEmpty()) {
            return;
        }
        Iterator<ExtractorConfig> iterator = dynamicEndRowTableClass.iterator();
        while (iterator.hasNext()) {
            ExtractorConfig extractorConfig = iterator.next();
            String etext = extractorConfig.getEndFlag().getText().replaceAll("\\s+", "");
            String ecolumnCell = extractorConfig.getEndFlag().getColumnCell();
            String evalue = rowData.get(ExcelCoordConverter.columnNameToIndex(ecolumnCell));

            if (evalue != null) {
                evalue = evalue.replaceAll("\\s+", "");
                if ((evalue.contains(etext) || etext.contains(evalue))) {
                    extractorConfig.setEndRow(String.valueOf(rowIndex));
                    mappingConfig.updateExtractor(extractorConfig);
                    context.set(extractorConfig.getId() + ".endRow", rowIndex);
                    iterator.remove();
                }
            }
        }
    }


    public void updateDynamicStartRowTablePosition(int rowIndex, Map<Integer, String> rowData) {
        if (dynamicStartRowTableClass.isEmpty()) {
            return;
        }
        Iterator<ExtractorConfig> iterator = dynamicStartRowTableClass.iterator();
        while (iterator.hasNext()) {
            ExtractorConfig extractorConfig = iterator.next();
            String stext = extractorConfig.getStartFlag().getText().replaceAll("\\s+", "");
            String scolumnCell = extractorConfig.getStartFlag().getColumnCell();
            String svalue = rowData.get(ExcelCoordConverter.columnNameToIndex(scolumnCell));

            if (svalue != null) {
                svalue = svalue.replaceAll("\\s+", "");
                if ((svalue.contains(stext) || stext.contains(svalue))) {
                    extractorConfig.setStartRow(String.valueOf(rowIndex+2));
                    mappingConfig.updateExtractor(extractorConfig);
                    context.set(extractorConfig.getId() + ".startRow", rowIndex+2);
                    iterator.remove();
                }
            }
        }
    }


    private boolean updateDynamicTablePosition() {
        if (dynamicTableClass.isEmpty()) {
            return true;
        }
        int i = 0;
        while (!dynamicTableClass.isEmpty()) {
            Iterator<ExtractorConfig> iterator = dynamicTableClass.iterator();
            while (iterator.hasNext()) {
                ExtractorConfig config = iterator.next();
                int isRemove = 0;
                // 先检查顶层的startRow和endRow 的表达式
                String startRowExpr = ExpressionExtractor.extractExpression(config.getStartRow());
                String endRowExpr = ExpressionExtractor.extractExpression(config.getEndRow());
                Object startRowValue = null;
                if (startRowExpr != null) {
                    startRowValue = resolveExpression(startRowExpr);
//                    System.out.println("startRowExpr:"+startRowExpr+" startRowValue:"+startRowValue);
                    if (startRowValue instanceof Integer && (!context.has(config.getId() + ".startRow") || context.get(config.getId() + ".startRow") instanceof Integer)) {
                        config.setStartRow(startRowValue.toString());
                        context.set(config.getId() + ".startRow", startRowValue);
                    } else isRemove++;
                }
                Object endRowValue = null;
                if (endRowExpr != null) {
                    endRowValue = resolveExpression(endRowExpr);
                    if (endRowValue instanceof Integer && (!context.has(config.getId() + ".endRow") || context.get(config.getId() + ".endRow") instanceof Integer)) {
                        config.setEndRow(endRowValue.toString());
                        context.set(config.getId() + ".endRow", endRowValue);
                    } else isRemove++;
                }
                mappingConfig.updateExtractor(config);
                // 删除不再需要的 dynamicTable
                if (isRemove == 0) {
                    iterator.remove();  // 使用 iterator.remove() 进行安全删除
                }
            }
            i++;
            if (i == 1000) {
                return false;
            }
            //        System.out.println("------------dynamicFiled---------------");
//        dynamicFieldClass.forEach(System.out::println);
//        System.out.println("-----------dynamicTableClass----------------");
//        dynamicTableClass.forEach(System.out::println);
//        System.out.println("------------dynamicRowTableClass---------------");
//        dynamicRowTableClass.forEach(System.out::println);
        }
        return true;
    }

    private void updateDynamicFieldPosition() {
        if(dynamicFieldClass.isEmpty()){
            return ;
        }
        Iterator<ExtractorConfig> iterator = dynamicFieldClass.iterator();
        while (iterator.hasNext()) {
            ExtractorConfig config = iterator.next();
            for (Map.Entry<String, FieldConfig> entry : config.getFields().entrySet()) {
                if (BooleanUtil.isTrue(entry.getValue().getIsDynamic())) {
                    FieldConfig fieldConfig = entry.getValue();
                    String expression = ExpressionExtractor.extractExpression(entry.getValue().getExcelCell());
                    Object value = resolveExpression(expression);
                    if (value != null) {
                        fieldConfig.setExcelCell(ExpressionExtractor.replaceExpression(fieldConfig.getExcelCell(), value.toString()));
                        config.updateField(entry.getKey(), fieldConfig);
                    }
                }
            }
            mappingConfig.updateExtractor(config);
        }
    }


    public Object resolveExpression(String expression) {
        if (StringUtils.isBlank(expression)) {
            return null;
        }
        // 创建 JEXL 引擎
        JexlEngine jexl = new JexlBuilder()
                .silent(false)  // 静默错误
                .strict(true)    // 严格模式
                .permissions(JexlPermissions.UNRESTRICTED)
                .create();

        // 创建并执行表达式
        JexlExpression jexlExpression = jexl.createExpression(expression);
        Object result;
        try {
            result = jexlExpression.evaluate(this.context);
        } catch (Exception e) {
            result = null;
        }
        return result;
    }


    /**
     * 打印JexlContext中的所有变量
     */
    private void printJexlContext() {
        // 遍历 context 中的所有变量
        // 获取 MapContext 类的所有字段
        Field[] fields = MapContext.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // 设置访问权限
            try {
                Object value = field.get(this.context);
                System.out.println("Field: " + field.getName() + ", Value: " + value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
