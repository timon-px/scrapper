package com.desktop.services.processor;

import com.desktop.services.models.FileSaveModel;
import com.desktop.services.storage.IStorageWorker;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class FilesProcessor {
    private final IStorageWorker storageWorker;
    private final Path mainPath;

    public CompletableFuture<Void> SaveFilesAsync(ConcurrentHashMap<String, FileSaveModel> filesToSave, DoubleProperty progress) {
        if (filesToSave.isEmpty()) return CompletableFuture.completedFuture(null);
        List<CompletableFuture<String>> downloadFutures = new ArrayList<>();

        try {
            storageWorker.InitFoldersAsync(filesToSave.values(), mainPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        updateProgress(progress, 0.05);

        int totalFiles = filesToSave.size();
        AtomicInteger completedFiles = new AtomicInteger(0);
        double baseProgress = progress.get();
        double downloadWeight = 1 - baseProgress;

        // Iterate over the map and start async downloads
        for (var entry : filesToSave.entrySet()) {
            String url = entry.getKey();
            FileSaveModel fileModel = entry.getValue();

            CompletableFuture<String> future = storageWorker.SaveFileAsync(url, fileModel, mainPath);
            future.thenRun(() -> {
                int completed = completedFiles.incrementAndGet();
                double fileProgress = (double) completed / totalFiles * downloadWeight;
                updateProgress(progress, baseProgress + fileProgress);
            }).exceptionally(throwable -> {
                System.err.println("Download failed: " + throwable.getMessage());
                return null;
            });

            downloadFutures.add(future);
        }

        // Wait for all downloads to complete
        return CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0]))
                .exceptionally(throwable -> {
                    System.err.println("Error during downloads: " + throwable.getMessage());
                    return null;
                });
    }

    private void updateProgress(DoubleProperty progress, double addValue) {
        double newValue = progress.get() + addValue;
        Platform.runLater(() -> progress.set(Math.min(newValue, 1.0)));
    }
}