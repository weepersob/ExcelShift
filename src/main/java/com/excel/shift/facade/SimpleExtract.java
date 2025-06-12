package com.excel.shift.facade;

import com.excel.shift.config.response.ColumnDoubleValueResponse;
import com.excel.shift.config.response.ColumnValueResponse;

import java.io.IOException;
import java.util.List;

public interface SimpleExtract{
//    // 简单提取 用于按列的提取
    List<ColumnDoubleValueResponse> extractByColumn(int sheetIndex) throws IOException;
}
