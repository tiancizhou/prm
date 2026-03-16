INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT ur.user_id, target.id
FROM sys_user_role ur
JOIN sys_role source ON source.id = ur.role_id
JOIN sys_role target ON target.code = 'PROJECT_ADMIN' AND target.deleted = 0
WHERE source.code = 'PM';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT ur.user_id, target.id
FROM sys_user_role ur
JOIN sys_role source ON source.id = ur.role_id
JOIN sys_role target ON target.code = 'DEV' AND target.deleted = 0
WHERE source.code IN ('QA', 'GUEST');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT target.id, rp.permission_id
FROM sys_role_permission rp
JOIN sys_role source ON source.id = rp.role_id
JOIN sys_role target ON target.code = 'PROJECT_ADMIN' AND target.deleted = 0
WHERE source.code = 'PM';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT target.id, rp.permission_id
FROM sys_role_permission rp
JOIN sys_role source ON source.id = rp.role_id
JOIN sys_role target ON target.code = 'DEV' AND target.deleted = 0
WHERE source.code IN ('QA', 'GUEST');

DELETE FROM sys_user_role
WHERE role_id IN (
    SELECT id FROM sys_role WHERE code IN ('PM', 'QA', 'GUEST')
);

DELETE FROM sys_role_permission
WHERE role_id IN (
    SELECT id FROM sys_role WHERE code IN ('PM', 'QA', 'GUEST')
);

DELETE FROM sys_role
WHERE code IN ('PM', 'QA', 'GUEST');
