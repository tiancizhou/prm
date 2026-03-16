-- 移除文档模块权限：删除角色-权限关联后软删除权限，按模块分配中不再展示
DELETE FROM sys_role_permission
WHERE permission_id IN (SELECT id FROM sys_permission WHERE code = 'docs:view' AND deleted = 0);

UPDATE sys_permission
SET deleted = 1
WHERE code = 'docs:view' AND deleted = 0;
