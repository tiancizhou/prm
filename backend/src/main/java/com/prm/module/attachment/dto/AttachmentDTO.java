package com.prm.module.attachment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentDTO {
    private Long id;
    private String bizType;
    private Long bizId;
    private String filename;
    private String filepath;
    private Long fileSize;
    private String mimeType;
    private Long uploaderId;
    private LocalDateTime createdAt;
}
