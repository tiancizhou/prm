-- 为默认角色初始化「按模块分配」权限，使界面显示与实际行为一致
-- 超级管理员：所有模块+动作（前端仍按角色名放行，此处仅保证对话框有数据）
-- 项目经理：所有模块+动作，便于后台与项目全权管理
-- 开发：仅工作台/项目集/文档/组织/项目概览/需求/Bug/迭代/项目成员及需求与Bug的创建与编辑

INSERT OR IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'SUPER_ADMIN' AND r.deleted = 0
  AND p.deleted = 0 AND p.type IN ('MODULE', 'ACTION');

INSERT OR IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'PROJECT_ADMIN' AND r.deleted = 0
  AND p.deleted = 0 AND p.type IN ('MODULE', 'ACTION');

INSERT OR IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.deleted = 0 AND p.type IN ('MODULE', 'ACTION')
WHERE r.code = 'DEV' AND r.deleted = 0
  AND p.code IN (
    'dashboard:view',
    'projects:view',
    'docs:view',
    'organization:view',
    'project.overview:view',
    'requirement:view',
    'requirement:create',
    'requirement:update',
    'bug:view',
    'bug:create',
    'bug:update',
    'sprint:view',
    'project.member:view'
  );
