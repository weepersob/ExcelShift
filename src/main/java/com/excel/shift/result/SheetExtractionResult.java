package com.excel.shift.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单个Sheet的提取结果
 */
public class SheetExtractionResult {
    
    // sheet索引
    private final int sheetIndex;
    
    // sheet名称
    private final String sheetName;

    // 是否提取成功
    private boolean isSuccess=true;
    
    // 按类名存储提取结果
    private final Map<String, Object> results = new HashMap<>();
    
    public SheetExtractionResult(int sheetIndex, String sheetName) {
        this.sheetIndex = sheetIndex;
        this.sheetName = sheetName;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * 添加提取结果
     * @param clazz 结果类型
     * @param result 提取结果（可能是单个对象或对象列表）
     */
    public void addResult(Class<?> clazz, Object result) {
        results.put(clazz.getName(), result);
    }
    
    /**
     * 获取指定类型的提取结果（带类型转换）
     * @param clazz 目标类型
     * @param <T> 返回类型
     * @return 提取结果，已转换为指定类型
     */
    @SuppressWarnings("unchecked")
    public <T> T getResult(Class<T> clazz) {
        Object result = results.get(clazz.getName());
        return (T) result;
    }

    /**
     * 获取指定类型的列表提取结果
     * 当提取的结果本身就是列表时使用此方法
     * 如果结果不存在或不是列表类型，则返回空列表
     * @param elementClass 列表元素类型
     * @param <T> 列表元素类型
     * @return 提取结果列表，永不为null
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getResultList(Class<T> elementClass) {
        Object result = results.get(elementClass.getName());
        if (result instanceof List) {
            return (List<T>) result;
        }
        // 返回空列表而不是null
        return new ArrayList<>();
    }
    
    /**
     * 是否包含指定类型的提取结果
     * @param clazz 目标类型
     * @return 是否包含
     */
    public boolean containsResult(Class<?> clazz) {
        return results.containsKey(clazz.getName());
    }
    
    /**
     * 获取所有提取结果
     * @return 所有提取结果
     */
    public Map<String, Object> getAllResults() {
        return results;
    }
    
    /**
     * 获取sheet索引
     * @return sheet索引
     */
    public int getSheetIndex() {
        return sheetIndex;
    }
    
    /**
     * 获取sheet名称
     * @return sheet名称
     */
    public String getSheetName() {
        return sheetName;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    /**
     * 获取结果数量
     * @return 提取的类型数量
     */
    public int size() {
        return results.size();
    }
    
    /**
     * 是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        return results.isEmpty();
    }
    
    /**
     * 清空结果
     */
    public void clear() {
        results.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SheetExtractionResult{");
        sb.append("sheetIndex=").append(sheetIndex);
        sb.append(", sheetName='").append(sheetName).append('\'');
        sb.append(", results=").append(results);
        sb.append('}');
        return sb.toString();
    }
} 