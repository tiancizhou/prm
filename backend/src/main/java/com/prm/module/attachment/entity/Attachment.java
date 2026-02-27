package com.prm.module.attachment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_attachment")
public class Attachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizType;
    private Long bizId;
    private String filename;
    private String filepath;
    private Long fileSize;
    private String mimeType;
    private Long uploaderId;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
}
