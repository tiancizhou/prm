-- 新增「创建项目」权限，挂到项目集模块下，便于按模块分配
INSERT OR IGNORE INTO sys_permission (name, code, type, parent_id, sort)
SELECT '创建项目', 'project:create', 'ACTION', id, 25
FROM sys_permission WHERE code = 'projects:view' AND deleted = 0 LIMIT 1;

-- 为超级管理员、项目经理分配该权限（与原有“创建/归档项目”能力一致）
INSERT OR IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.code = 'project:create' AND p.deleted = 0
WHERE r.code IN ('SUPER_ADMIN', 'PROJECT_ADMIN') AND r.deleted = 0;
