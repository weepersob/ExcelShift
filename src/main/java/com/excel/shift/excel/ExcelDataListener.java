package com.excel.shift.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import com.excel.shift.config.ExcelMappingConfig;
import com.excel.shift.util.DynamicExpressionResolver;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class ExcelDataListener extends AnalysisEventListener<Map<Integer, String>> {
    
    private TreeMap<Integer, Map<Integer, String>> data = new TreeMap<>();
    private DynamicExpressionResolver expressionResolver;
    private boolean isSuccess= true;
    
    public ExcelDataListener(ExcelMappingConfig mappingConfig) {
        if (mappingConfig != null) {
            this.expressionResolver = new DynamicExpressionResolver(mappingConfig);
        }
    }
    
    @Override
    public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex();
        data.put(rowIndex, rowData);
        if(expressionResolver!=null){
            expressionResolver.updateDynamicRowTablePosition(rowIndex, rowData);
            expressionResolver.updateDynamicStartRowTablePosition(rowIndex, rowData);
        }

    }
    
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if(expressionResolver!=null) this.isSuccess=expressionResolver.updateDynamicPosition();
    }
    
    /**
     * 检查Sheet是否符合配置要求
     * 从配置中选择几个关键字段进行检查
     */
    
    /**
     * 获取单元格值
     */
    private String getCellValue(int row, int col) {
        Map<Integer, String> rowData = data.get(row);
        if (rowData == null) {
            return null;
        }
        return rowData.get(col);
    }
    
    public Map<Integer, Map<Integer, String>> getData() {
        if(!isSuccess){
            return new TreeMap<>();
        }
        return data;
    }
}