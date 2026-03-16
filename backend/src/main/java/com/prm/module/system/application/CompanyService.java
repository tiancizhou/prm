package com.prm.module.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.common.exception.BizException;
import com.prm.module.system.dto.CompanyProfileDTO;
import com.prm.module.system.dto.SaveCompanyRequest;
import com.prm.module.system.entity.SysCompany;
import com.prm.module.system.mapper.SysCompanyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    public static final String DEFAULT_COMPANY_NAME = "默认公司";

    private final SysCompanyMapper companyMapper;

    public CompanyProfileDTO getCurrent() {
        return toDTO(getOrCreateCurrentCompany());
    }

    public List<SysCompany> listActiveCompanies() {
        List<SysCompany> companies = companyMapper.selectList(
                new LambdaQueryWrapper<SysCompany>()
                        .eq(SysCompany::getDeleted, 0)
                        .orderByAsc(SysCompany::getId)
        );
        if (!companies.isEmpty()) {
            return companies;
        }
        return List.of(getOrCreateCurrentCompany());
    }

    public SysCompany requireExistingCompany(Long companyId) {
        SysCompany company = companyMapper.selectById(companyId);
        if (company == null || company.getDeleted() == 1) {
            throw BizException.notFound("公司");
        }
        return company;
    }

    public Long getCurrentCompanyId() {
        return getOrCreateCurrentCompany().getId();
    }

    @Transactional
    public CompanyProfileDTO updateCurrent(SaveCompanyRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw BizException.of("公司名称不能为空");
        }

        SysCompany company = getOrCreateCurrentCompany();
        company.setName(request.getName().trim());
        company.setShortName(normalize(request.getShortName()));
        company.setContactName(normalize(request.getContactName()));
        company.setPhone(normalize(request.getPhone()));
        company.setEmail(normalize(request.getEmail()));
        company.setAddress(normalize(request.getAddress()));
        company.setDescription(normalize(request.getDescription()));
        company.setStatus("ACTIVE");
        companyMapper.updateById(company);
        return toDTO(companyMapper.selectById(company.getId()));
    }

    @Transactional
    public SysCompany getOrCreateCurrentCompany() {
        List<SysCompany> companies = companyMapper.selectList(
                new LambdaQueryWrapper<SysCompany>()
                        .eq(SysCompany::getDeleted, 0)
                        .orderByAsc(SysCompany::getId)
                        .last("LIMIT 1")
        );
        if (!companies.isEmpty()) {
            return companies.get(0);
        }

        SysCompany company = new SysCompany();
        company.setName(DEFAULT_COMPANY_NAME);
        company.setShortName(DEFAULT_COMPANY_NAME);
        company.setStatus("ACTIVE");
        company.setDeleted(0);
        companyMapper.insert(company);
        return company;
    }

    private CompanyProfileDTO toDTO(SysCompany company) {
        CompanyProfileDTO dto = new CompanyProfileDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setShortName(company.getShortName());
        dto.setContactName(company.getContactName());
        dto.setPhone(company.getPhone());
        dto.setEmail(company.getEmail());
        dto.setAddress(company.getAddress());
        dto.setDescription(company.getDescription());
        dto.setStatus(company.getStatus());
        return dto;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
