package com.excel.shift.config.response;

import lombok.Data;

import java.util.List;

@Data
public class ColumnValueResponse<T> {

    /**
     * 对应Java字段名
     */
    private String excelFieldName;

    /**
     * 单位
     */
    private String unit;
    /**
     * 字段值列表
     */

    private List<T> columnValues;
}
