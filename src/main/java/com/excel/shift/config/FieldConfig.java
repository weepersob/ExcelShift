package com.excel.shift.config;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 字段配置
 */
@Data
@Accessors(chain = true)
public class FieldConfig {
    /**
     * 字段顺序
     */
    private Integer order;
    
    /**
     * 对应Java字段名
     */
    private String javaFieldName;
    
    /**
     * Java字段类型
     */
    private String javaFieldType;
    
    /**
     * Excel单元格位置
     */
    private String excelCell;
    
    /**
     * 字段描述
     */
    private String description;
    
    /**
     * 提取模式
     */
    private String extractPattern;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 是否动态计算
     */
    private Boolean isDynamic;
} 