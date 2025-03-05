package com.desktop.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

@Data
@AllArgsConstructor
public class ScrapperRequestDTO {
    private Path directory;
    private String url;
}
