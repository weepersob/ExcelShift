package com.excel.shift.result;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel提取结果
 * 包含整个Excel文件的所有提取结果
 */
@Slf4j
public class ExtractionResult {
    
    // 按sheet索引存储每个sheet的提取结果 
    private final Map<Integer, SheetExtractionResult> sheetResults = new HashMap<>();

    private int totalSheetCount = 0;
    
    public ExtractionResult(int totalSheetCount) {
        this.totalSheetCount = totalSheetCount;
    }
    
    /**
     * 添加单个sheet的提取结果
     * @param sheetIndex sheet索引
     * @param sheetResult sheet提取结果
     */
    public void addSheetResult(int sheetIndex, SheetExtractionResult sheetResult) {
        sheetResults.put(sheetIndex, sheetResult);
    }
    
    /**
     * 获取指定sheet的提取结果
     * @param sheetIndex sheet索引
     * @return sheet提取结果
     */
    public SheetExtractionResult getSheetResult(int sheetIndex) {
        return sheetResults.get(sheetIndex);
    }
    
    /**
     * 获取所有sheet的提取结果
     * @return 所有sheet的提取结果
     */
    public Map<Integer, SheetExtractionResult> getAllSheetResults() {
        return sheetResults;
    }
    
    /**
     * 获取所有成功的sheet提取结果
     * @return 所有成功的sheet提取结果
     */
    public List<SheetExtractionResult> getSuccessfulSheetResults() {
        return sheetResults.values().stream()
                .filter(SheetExtractionResult::isSuccess)
                .collect(Collectors.toList());
    }
    
    /**
     * 从指定sheet中获取指定类型的提取结果
     * @param sheetIndex sheet索引
     * @param clazz 目标类型
     * @param <T> 返回类型
     * @return 提取结果，已转换为指定类型
     */
    @SuppressWarnings("unchecked")
    public <T> T getResult(int sheetIndex, Class<T> clazz) {
        SheetExtractionResult sheetResult = sheetResults.get(sheetIndex);
        if (sheetResult == null) {
            return null;
        }
        return sheetResult.getResult(clazz);
    }
    
    /**
     * 从指定sheet中获取指定类型的提取结果列表
     * 当提取的结果本身就是列表时使用此方法
     * 如果sheet不存在或结果不是列表类型，则返回空列表
     * @param sheetIndex sheet索引
     * @param elementClass 列表元素类型
     * @param <T> 列表元素类型
     * @return 提取结果列表，永不为null
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getResultList(int sheetIndex, Class<T> elementClass) {
        SheetExtractionResult sheetResult = sheetResults.get(sheetIndex);
        if (sheetResult == null) {
            // 返回空列表而不是null
            return new ArrayList<>();
        }
        return sheetResult.getResultList(elementClass);
    }
    
    /**
     * 是否包含指定sheet的提取结果
     * @param sheetIndex sheet索引
     * @return 是否包含
     */
    public boolean containsSheet(int sheetIndex) {
        return sheetResults.containsKey(sheetIndex);
    }
    
    /**
     * 是否包含指定sheet中的指定类型结果
     * @param sheetIndex sheet索引
     * @param clazz 目标类型
     * @return 是否包含
     */
    public boolean containsResult(int sheetIndex, Class<?> clazz) {
        SheetExtractionResult sheetResult = sheetResults.get(sheetIndex);
        if (sheetResult == null) {
            return false;
        }
        return sheetResult.containsResult(clazz);
    }
    
    /**
     * 获取结果数量
     * @return 提取的成功的sheet数量
     */
    public int size() {
        return sheetResults.size();
    }
    
    /**
     * 是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        return sheetResults.isEmpty();
    }
    
    /**
     * 清空结果
     */
    public void clear() {
        sheetResults.clear();
    }
    
    /**
     * 判断整个提取过程是否完全成功
     * @return 如果所有处理的sheet都成功，则返回true
     */
    public boolean isAllSuccess() {
        if (sheetResults.isEmpty()) {
            return false;
        }
        return sheetResults.values().stream().allMatch(SheetExtractionResult::isSuccess);
    }
    
    /**
     * 获取所有sheet中的所有错误
     * @return 所有错误的列表
     */
    public List<ExtractionError> getAllErrors() {
        List<ExtractionError> allErrors = new ArrayList<>();
        sheetResults.values().forEach(sheet -> allErrors.addAll(sheet.getErrors()));
        return allErrors;
    }
    
    /**
     * 获取指定提取器的所有错误
     * @param extractorId 提取器ID
     * @return 该提取器的错误列表
     */
    public List<ExtractionError> getErrorsByExtractor(String extractorId) {
        return getAllErrors().stream()
                .filter(e -> extractorId != null && extractorId.equals(e.getExtractorId()))
                .collect(Collectors.toList());
    }
    
    /**
     * 判断是否存在任何错误
     * @return 是否有任何错误
     */
    public boolean hasErrors() {
        return sheetResults.values().stream().anyMatch(SheetExtractionResult::hasErrors);
    }
    
    /**
     * 获取成功比例
     * @return 成功sheet数量 / 总sheet数量
     */
    public double getSuccessRatio() {
        if (totalSheetCount == 0) {
            return 0.0;
        }
        return (double) getSuccessfulSheetResults().size() / totalSheetCount;
    }
    
    @Override
    public String toString() {
        int successCount = getSuccessfulSheetResults().size();
        return "ExtractionResult{" +
                "totalSheets=" + totalSheetCount +
                ", processedSheets=" + sheetResults.size() +
                ", successfulSheets=" + successCount +
                ", successRatio=" + String.format("%.2f", getSuccessRatio()) +
                ", errors=" + getAllErrors().size() +
                '}';
    }
} 