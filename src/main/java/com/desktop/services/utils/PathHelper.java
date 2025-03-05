package com.desktop.services.utils;

import com.desktop.services.config.constants.ScrapperConstants;
import com.desktop.services.config.enums.SaveAsEnum;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class PathHelper {
    public static Path GetPath(SaveAsEnum type) {
        return GetPath(type, null);
    }

    public static Path GetPath(SaveAsEnum type, String initFolder) {
        if (!MAP.containsKey(type)) Paths.get(initFolder, ScrapperConstants.MEDIA_FOLDER);

        String _initFolder = ".";
        if (initFolder != null) _initFolder = initFolder;

        String folder = MAP.get(type);
        return Paths.get(_initFolder, folder);
    }

    private static final Map<SaveAsEnum, String> MAP = Map.of(
            SaveAsEnum.ASSET, ScrapperConstants.MEDIA_FOLDER,
            SaveAsEnum.IMAGES, ScrapperConstants.MEDIA_FOLDER,
            SaveAsEnum.STYLESHEET, ScrapperConstants.STYLESHEET_FOLDER,
            SaveAsEnum.FONT, ScrapperConstants.FONT_FOLDER,
            SaveAsEnum.SCRIPT, ScrapperConstants.SCRIPTS_FOLDER,
            SaveAsEnum.VIDEO, ScrapperConstants.VIDEO_FOLDER,
            SaveAsEnum.AUDIO, ScrapperConstants.AUDIO_FOLDER);
}
