package com.excel.shift.config;

import lombok.Data;

import java.util.List;

@Data
public class ExcelColumnConfig {
    private List<ExcelColumnInfo> columns;
}
