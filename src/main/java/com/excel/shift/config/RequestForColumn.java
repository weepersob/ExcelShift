package com.excel.shift.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
@Data
@Accessors(chain = true)
public class RequestForColumn {
    // excel文件路径
    private String filePath;
    // 解析开始行
    private Integer startRow;

    // 解析结束行
    private Integer endRow;

    // 配置文件路径
    private String configPath;

    // 解析的excel表头列信息
    private List<ExcelColumnInfo> excelColumnInfoList;
}
