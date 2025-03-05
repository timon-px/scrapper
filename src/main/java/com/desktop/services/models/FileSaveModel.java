package com.desktop.services.models;

import com.desktop.services.config.enums.SaveAsEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileSaveModel {
    private String uniqueName;
    private SaveAsEnum fileType;
}
