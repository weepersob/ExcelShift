package com.excel.shift.config;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public  class ExcelColumnInfo {
    private String excelFieldName;
    private String unit;
    private int order;
    private String columnCell;
    private String dataType;
}
