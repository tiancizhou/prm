package com.prm.module.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaveRoleModulePermissionsRequest {
    private List<String> permissionCodes;
}
