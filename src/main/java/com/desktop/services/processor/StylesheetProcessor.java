package com.desktop.services.processor;

import com.desktop.services.config.constants.ScrapperConstants;
import com.desktop.services.config.enums.SaveAsEnum;
import com.desktop.services.models.FileSaveModel;
import com.desktop.services.storage.IStorageWorker;
import com.desktop.services.utils.FilesWorker;
import com.desktop.services.utils.PathHelper;
import com.desktop.services.utils.ScrapperWorker;
import com.desktop.services.utils.StylesheetWorker;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StylesheetProcessor {
    private final IStorageWorker storageWorker;
    private final Path stylesheetPath;
    private final ConcurrentHashMap<String, FileSaveModel> filesToSave;
    private final HashMap<String, String> downloadedCssFiles = new HashMap<>();
    private final CssPathResolver pathResolver;

    public StylesheetProcessor(IStorageWorker storageWorker, Path mainPath, ConcurrentHashMap<String, FileSaveModel> filesToSave) {
        this.storageWorker = storageWorker;
        this.stylesheetPath = mainPath.resolve(ScrapperConstants.STYLESHEET_FOLDER);
        this.filesToSave = filesToSave;
        this.pathResolver = new CssPathResolver();
    }

    public CompletableFuture<Void> SaveStylesheetsAsync(Document document) {
        Elements styles = ScrapperWorker.ScrapStylesheets(document);
        Elements blockStyles = ScrapperWorker.ScrapBlockStylesheets(document);
        Elements inlineStyles = ScrapperWorker.ScrapInlineStylesheets(document);
        String documentUrl = ScrapperWorker.ResolveDocumentUrl(document);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(processBlockStylesAsync(blockStyles, documentUrl));
        futures.add(processInlineStylesAsync(inlineStyles, documentUrl));
        futures.add(processExternalStylesAsync(styles));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processBlockStylesAsync(Elements styles, String documentUrl) {
        return CompletableFuture.runAsync(() -> {
            for (Element style : styles) {
                String cssContent = style.html();
                String updatedCssContent = processCssDependencies(cssContent, documentUrl, true);
                style.html(updatedCssContent);
            }
        });
    }

    private CompletableFuture<Void> processInlineStylesAsync(Elements styles, String documentUrl) {
        return CompletableFuture.runAsync(() -> {
            for (Element style : styles) {
                if (!style.hasAttr("style")) continue;
                String cssContent = style.attr("style");

                String updatedCssContent = processCssDependencies(cssContent, documentUrl, true);
                style.attr("style", updatedCssContent);
            }
        });
    }

    private CompletableFuture<Void> processExternalStylesAsync(Elements styles) {
        List<CompletableFuture<Void>> futures = styles.stream()
                .map(this::processExternalStyleAsync)
                .toList();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> processExternalStyleAsync(Element style) {
        String href = style.absUrl("href");
        return CompletableFuture.supplyAsync(() -> processStylesheetFile(href))
                .thenAccept(cssName -> {
                    if (cssName != null) {
                        style.attr("href", pathResolver.resolveExternalClear(cssName, SaveAsEnum.STYLESHEET, "."));
                    }
                });
    }

    private String processStylesheetFile(String cssUrl) {
        if (downloadedCssFiles.containsKey(cssUrl)) {
            return downloadedCssFiles.get(cssUrl);
        }

        String cssName = Paths.get(cssUrl).getFileName().toString();
        String cleanCssName = ScrapperWorker.CleanName(cssName);
        String uniqueCssName = storageWorker.GetUniqueFileName(stylesheetPath, cleanCssName);

        try {
            String cssContent = fetchCssContent(cssUrl);
            String updatedCss = processCssDependencies(cssContent, cssUrl, false);

            storageWorker.SaveToFolderAsync(updatedCss, stylesheetPath, uniqueCssName)
                    .thenAccept(path -> downloadedCssFiles.put(cssUrl, uniqueCssName)).join();
            return uniqueCssName;
        } catch (IOException e) {
            return null;
        }
    }

    private String processCssDependencies(String cssContent, String baseUrl, boolean isInline) {
        return StylesheetWorker.ProcessDependencies(cssContent,
                importUrl -> processImportUrl(importUrl, baseUrl, isInline),
                externalUrl -> processExternalUrl(externalUrl, baseUrl, isInline));
    }

    private String processImportUrl(String importUrl, String baseUrl, boolean isInline) {
        String absoluteUrl = ScrapperWorker.ResolveAbsoluteUrl(baseUrl, importUrl);
        SaveAsEnum fileType = FilesWorker.GetFileType(absoluteUrl);

        if (fileType != SaveAsEnum.STYLESHEET) {
            return processExternalUrl(importUrl, baseUrl, isInline);
        }

        String cssName = processStylesheetFile(absoluteUrl);
        return cssName != null ? pathResolver.resolveImport(cssName) : importUrl;
    }

    private String processExternalUrl(String url, String baseUrl, boolean isInline) {
        String absoluteUrl = ScrapperWorker.ResolveAbsoluteUrl(baseUrl, url);
        if (downloadedCssFiles.containsKey(absoluteUrl)) {
            return pathResolver.resolve(isInline, downloadedCssFiles.get(absoluteUrl), FilesWorker.GetFileType(absoluteUrl));
        }

        FileSaveModel file = FilesWorker.SetFilesToSave(absoluteUrl, filesToSave);
        return pathResolver.resolve(isInline, file.getUniqueName(), file.getFileType());
    }

    private String fetchCssContent(String cssUrl) throws IOException {
        Connection.Response response = Jsoup.connect(cssUrl).ignoreContentType(true).execute();
        return response.body();
    }

    // Inner class to handle path resolution
    private static class CssPathResolver {
        private static final String URL_PATTERN = "url('%s')";
        private static final String IMPORT_PATTERN = "@import url('%s')";

        public String resolve(boolean isInline, String fileName, SaveAsEnum fileType) {
            if (fileType == SaveAsEnum.STYLESHEET) {
                return resolveImport(fileName);
            }
            return resolveExternal(fileName, fileType, isInline ? "." : "..");
        }

        private String resolveExternal(String fileName, SaveAsEnum fileType, String baseFolder) {
            return String.format(URL_PATTERN, resolveExternalClear(fileName, fileType, baseFolder));
        }

        public String resolveImport(String fileName) {
            return String.format(IMPORT_PATTERN, "./" + fileName);
        }

        private String resolveExternalClear(String fileName, SaveAsEnum fileType, String baseFolder) {
            return PathHelper.GetPath(fileType, baseFolder).resolve(fileName).toString();
        }
    }
}

