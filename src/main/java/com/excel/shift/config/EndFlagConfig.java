package com.excel.shift.config;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 结束标志配置
 */
@Data
@Accessors(chain = true)
public class EndFlagConfig {
    /**
     * 结束标志文本
     */
    private String text;
    
    /**
     * 结束标志所在列
     */
    private String columnCell;
} 