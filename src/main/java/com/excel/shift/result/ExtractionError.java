package com.excel.shift.result;

import java.util.Date;

/**
 * 提取过程中的错误信息类
 */
public class ExtractionError {
    // 错误消息
    private String message;
    // 原始异常
    private Exception exception;
    // 发生错误的提取器ID
    private String extractorId;
    // 错误发生时间
    private Date timestamp;
    // 行号（可选）
    private Integer rowNumber;
    // 列号或列名（可选）
    private String columnInfo;
    
    public ExtractionError(String message, Exception exception, String extractorId) {
        this.message = message;
        this.exception = exception;
        this.extractorId = extractorId;
        this.timestamp = new Date();
    }
    
    public ExtractionError(String message, Exception exception, String extractorId, 
                          Integer rowNumber, String columnInfo) {
        this(message, exception, extractorId);
        this.rowNumber = rowNumber;
        this.columnInfo = columnInfo;
    }
    
    // Getters
    public String getMessage() {
        return message;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public String getExtractorId() {
        return extractorId;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public Integer getRowNumber() {
        return rowNumber;
    }
    
    public String getColumnInfo() {
        return columnInfo;
    }
    
    /**
     * 获取完整的错误信息
     */
    public String getFullErrorMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("ERROR: ").append(message);
        
        if (extractorId != null) {
            sb.append(" (提取器ID: ").append(extractorId).append(")");
        }
        
        if (rowNumber != null) {
            sb.append(" 行: ").append(rowNumber);
        }
        
        if (columnInfo != null) {
            sb.append(" 列: ").append(columnInfo);
        }
        
        if (exception != null) {
            sb.append("\n原因: ").append(exception.getMessage());
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getFullErrorMessage();
    }
} 