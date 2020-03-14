package com.clc.learnplatform.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class StringUtil {

    /**
     * 使用正则表达式来匹配，然后替换成空字符
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
