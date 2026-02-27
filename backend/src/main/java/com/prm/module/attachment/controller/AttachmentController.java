package com.prm.module.attachment.controller;

import com.prm.common.result.R;
import com.prm.module.attachment.application.AttachmentService;
import com.prm.module.attachment.dto.AttachmentDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "附件")
@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(summary = "上传需求附件")
    @PostMapping("/{requirementId}/attachments")
    public R<AttachmentDTO> upload(
            @PathVariable Long requirementId,
            @RequestParam("file") MultipartFile file) {
        return R.ok(attachmentService.uploadForRequirement(requirementId, file));
    }

    @Operation(summary = "需求附件列表")
    @GetMapping("/{requirementId}/attachments")
    public R<List<AttachmentDTO>> list(@PathVariable Long requirementId) {
        return R.ok(attachmentService.listByRequirement(requirementId));
    }

    @Operation(summary = "删除需求附件")
    @DeleteMapping("/{requirementId}/attachments/{attachmentId}")
    public R<Void> delete(@PathVariable Long requirementId, @PathVariable Long attachmentId) {
        attachmentService.delete(requirementId, attachmentId);
        return R.ok();
    }

    @Operation(summary = "下载需求附件")
    @GetMapping("/{requirementId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long requirementId,
            @PathVariable Long attachmentId) throws UnsupportedEncodingException {
        var result = attachmentService.getFileForDownload(requirementId, attachmentId);
        Resource resource = new FileSystemResource(result.path.toFile());
        String encodedFilename = URLEncoder.encode(result.filename, StandardCharsets.UTF_8.toString())
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }
}
