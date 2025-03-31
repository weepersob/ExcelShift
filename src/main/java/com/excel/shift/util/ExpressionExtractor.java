package com.excel.shift.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionExtractor {

    // 提取字符串中 ${} 中的内容
    public static String extractExpression(String input) {
        if(input == null) return null;
        // 正则表达式：提取 ${} 中的内容
        String regex = "\\$\\{(.*?)\\}";

        // 创建匹配器
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // 如果匹配成功，返回提取的内容
        if (matcher.find()) {
            return matcher.group(1);  // 返回 ${} 中的表达式内容
        } else {
            return null;  // 如果没有匹配到，返回 null
        }
    }

    // 替换字符串中的 ${} 中的表达式
    public static String replaceExpression(String input, String newValue) {
        // 正则表达式：匹配所有 ${} 中的内容
        String regex = "\\$\\{(.*?)\\}";

        // 使用 replaceAll 方法替换 ${} 中的表达式为新的值
        return input.replaceAll(regex, Matcher.quoteReplacement(newValue));
    }
}
