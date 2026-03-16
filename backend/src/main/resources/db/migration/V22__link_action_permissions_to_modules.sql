UPDATE sys_permission
SET parent_id = (
    SELECT id FROM sys_permission parent
    WHERE parent.code = 'admin:view' AND parent.deleted = 0
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
    SELECT id FROM sys_permission parent
    WHERE parent.code = 'requirement:view' AND parent.deleted = 0
    LIMIT 1
)
WHERE code IN (
    'requirement:create', 'requirement:update', 'requirement:delete', 'requirement:assign'
);

UPDATE sys_permission
SET parent_id = (
    SELECT id FROM sys_permission parent
    WHERE parent.code = 'bug:view' AND parent.deleted = 0
    LIMIT 1
)
WHERE code IN (
    'bug:create', 'bug:update', 'bug:delete', 'bug:assign'
);
