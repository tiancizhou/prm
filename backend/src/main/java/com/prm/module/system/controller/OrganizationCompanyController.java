package com.prm.module.system.controller;

import com.prm.common.result.R;
import com.prm.module.system.application.CompanyService;
import com.prm.module.system.dto.CompanyProfileDTO;
import com.prm.module.system.dto.SaveCompanyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公司信息")
@RestController
@RequestMapping("/api/organization/company")
@RequiredArgsConstructor
public class OrganizationCompanyController {

    private final CompanyService companyService;

    @Operation(summary = "当前公司资料")
    @GetMapping
    public R<CompanyProfileDTO> getCurrent() {
        return R.ok(companyService.getCurrent());
    }

    @Operation(summary = "更新当前公司资料")
    @PutMapping
    public R<CompanyProfileDTO> update(@Valid @RequestBody SaveCompanyRequest request) {
        return R.ok(companyService.updateCurrent(request));
    }
}
