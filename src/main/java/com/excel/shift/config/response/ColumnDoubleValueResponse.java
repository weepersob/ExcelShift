package com.excel.shift.config.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ColumnDoubleValueResponse extends ColumnValueResponse<Double> {
    @Override
    public String toString() {
        return "ColumnDoubleValueResponse{" +
                "excelFieldName='" + getExcelFieldName() + '\'' +
                ", unit='" + getUnit() + '\'' +
                ", columnValues=" + getColumnValues() +
                '}';
    }
}
