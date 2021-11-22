package org.jdbc.test;

import java.util.Properties;

import static java.lang.String.format;

public class Template {
    public static String replace(String template, Properties values) {
        String result = template;
        for (String key : values.stringPropertyNames()) {
            String value = values.getProperty(key);
            result = result.replace(format("${%s}", key), value);
        }
        return result;
    }

    public static String replaceParameters(String sql, Object[] args) {
        String result = sql;
        for (Object arg : args) {
            String replacement = arg instanceof String ? "'" + arg + "'" : "" + arg;
            result = result.replaceFirst("\\?", replacement);
        }
        return result;
    }
}
