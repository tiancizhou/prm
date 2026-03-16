-- 区分超级管理员与项目经理：项目经理仅保留「非后台」权限，不能进入后台管理（部门/用户/权限）
-- 超级管理员保持全部权限不变；项目经理移除 admin:view 及其下所有动作权限

DELETE FROM sys_role_permission
WHERE role_id = (SELECT id FROM sys_role WHERE code = 'PROJECT_ADMIN' AND deleted = 0)
  AND permission_id IN (
    SELECT id FROM sys_permission
    WHERE deleted = 0
      AND (
        code = 'admin:view'
        OR code IN (
          'department:create', 'department:update', 'department:delete',
          'user:create', 'user:update', 'user:delete',
          'role-group:create', 'role-group:update', 'role-group:delete',
          'permission:assign'
        )
      )
  );
