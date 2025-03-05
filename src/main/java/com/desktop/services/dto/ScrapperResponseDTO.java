package com.desktop.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ScrapperResponseDTO {
    private boolean success;
    private String message;
}
