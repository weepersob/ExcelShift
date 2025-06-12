package com.excel.shift.result;

import com.excel.shift.config.response.ColumnValueResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 单个Sheet的提取结果
 */
public class SheetExtractionResult {

    // sheet索引
    private final int sheetIndex;

    // sheet名称
    private final String sheetName;

    // 是否提取成功
    private boolean isSuccess = true;

    // 按类名存储提取结果
    private final Map<String, Object> results = new HashMap<>();

    // 存储提取过程中的错误信息，使用LinkedHashSet防止重复并保持顺序
    private final Set<ExtractionError> errors = new LinkedHashSet<>();

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

    /**
     * 添加错误信息
     * 相同错误信息不会重复添加（使用LinkedHashSet实现）
     * @param error 错误信息对象
     */
    public void addError(ExtractionError error) {
        if (error != null) {
            this.errors.add(error);
            // 当有错误时不自动设置失败，由调用者决定是否设置失败状态
        }
    }

    /**
     * 添加错误信息的便捷方法
     * @param message 错误消息
     * @param ex 异常对象
     * @param extractorId 提取器ID
     */
    public void addError(String message, Exception ex, String extractorId) {
        addError(new ExtractionError(message, ex, extractorId));
    }

    /**
     * 添加错误信息的便捷方法（带位置信息）
     * @param message 错误消息
     * @param ex 异常对象
     * @param extractorId 提取器ID
     * @param rowNumber 行号
     * @param columnInfo 列信息
     */
    public void addError(String message, Exception ex, String extractorId,
                         Integer rowNumber, String columnInfo) {
        addError(new ExtractionError(message, ex, extractorId, rowNumber, columnInfo));
    }

    /**
     * 获取所有错误信息
     * @return 不可修改的错误信息列表
     */
    public List<ExtractionError> getErrors() {
        // 转换Set为List，保持兼容性
        return new ArrayList<>(errors);
    }

    /**
     * 判断是否包含错误
     * @return 是否存在任何错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 获取指定提取器的错误
     * @param extractorId 提取器ID
     * @return 该提取器的错误列表
     */
    public List<ExtractionError> getErrorsByExtractor(String extractorId) {
        return errors.stream()
                .filter(e -> extractorId != null && extractorId.equals(e.getExtractorId()))
                .collect(Collectors.toList());
    }

    public String toString() {
        return "SheetExtractionResult{" +
                "sheetIndex=" + sheetIndex +
                ", sheetName='" + sheetName + '\'' +
                ", isSuccess=" + isSuccess +
                ", results=" + results.size() + " items" +
                ", errors=" + errors.size() + " items" +
                '}';
    }
}
