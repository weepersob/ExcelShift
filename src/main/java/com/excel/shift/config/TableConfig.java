package com.excel.shift.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 表格配置
 */
@Data
@Accessors(chain = true)
public class TableConfig {
    /**
     * 开始列
     */
    private String startColumn;

    /**
     * 表格列配置，使用Map或List
     */
    private Map<String, ColumnConfig> columns;

    /**
     * 描述
     */
    private String description;
    
    /**
     * 根据键获取列配置
     * @param key 列键
     * @return 列配置，如果不存在则返回null
     */
    public ColumnConfig getColumnByKey(String key) {
        if (columns == null || key == null) {
            return null;
        }
        return columns.get(key);
    }
    
    /**
     * 根据顺序获取列配置
     * @param order 列顺序
     * @return 列配置，如果不存在则返回null
     */
    public ColumnConfig getColumnByOrder(int order) {
        if (columns == null) {
            return null;
        }
        
        for (ColumnConfig column : columns.values()) {
            if (column.getOrder() != null && column.getOrder() == order) {
                return column;
            }
        }
        
        return null;
    }
    
    /**
     * 添加列配置
     * @param key 列键
     * @param column 列配置
     * @return 是否添加成功
     */
    public boolean addColumn(String key, ColumnConfig column) {
        if (key == null || column == null) {
            return false;
        }
        
        if (columns == null) {
            columns = new LinkedHashMap<>();
        }
        
        if (columns.containsKey(key)) {
            return false;
        }
        
        columns.put(key, column);
        sortColumns();
        return true;
    }
    
    /**
     * 更新列配置
     * @param key 列键
     * @param column 更新后的列配置
     * @return 是否更新成功
     */
    public boolean updateColumn(String key, ColumnConfig column) {
        if (key == null || column == null || columns == null) {
            return false;
        }
        
        if (!columns.containsKey(key)) {
            return false;
        }
        
        columns.put(key, column);
        sortColumns();
        return true;
    }
    
    /**
     * 删除列配置
     * @param key 列键
     * @return 是否删除成功
     */
    public boolean removeColumn(String key) {
        if (key == null || columns == null) {
            return false;
        }
        
        if (!columns.containsKey(key)) {
            return false;
        }
        
        columns.remove(key);
        return true;
    }
    
    /**
     * 获取列数量
     * @return 列数量
     */
    public int getColumnCount() {
        return columns == null ? 0 : columns.size();
    }
    
    /**
     * 清空所有列配置
     */
    public void clearColumns() {
        if (columns != null) {
            columns.clear();
        }
    }
    
    /**
     * 按顺序排序列
     */
    public void sortColumns() {
        if (columns == null || columns.isEmpty()) {
            return;
        }
        
        Map<String, ColumnConfig> sortedColumns = columns.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.comparing(ColumnConfig::getOrder)))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        
        this.columns = sortedColumns;
    }
    
    /**
     * 批量添加列配置
     * @param columnsToAdd 要添加的列配置Map
     * @return 添加成功的列数量
     */
    public int addColumns(Map<String, ColumnConfig> columnsToAdd) {
        if (columnsToAdd == null || columnsToAdd.isEmpty()) {
            return 0;
        }
        
        if (columns == null) {
            columns = new LinkedHashMap<>();
        }
        
        int addedCount = 0;
        for (Map.Entry<String, ColumnConfig> entry : columnsToAdd.entrySet()) {
            if (!columns.containsKey(entry.getKey())) {
                columns.put(entry.getKey(), entry.getValue());
                addedCount++;
            }
        }
        
        if (addedCount > 0) {
            sortColumns();
        }
        
        return addedCount;
    }

} 