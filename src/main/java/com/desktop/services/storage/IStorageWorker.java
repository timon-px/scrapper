package com.desktop.services.storage;

import com.desktop.services.models.FileSaveModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface IStorageWorker {

    Path GetFolderPath(String initName);

    CompletableFuture<Path> SaveToFolderAsync(String content, Path folderPath, String fileName);

    CompletableFuture<String> SaveFileAsync(String url, FileSaveModel fileSaveModel, Path mainPath);

    CompletableFuture<Void> InitFoldersAsync(Collection<FileSaveModel> fileSaveModels, Path mainPath) throws IOException;

    String GetUniqueFileName(Path folderPath, String fileName);
}
