package com.excel.shift.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Request {
    // 文件路径
    private String filePath;
    // 解析开始行
    private Integer startRow;

    // 解析结束行
    private Integer endRow;

    // 类信息
    private List<Class<?>> classInfoList;

    // 需要解析的列信息
    private List<ColumnInfo> columnInfoList;

    // 解析的excel表头列信息
    private List<ExcelColumnInfo> excelColumnInfoList;


    @Data
    public static class ColumnInfo {
        // 列名
        private String columnName;

        // 列单位
        private String columnName_unit;

        // 列号
        private String columnSeq;
    }

}
