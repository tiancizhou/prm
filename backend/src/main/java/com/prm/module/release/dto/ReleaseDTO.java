package com.prm.module.release.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReleaseDTO {
    private Long id;
    private Long projectId;
    private Long sprintId;
    private String version;
    private String description;
    private String status;
    private LocalDate releaseDate;
    private Long releasedBy;
    private String releasedByName;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
}
