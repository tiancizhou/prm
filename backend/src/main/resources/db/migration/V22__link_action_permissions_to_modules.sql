-- 使用派生表包装子查询，避免 MySQL 更新同表时的 Error 1093
UPDATE sys_permission
SET parent_id = (
    SELECT id FROM (SELECT id, code, deleted FROM sys_permission) AS tmp
    WHERE tmp.code = 'admin:view' AND tmp.deleted = 0
    LIMIT 1
)
WHERE code IN (
    'department:create', 'department:update', 'department:delete',
    'user:create', 'user:update', 'user:delete',
    'role-group:create', 'role-group:update', 'role-group:delete',
    'permission:assign'
);

UPDATE sys_permission
SET parent_id = (
    SELECT id FROM (SELECT id, code, deleted FROM sys_permission) AS tmp
    WHERE tmp.code = 'requirement:view' AND tmp.deleted = 0
    LIMIT 1
)
WHERE code IN (
    'requirement:create', 'requirement:update', 'requirement:delete', 'requirement:assign'
);

UPDATE sys_permission
SET parent_id = (
    SELECT id FROM (SELECT id, code, deleted FROM sys_permission) AS tmp
    WHERE tmp.code = 'bug:view' AND tmp.deleted = 0
    LIMIT 1
)
WHERE code IN (
    'bug:create', 'bug:update', 'bug:delete', 'bug:assign'
);
