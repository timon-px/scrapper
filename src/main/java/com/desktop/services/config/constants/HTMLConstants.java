package com.desktop.services.config.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class HTMLConstants {
    @Getter
    private static final List<String> simpleSaveAttr = Arrays.asList("src", "href", "poster", "content", "data-lazy-src");
    @Getter
    private static final List<String> complexSaveAttr = Arrays.asList("srcset", "data-lazy-srcset");
}
