package com.prm.module.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.common.exception.BizException;
import com.prm.module.system.dto.DepartmentDetailDTO;
import com.prm.module.system.dto.DepartmentTreeDTO;
import com.prm.module.system.dto.SaveDepartmentRequest;
import com.prm.module.system.entity.SysCompany;
import com.prm.module.system.entity.SysDepartment;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysDepartmentMapper;
import com.prm.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private static final String DEFAULT_COMPANY_NAME = "默认公司";

    private final SysDepartmentMapper departmentMapper;
    private final SysUserMapper userMapper;

    @Autowired(required = false)
    private CompanyService companyService;

    public List<DepartmentTreeDTO> listTree() {
        List<SysDepartment> departments = departmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getDeleted, 0)
                        .orderByAsc(SysDepartment::getCompanyId, SysDepartment::getSortOrder, SysDepartment::getId)
        );

        Map<Long, DepartmentTreeDTO> companyNodeMap = new LinkedHashMap<>();
        for (SysCompany company : listCompaniesForTree()) {
            companyNodeMap.put(company.getId(), toCompanyNode(company));
        }

        Map<Long, DepartmentTreeDTO> nodeMap = new LinkedHashMap<>();
        for (SysDepartment department : departments) {
            DepartmentTreeDTO dto = toTreeDTO(department);
            nodeMap.put(dto.getId(), dto);
        }

        for (SysDepartment department : departments) {
            DepartmentTreeDTO current = nodeMap.get(department.getId());
            if (department.getParentId() == null || !nodeMap.containsKey(department.getParentId())) {
                Long companyId = resolveDepartmentCompanyId(department);
                DepartmentTreeDTO companyNode = companyNodeMap.computeIfAbsent(companyId, this::buildFallbackCompanyNode);
                companyNode.getChildren().add(current);
            } else {
                nodeMap.get(department.getParentId()).getChildren().add(current);
            }
        }

        List<DepartmentTreeDTO> roots = new ArrayList<>(companyNodeMap.values());
        roots.forEach(this::sortChildrenRecursively);
        return roots;
    }

    public DepartmentDetailDTO getById(Long id) {
        SysDepartment department = requireDepartment(id);
        List<SysDepartment> children = departmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getDeleted, 0)
                        .eq(SysDepartment::getParentId, id)
                        .orderByAsc(SysDepartment::getSortOrder, SysDepartment::getId)
        );
        long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .eq(SysUser::getDepartmentId, id)
        );

        DepartmentDetailDTO detail = new DepartmentDetailDTO();
        detail.setId(department.getId());
        detail.setCompanyId(resolveDepartmentCompanyId(department));
        detail.setName(department.getName());
        detail.setParentId(department.getParentId());
        detail.setSortOrder(department.getSortOrder());
        detail.setStatus(department.getStatus());
        detail.setUserCount(userCount);
        detail.setChildren(children.stream().map(this::toTreeDTO).toList());
        return detail;
    }

    @Transactional
    public DepartmentDetailDTO create(SaveDepartmentRequest request) {
        validateParent(request.getParentId(), null);
        Long companyId = resolveCompanyId(request.getCompanyId(), request.getParentId(), null);
        ensureNameUnique(request.getName(), companyId, request.getParentId(), null);

        SysDepartment department = new SysDepartment();
        department.setName(request.getName().trim());
        department.setCompanyId(companyId);
        department.setParentId(request.getParentId());
        department.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        department.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ACTIVE");
        department.setDeleted(0);
        departmentMapper.insert(department);
        return getById(department.getId());
    }

    @Transactional
    public DepartmentDetailDTO update(Long id, SaveDepartmentRequest request) {
        SysDepartment department = requireDepartment(id);
        validateParent(request.getParentId(), id);
        Long companyId = resolveCompanyId(request.getCompanyId(), request.getParentId(), department);
        ensureNameUnique(request.getName(), companyId, request.getParentId(), id);

        department.setName(request.getName().trim());
        department.setCompanyId(companyId);
        department.setParentId(request.getParentId());
        department.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        department.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ACTIVE");
        departmentMapper.updateById(department);
        syncUserDepartmentNames(department.getId(), department.getName());
        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        requireDepartment(id);

        long childCount = departmentMapper.selectCount(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getDeleted, 0)
                        .eq(SysDepartment::getParentId, id)
        );
        if (childCount > 0) {
            throw BizException.of("请先删除子部门");
        }

        long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .eq(SysUser::getDepartmentId, id)
        );
        if (userCount > 0) {
            throw BizException.of("请先迁移部门下用户");
        }

        departmentMapper.deleteById(id);
    }

    private void validateParent(Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }
        if (currentId != null && parentId.equals(currentId)) {
            throw BizException.of("上级部门不能是当前部门");
        }

        SysDepartment parent = requireDepartment(parentId);
        Long parentCursor = parent.getParentId();
        while (parentCursor != null) {
            if (currentId != null && parentCursor.equals(currentId)) {
                throw BizException.of("上级部门不能是当前部门的子部门");
            }
            SysDepartment cursorDepartment = departmentMapper.selectById(parentCursor);
            if (cursorDepartment == null || cursorDepartment.getDeleted() == 1) {
                break;
            }
            parentCursor = cursorDepartment.getParentId();
        }
    }

    private void ensureNameUnique(String rawName, Long companyId, Long parentId, Long currentId) {
        String name = rawName == null ? "" : rawName.trim();
        if (!StringUtils.hasText(name)) {
            throw BizException.of("部门名称不能为空");
        }

        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getDeleted, 0)
                .eq(SysDepartment::getCompanyId, companyId)
                .eq(SysDepartment::getName, name);
        if (parentId == null) {
            wrapper.isNull(SysDepartment::getParentId);
        } else {
            wrapper.eq(SysDepartment::getParentId, parentId);
        }
        if (currentId != null) {
            wrapper.ne(SysDepartment::getId, currentId);
        }

        if (departmentMapper.selectCount(wrapper) > 0) {
            throw BizException.of("同级部门名称已存在");
        }
    }

    private SysDepartment requireDepartment(Long id) {
        SysDepartment department = departmentMapper.selectById(id);
        if (department == null || department.getDeleted() == 1) {
            throw BizException.notFound("部门");
        }
        return department;
    }

    private void syncUserDepartmentNames(Long departmentId, String departmentName) {
        List<SysUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .eq(SysUser::getDepartmentId, departmentId)
        );
        for (SysUser user : users) {
            SysUser update = new SysUser();
            update.setId(user.getId());
            update.setDepartment(departmentName);
            userMapper.updateById(update);
        }
    }

    private DepartmentTreeDTO toTreeDTO(SysDepartment department) {
        DepartmentTreeDTO dto = new DepartmentTreeDTO();
        dto.setNodeKey("department-" + department.getId());
        dto.setNodeType("DEPARTMENT");
        dto.setId(department.getId());
        dto.setCompanyId(resolveDepartmentCompanyId(department));
        dto.setName(department.getName());
        dto.setParentId(department.getParentId());
        dto.setSortOrder(department.getSortOrder());
        dto.setStatus(department.getStatus());
        return dto;
    }

    private DepartmentTreeDTO toCompanyNode(SysCompany company) {
        DepartmentTreeDTO dto = new DepartmentTreeDTO();
        dto.setNodeKey("company-" + company.getId());
        dto.setNodeType("COMPANY");
        dto.setId(company.getId());
        dto.setCompanyId(company.getId());
        dto.setName(company.getName());
        dto.setSortOrder(0);
        dto.setStatus(company.getStatus());
        return dto;
    }

    private DepartmentTreeDTO buildFallbackCompanyNode(Long companyId) {
        DepartmentTreeDTO dto = new DepartmentTreeDTO();
        dto.setNodeKey("company-" + companyId);
        dto.setNodeType("COMPANY");
        dto.setId(companyId);
        dto.setCompanyId(companyId);
        dto.setName(DEFAULT_COMPANY_NAME);
        dto.setSortOrder(0);
        dto.setStatus("ACTIVE");
        return dto;
    }

    private List<SysCompany> listCompaniesForTree() {
        if (companyService != null) {
            return companyService.listActiveCompanies();
        }

        SysCompany company = new SysCompany();
        company.setId(0L);
        company.setName(DEFAULT_COMPANY_NAME);
        company.setStatus("ACTIVE");
        return List.of(company);
    }

    private Long resolveCompanyId(Long requestedCompanyId, Long parentId, SysDepartment currentDepartment) {
        if (parentId != null) {
            SysDepartment parentDepartment = requireDepartment(parentId);
            Long parentCompanyId = resolveDepartmentCompanyId(parentDepartment);
            if (requestedCompanyId != null && !requestedCompanyId.equals(parentCompanyId)) {
                throw BizException.of("上级部门与公司不一致");
            }
            return parentCompanyId;
        }

        if (requestedCompanyId != null) {
            return requireCompanyId(requestedCompanyId);
        }

        if (currentDepartment != null && currentDepartment.getCompanyId() != null) {
            return currentDepartment.getCompanyId();
        }

        return resolveDefaultCompanyId();
    }

    private Long requireCompanyId(Long companyId) {
        if (companyService != null) {
            return companyService.requireExistingCompany(companyId).getId();
        }
        return companyId;
    }

    private Long resolveDefaultCompanyId() {
        if (companyService != null) {
            return companyService.getCurrentCompanyId();
        }
        return 0L;
    }

    private Long resolveDepartmentCompanyId(SysDepartment department) {
        return department.getCompanyId() == null ? resolveDefaultCompanyId() : department.getCompanyId();
    }

    private void sortChildrenRecursively(DepartmentTreeDTO node) {
        node.getChildren().sort(Comparator.comparing((DepartmentTreeDTO item) -> item.getSortOrder() == null ? 0 : item.getSortOrder()).thenComparing(DepartmentTreeDTO::getId));
        node.getChildren().forEach(this::sortChildrenRecursively);
    }
}
