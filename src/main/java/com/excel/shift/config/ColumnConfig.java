package com.excel.shift.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 列配置
 */
@Data
@Accessors(chain = true)
public class ColumnConfig {
    /**
     * 列顺序
     */
    private Integer order;
    
    /**
     * 对应Java字段名
     */
    private String javaFieldName;
    
    /**
     * Excel列标识
     */
    private String columnCell;

    /**
     * Excel行标识（用于垂直列表类型）
     */
    private String rowCell;
    
    /**
     * Java字段类型
     */
    private String javaFieldType;
    
    /**
     * 列描述
     */
    private String description;
    
    /**
     * 单元格值的单位
     */
    private String unit;
    
    /**
     * 是否是合并单元格
     */
    private Boolean isMergeType;
    
    /**
     * 是否动态
     */
    private Boolean isDynamic;

    /**
     * 提取模式
     */
    private String extractPattern;
    
    /**
     * 行组索引，用于GROUP_LIST类型
     * 表示从行组中的第几行取值（从1开始）
     */
    private Integer groupRowIndex;

    /**
     * 备选列列表
     * 当主列无法提供有效数据时，尝试从这些备选列获取数据
     */
    private List<String> alternativeColumnCell;

    /**
     * 备选策略
     * 例如：APPEND_UP - 将备选列内容向上合并到最近的非备选字段
     */
    private String alternativeStrategy;
} 