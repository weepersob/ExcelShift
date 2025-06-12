package com.excel.shift.util;

/**
 * Excel坐标转换工具类
 * 用于Excel坐标格式（如A1, B2, AA3）和行列索引之间的转换
 */
public class ExcelCoordConverter {
    
    /**
     * 将Excel列名转换为列索引（0-based）
     * 例如：A -> 0, B -> 1, Z -> 25, AA -> 26
     */
    public static int columnNameToIndex(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            throw new IllegalArgumentException("excel列名不能为空");
        }
        
        int result = 0;
        for (char c : columnName.toUpperCase().toCharArray()) {
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("无效的列名: " + columnName);
            }
            result = result * 26 + (c - 'A' + 1);
        }
        return result - 1;
    }

    /**
     * 将列索引转换为Excel列名
     * 例如：0 -> A, 1 -> B, 25 -> Z, 26 -> AA
     */
    public static String indexToColumnName(int columnIndex) {
        if (columnIndex < 0) {
            throw new IllegalArgumentException("列索引不能为负数: " + columnIndex);
        }
        
        StringBuilder result = new StringBuilder();
        columnIndex++; // 转为1-based以便计算

        while (columnIndex > 0) {
            columnIndex--;
            result.insert(0, (char) ('A' + columnIndex % 26));
            columnIndex /= 26;
        }

        return result.toString();
    }

    /**
     * 将Excel坐标转换为行列索引对象
     * 例如：A1 -> {row: 0, column: 0}, B2 -> {row: 1, column: 1}
     */
    public static CellPosition excelCoordToPosition(String excelCoord) {
        if (excelCoord == null || excelCoord.isEmpty()) {
            throw new IllegalArgumentException("Excel坐标不能为空");
        }
        
        // 分离列名和行号
        String columnName = excelCoord.replaceAll("\\d", "");
        String rowStr = excelCoord.replaceAll("[A-Za-z]", "");
        
        if (columnName.isEmpty() || rowStr.isEmpty()) {
            throw new IllegalArgumentException("无效的Excel坐标: " + excelCoord);
        }
        
        try {
            int rowNumber = Integer.parseInt(rowStr);
            int columnIndex = columnNameToIndex(columnName);
            
            if (rowNumber <= 0) {
                throw new IllegalArgumentException("行号必须大于0: " + rowNumber);
            }
            
            return new CellPosition(rowNumber - 1, columnIndex);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的行号: " + excelCoord);
        }
    }

    /**
     * 将行列索引转换为Excel坐标
     * 例如：{row: 0, column: 0} -> A1, {row: 1, column: 1} -> B2
     */
    public static String positionToExcelCoord(int rowIndex, int columnIndex) {
        if (rowIndex < 0) {
            throw new IllegalArgumentException("行索引不能为负数: " + rowIndex);
        }
        if (columnIndex < 0) {
            throw new IllegalArgumentException("列索引不能为负数: " + columnIndex);
        }
        
        return indexToColumnName(columnIndex) + (rowIndex + 1);
    }

    /**
     * 将行列索引转换为Excel坐标
     */
    public static String positionToExcelCoord(CellPosition position) {
        if (position == null) {
            throw new IllegalArgumentException("位置对象不能为空");
        }
        return positionToExcelCoord(position.row, position.column);
    }

    /**
     * 单元格位置类，用于存储行列索引
     */
    public static class CellPosition {
        public final int row;    // 行索引（0-based）
        public final int column; // 列索引（0-based）

        public CellPosition(int row, int column) {
            if (row < 0) {
                throw new IllegalArgumentException("行索引不能为负数: " + row);
            }
            if (column < 0) {
                throw new IllegalArgumentException("列索引不能为负数: " + column);
            }
            this.row = row;
            this.column = column;
        }

        @Override
        public String toString() {
            return String.format("Row: %d, Column: %d", row, column);
        }
    }
} 