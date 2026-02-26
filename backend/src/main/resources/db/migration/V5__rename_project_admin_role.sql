-- 将「项目管理员」重命名为「项目经理」
UPDATE sys_role SET name = '项目经理' WHERE code = 'PROJECT_ADMIN';
