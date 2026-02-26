package com.prm.module.project.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String status;
    private String visibility;
    private Long ownerId;
    private String ownerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private Integer memberCount;
    /** 当前登录用户是否有编辑权限 */
    private Boolean canEdit;
}
