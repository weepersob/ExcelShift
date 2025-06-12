package com.excel.shift.util;

import com.alibaba.fastjson.JSON;
import com.excel.shift.config.ExcelColumnConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JsonConfigReader {

    public static ExcelColumnConfig readConfig(String configPath) throws IOException {
        InputStream input = new FileInputStream(configPath);
        try (input) {
            return JSON.parseObject(input, ExcelColumnConfig.class);
        }
    }
}
