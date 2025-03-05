package com.desktop.services.utils;

import com.desktop.services.config.constants.RegexConstants;

import java.util.function.Function;
import java.util.regex.Matcher;

public class StylesheetWorker {
    public static String ProcessDependencies(String cssContent, Function<String, String> processImports, Function<String, String> processExternal) {
        String processedImports = processImports(cssContent, processImports);
        return processExternalUrls(processedImports, processExternal);
    }

    private static String processImports(String cssContent, Function<String, String> processImports) {
        return RegexWorker.
                ProcessUrlsByRegex(cssContent,
                        RegexConstants.IMPORT_CSS_URL_REGEX,
                        (matcher -> processImportsMatcher(matcher, processImports)));
    }

    private static String processExternalUrls(String cssContent, Function<String, String> processExternal) {
        return RegexWorker.
                ProcessUrlsByRegex(cssContent,
                        RegexConstants.STYLESHEET_URL_REGEX,
                        (matcher -> processExternalUrlsMatcher(matcher, processExternal)));
    }

    private static String processImportsMatcher(Matcher matcher, Function<String, String> processImports) {
        String url = matcher.group(1);
        return processStylesheetMatchers(url, processImports);
    }

    private static String processExternalUrlsMatcher(Matcher matcher, Function<String, String> processExternal) {
        String url = matcher.group(2);
        return processStylesheetMatchers(url, processExternal);
    }

    private static String processStylesheetMatchers(String str, Function<String, String> processFunc) {
        if (str == null || str.isEmpty()) return null;

        try {
            return processFunc.apply(str);
        } catch (Exception e) {
            return null;
        }
    }
}
