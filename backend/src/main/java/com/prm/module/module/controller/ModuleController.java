package com.prm.module.module.controller;

import com.prm.common.result.R;
import com.prm.module.module.application.ModuleService;
import com.prm.module.module.dto.ModuleDTO;
import com.prm.module.module.dto.SaveModuleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    public R<List<ModuleDTO>> listTree(@PathVariable Long projectId) {
        return R.ok(moduleService.listTree(projectId));
    }

    @PostMapping
    public R<ModuleDTO> create(@PathVariable Long projectId,
                               @RequestBody @Valid SaveModuleRequest req) {
        return R.ok(moduleService.create(projectId, req));
    }

    @PutMapping("/{id}")
    public R<ModuleDTO> update(@PathVariable Long projectId,
                               @PathVariable Long id,
                               @RequestBody @Valid SaveModuleRequest req) {
        return R.ok(moduleService.update(projectId, id, req));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long projectId,
                          @PathVariable Long id) {
        moduleService.delete(projectId, id);
        return R.ok();
    }
}
