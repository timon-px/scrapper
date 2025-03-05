package com.desktop.services.utils;

import com.desktop.services.config.enums.SaveAsEnum;
import com.desktop.services.models.FileSaveModel;
import org.apache.commons.io.FilenameUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class FilesWorker {
    public static FileSaveModel SetFilesToSave(String absoluteUrl, ConcurrentHashMap<String, FileSaveModel> filesToSave) {
//        if (filesToSave.containsKey(absoluteUrl)) return filesToSave.get(absoluteUrl);
//
//        String fileName = FilenameUtils.getName(absoluteUrl);
//
//        String cleanName = ScrapperWorker.CleanName(fileName);
//        String uniqueName = getUniqueFileName(cleanName, filesToSave.values());
//
//        // Change file url to clear, for getting proper contentType
//        String clearAbsoluteUrl = Paths.get(absoluteUrl).getParent().resolve(uniqueName).toString();
//
//        SaveAsEnum importType = GetFileType(clearAbsoluteUrl);
//        FileSaveModel fileSaveModel = new FileSaveModel(uniqueName, importType);
//
//        filesToSave.put(absoluteUrl, fileSaveModel);
//
//        return fileSaveModel;

        return filesToSave.computeIfAbsent(absoluteUrl, url -> {
            String fileName = FilenameUtils.getName(url);
            String cleanName = ScrapperWorker.CleanName(fileName);
            String uniqueName = getUniqueName(cleanName, filesToSave.values());
            SaveAsEnum fileType = GetFileType(url);
            return new FileSaveModel(uniqueName, fileType);
        });
    }

    public static SaveAsEnum GetFileType(String absoluteUrl) {
        try {
            String contentType = Files.probeContentType(Paths.get(absoluteUrl));
            return ScrapperWorker.GetSaveAsFromContentType(contentType);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return SaveAsEnum.ASSET;
        }
    }

    private static String getUniqueName(String name, Collection<FileSaveModel> names) {
        String uniqueName = URLDecoder.decode(name, StandardCharsets.UTF_8);

        if (!isNameUnique(uniqueName, names)) {
            int counter = 1;

            String extension = FilenameUtils.getExtension(name);
            String nameWithoutExtension = FilenameUtils.getBaseName(name);

            do {
                uniqueName = nameWithoutExtension + "_" + counter + "." + extension;
                counter++;
            } while (!isNameUnique(uniqueName, names));
        }

        return uniqueName;
    }

    private static boolean isNameUnique(String name, Collection<FileSaveModel> names) {
        for (FileSaveModel fileSaveModel : names) {
            if (fileSaveModel.getUniqueName().equals(name)) return false;
        }
        return true;
    }
}