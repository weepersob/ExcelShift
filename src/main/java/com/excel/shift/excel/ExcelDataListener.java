package com.excel.shift.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import com.excel.shift.config.ExcelMappingConfig;
import com.excel.shift.config.ExtractorConfig;
import com.excel.shift.util.DynamicExpressionResolver;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.util.ObjUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ExcelDataListener extends AnalysisEventListener<Map<Integer, String>> {

    ExcelMappingConfig excelMappingConfig;
    private TreeMap<Integer, Map<Integer, String>> data = new TreeMap<>();
    private DynamicExpressionResolver expressionResolver;
    private boolean isSuccess = true;
    private Integer startRow;
    private Integer endRow;

    public ExcelDataListener(ExcelMappingConfig mappingConfig, int startRow, int endRow) {
        if (mappingConfig != null) {
            this.expressionResolver = new DynamicExpressionResolver(mappingConfig);
            this.excelMappingConfig = mappingConfig;
        }
        this.startRow = startRow;
        if(ObjUtil.isNotNull(startRow)&&startRow>0)this.startRow--;
        this.endRow = endRow;
        if(ObjUtil.isNotNull(endRow)&&endRow>0)this.endRow--;
    }

    public ExcelDataListener(ExcelMappingConfig mappingConfig) {
        if (mappingConfig != null) {
            this.expressionResolver = new DynamicExpressionResolver(mappingConfig);
            this.excelMappingConfig = mappingConfig;
        }
    }

    @Override
    public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex();
//
//        boolean allStartNumeric = true;
//        boolean allEndNumeric = true;
//        int minStart = Integer.MAX_VALUE;
//        int maxEnd = Integer.MIN_VALUE;
//
//        // 一次遍历，收集 startRow 和 endRow 信息
//        for (ExtractorConfig cfg : excelMappingConfig.getAllExtractors()) {
//            String s = cfg.getStartRow();
//            if (s != null && s.trim().matches("\\d+")) {
//                int v = Integer.parseInt(s.trim());
//                minStart = Math.min(minStart, v);
//            } else {
//                allStartNumeric = false;
//            }
//
//            String e = cfg.getEndRow();
//            if (e != null && e.trim().matches("\\d+")) {
//                int v = Integer.parseInt(e.trim());
//                maxEnd = Math.max(maxEnd, v);
//            } else {
//                allEndNumeric = false;
//            }
//        }
//
//        // 如果所有 startRow 都是数字，才做开始行判断
//        if (allStartNumeric && rowIndex < minStart) {
//            return;
//        }
//        // 如果所有 endRow 都是数字，才做结束行判断
//        if (allEndNumeric && rowIndex > maxEnd) {
//            return;
//        }
        if(ObjUtil.isNotNull(startRow)&&startRow!=-1 && rowIndex <startRow)return;
        if(ObjUtil.isNotNull(endRow)&&endRow!=-1 && rowIndex > endRow)return;
//        log.error("startRow:{},endRow:{},rowIndex:{}",startRow,endRow,rowIndex);
        data.put(rowIndex, rowData);  // 经过边界过滤后，才真正处理这一行
        log.error("rowIndex:{}",rowIndex);
        log.error("rowData:{}",rowData);

        if (expressionResolver != null) {
            expressionResolver.updateDynamicRowTablePosition(rowIndex, rowData);
            expressionResolver.updateDynamicStartRowTablePosition(rowIndex, rowData);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (expressionResolver != null) this.isSuccess = expressionResolver.updateDynamicPosition();
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
        if (!isSuccess) {
            return new TreeMap<>();
        }
        return data;
    }
}
