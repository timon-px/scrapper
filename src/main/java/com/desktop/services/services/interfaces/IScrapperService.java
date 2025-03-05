package com.desktop.services.services.interfaces;

import com.desktop.services.dto.ScrapperResponseDTO;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface IScrapperService {

    CompletableFuture<ScrapperResponseDTO> GetWeb(Path folderPath, String websiteUrl);
}
