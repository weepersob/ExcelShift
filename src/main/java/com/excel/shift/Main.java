package com.excel.shift;

import com.excel.shift.config.ExcelColumnConfig;
import com.excel.shift.config.ExcelColumnInfo;
import com.excel.shift.config.Request;
import com.excel.shift.config.RequestForColumn;
import com.excel.shift.config.response.ColumnDoubleValueResponse;
import com.excel.shift.excel.ExcelExtractor;
import com.excel.shift.result.SheetExtractionResult;
import com.excel.shift.util.JsonConfigReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        String excelPath = "D:\\JavaProject\\ExcelShift4 - clean\\src\\main\\resources\\复杂事件模板.xlsx";
        String configPath = "D:\\JavaProject\\ExcelShift4 - clean\\src\\main\\resources\\AllComplexEventsConfig.json";
        List<Class<?>> classList = new ArrayList<>();
        classList.add(ComplexEventsTemplateVO.class);
        ExcelExtractor extractor = new ExcelExtractor(excelPath, configPath,classList);
        SheetExtractionResult result = extractor.extractSheetByIndex(0);
        result.getResultList(ComplexEventsTemplateVO.class).forEach(System.out::println);



////        List<Class<?>> classList = new ArrayList<>();
////        classList.add(MudGeoOilgas.class);
////        ExcelExtractor extractor = new ExcelExtractor(excelPath, configPath, classList);
//
//
////        columnInfoList.forEach(System.out::println);
//        RequestForColumn requestForColumn = new RequestForColumn();
//        requestForColumn.setFilePath(excelPath);
//        requestForColumn.setConfigPath(configPath);
//        requestForColumn.setStartRow(3);
//        requestForColumn.setEndRow(10);
//        ExcelExtractor extractor =new ExcelExtractor(requestForColumn);
////        columnDoubleValueResponses.forEach(System.out::println);
//        extractor.extractByColumn(0).forEach(System.out::println);
//        var s= extractor.getMappingConfig().getAllExtractors();
//        System.out.println(s);
//        SheetExtractionResult sheetExtractionResult = extractor.extractSheetByIndex(0);
//        sheetExtractionResult.getResultList(MudGeoOilgas.class).forEach(System.out::println);
//        sheetExtractionResult.getErrors().forEach(System.out::println);

    }
}
