package com.excel.shift.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * 提取器配置基类
 * 作为所有提取器配置的基础类
 */
@Data
@Accessors(chain = true)
public class ExtractorConfig {
    /**
     * 提取器ID
     */
    private String id;

    /**
     * 目标类全限定名
     */
    private String targetClass;

    /**
     * 提取器描述
     */
    private String description;

    /**
     * 提取器顺序
     */
    private Integer order;

    /**
     * 结果类型
     */
    private ResultType resultType;

    /**
     * 行组计数（用于GROUP_LIST类型）
     * 表示多少行数据组成一个数据对象
     */
    private Integer groupRowCount;

    /**
     * 字段映射配置
     */
    private Map<String, FieldConfig> fields;

    /**
     * 表格配置（从数组修改为单个对象）
     */
    private TableConfig table;

    /**
     * 开始行
     * 适用于整个提取器的表格范围
     */
    private String startRow;

    /**
     * 开始列
     * 适用于整个提取器的表格范围
     */
    private String startColumn;

    /**
     * 结束行
     * 适用于整个提取器的表格范围
     */
    private String endRow;

    /**
     * 是否动态
     * 适用于整个提取器的表格范围
     */
    private Boolean isDynamic;

    /**
     * 是否动态行
     * 适用于整个提取器的表格范围
     */
    private Boolean isDynamicRows;

    /**
     * 结束标志
     * 适用于整个提取器的表格范围
     */
    private EndFlagConfig endFlag;

    /**
     * 开始标志配置
     */
    private EndFlagConfig startFlag;

    /**
     * 打印配置信息
     */
    public void printConfig() {
        System.out.println("\n========== ExtractorConfig ==========");
        System.out.println("id: " + id);
        System.out.println("targetClass: " + targetClass);
        System.out.println("description: " + description);
        System.out.println("order: " + order);
        System.out.println("resultType: " + resultType);
        if (resultType == ResultType.GROUP_LIST) {
            System.out.println("groupRowCount: " + groupRowCount);
        }

        System.out.println("\n--- Basic Config ---");
        System.out.println("startRow: " + startRow);
        System.out.println("startColumn: " + startColumn);
        System.out.println("endRow: " + endRow);
        System.out.println("isDynamic: " + isDynamic);
        System.out.println("isDynamicRows: " + isDynamicRows);

        if (endFlag != null) {
            System.out.println("\n--- EndFlag Config ---");
            System.out.println("text: " + endFlag.getText());
            System.out.println("columnCell: " + endFlag.getColumnCell());
        }
        if (startFlag != null) {
            System.out.println("\n--- StartFlag Config ---");
            System.out.println("text: " + startFlag.getText());
            System.out.println("columnCell: " + startFlag.getColumnCell());
        }

        if (fields != null && !fields.isEmpty()) {
            System.out.println("\n--- Fields Config ---");
            fields.forEach((key, field) -> {
                System.out.println("\nField: " + key);
                System.out.println("  javaFieldName: " + field.getJavaFieldName());
                System.out.println("  excelCell: " + field.getExcelCell());
                System.out.println("  javaFieldType: " + field.getJavaFieldType());
                System.out.println("  description: " + field.getDescription());
                System.out.println("  unit: " + field.getUnit());
                System.out.println("  extractPattern: " + field.getExtractPattern());
                System.out.println("  defaultValue: " + field.getDefaultValue());
                System.out.println("  isDynamic: " + field.getIsDynamic());
                System.out.println("  order: " + field.getOrder());
            });
        }

        if (table != null) {
            System.out.println("\n--- Table Config ---");
            System.out.println("\nTable");
            System.out.println("  startColumn: " + table.getStartColumn());

            if (table.getColumns() != null && !table.getColumns().isEmpty()) {
                System.out.println("  columns:");
                table.getColumns().forEach((key, column) -> {
                    System.out.println("\n    Column: " + key);
                    System.out.println("      order: " + column.getOrder());
                    System.out.println("      javaFieldName: " + column.getJavaFieldName());
                    System.out.println("      columnCell: " + column.getColumnCell());
                    System.out.println("      javaFieldType: " + column.getJavaFieldType());
                    System.out.println("      description: " + column.getDescription());
                    System.out.println("      unit: " + column.getUnit());
                    if (resultType == ResultType.GROUP_LIST) {
                        System.out.println("      groupRowIndex: " + column.getGroupRowIndex());
                    }
                    System.out.println("      extractPattern: " + column.getExtractPattern());
                    System.out.println("      isDynamic: " + column.getIsDynamic());
                    System.out.println("      isMergeType: " + column.getIsMergeType());
                });
            }
        }
        System.out.println("\n====================================\n");
    }

    /**
     * 根据顺序获取表格配置 - 由于表格不再是数组，废弃此方法
     *
     * @deprecated 表格配置已经改为单个对象
     */
    @Deprecated
    public TableConfig getTableByOrder(int order) {
        return table;
    }

    /**
     * 根据索引获取表格配置 - 由于表格不再是数组，废弃此方法
     *
     * @deprecated 表格配置已经改为单个对象
     */
    @Deprecated
    public TableConfig getTableByIndex(int index) {
        if (index == 0) {
            return table;
        }
        return null;
    }

    /**
     * 更新表格配置
     */
    public boolean updateTableConfig(TableConfig updatedTableConfig) {
        if (updatedTableConfig == null) {
            return false;
        }

        this.table = updatedTableConfig;
        return true;
    }

    /**
     * 删除表格配置
     *
     * @return 是否删除成功
     */
    public boolean removeTable() {
        if (table == null) {
            return false;
        }

        this.table = null;
        return true;
    }

    /**
     * 获取表格数量
     *
     * @return 表格数量
     */
    public int getTableCount() {
        return table == null ? 0 : 1;
    }

    /**
     * 更新表格中的列配置
     *
     * @param columnKey     列键
     * @param updatedColumn 更新后的列配置
     * @return 是否更新成功
     */
    public boolean updateTableColumn(String columnKey, ColumnConfig updatedColumn) {
        if (table == null || columnKey == null || updatedColumn == null) {
            return false;
        }

        Map<String, ColumnConfig> columns = table.getColumns();
        if (columns == null || !columns.containsKey(columnKey)) {
            return false;
        }

        columns.put(columnKey, updatedColumn);
        return true;
    }

    /**
     * 添加表格列配置
     *
     * @param columnKey 列键
     * @param newColumn 新的列配置
     * @return 是否添加成功
     */
    public boolean addTableColumn(String columnKey, ColumnConfig newColumn) {
        if (table == null || columnKey == null || newColumn == null) {
            return false;
        }

        Map<String, ColumnConfig> columns = table.getColumns();
        if (columns == null) {
            columns = new java.util.LinkedHashMap<>();
            table.setColumns(columns);
        }

        if (columns.containsKey(columnKey)) {
            return false;
        }

        columns.put(columnKey, newColumn);

        // 重新排序列
        Map<String, ColumnConfig> sortedColumns = columns.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByValue(java.util.Comparator.comparing(ColumnConfig::getOrder)))
                .collect(java.util.stream.Collectors.toMap(
                        java.util.Map.Entry::getKey,
                        java.util.Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));

        table.setColumns(sortedColumns);
        return true;
    }

    /**
     * 删除表格列配置
     *
     * @param columnKey 列键
     * @return 是否删除成功
     */
    public boolean removeTableColumn(String columnKey) {
        if (table == null || columnKey == null) {
            return false;
        }

        Map<String, ColumnConfig> columns = table.getColumns();
        if (columns == null || !columns.containsKey(columnKey)) {
            return false;
        }

        columns.remove(columnKey);
        return true;
    }

    /**
     * 获取字段配置
     *
     * @param key 字段键
     * @return 字段配置
     */
    public FieldConfig getField(String key) {
        return fields != null ? fields.get(key) : null;
    }

    /**
     * 获取所有字段配置
     *
     * @return 字段配置映射
     */
    public Map<String, FieldConfig> getFields() {
        return fields;
    }

    /**
     * 添加字段配置
     *
     * @param key   字段键
     * @param field 字段配置
     * @return 是否添加成功
     */
    public boolean addField(String key, FieldConfig field) {
        if (key == null || field == null) {
            return false;
        }
        if (fields == null) {
            fields = new LinkedHashMap<>();
        }
        fields.put(key, field);
        return true;
    }

    /**
     * 更新字段配置
     *
     * @param key   字段键
     * @param field 新的字段配置
     * @return 是否更新成功
     */
    public boolean updateField(String key, FieldConfig field) {
        if (key == null || field == null || fields == null || !fields.containsKey(key)) {
            return false;
        }
        fields.put(key, field);
        return true;
    }

    /**
     * 删除字段配置
     *
     * @param key 字段键
     * @return 是否删除成功
     */
    public boolean removeField(String key) {
        if (key == null || fields == null) {
            return false;
        }
        return fields.remove(key) != null;
    }

    /**
     * 清空所有字段配置
     */
    public void clearFields() {
        if (fields != null) {
            fields.clear();
        }
    }

    /**
     * 批量添加字段配置
     *
     * @param fieldsToAdd 要添加的字段配置映射
     * @return 是否添加成功
     */
    public boolean addFields(Map<String, FieldConfig> fieldsToAdd) {
        if (fieldsToAdd == null || fieldsToAdd.isEmpty()) {
            return false;
        }
        if (fields == null) {
            fields = new LinkedHashMap<>();
        }
        fields.putAll(fieldsToAdd);
        return true;
    }

    /**
     * 获取字段数量
     *
     * @return 字段数量
     */
    public int getFieldCount() {
        return fields == null ? 0 : fields.size();
    }

    /**
     * 结果类型枚举
     */
    public enum ResultType {
        /**
         * 单个对象
         */
        SINGLE,

        /**
         * 对象列表
         */
        LIST,
        
        /**
         * 组行对象列表
         * 多行组成一个对象的列表
         */
        GROUP_LIST,
        
        /**
         * 垂直对象列表
         * 每行代表一个字段，每列代表一个对象
         */
        VERTICAL_LIST
    }
}
