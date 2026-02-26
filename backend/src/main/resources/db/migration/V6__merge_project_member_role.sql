-- 合并项目成员角色与系统角色：将旧的 ADMIN 统一改为 PROJECT_ADMIN
UPDATE pm_project_member SET role = 'PROJECT_ADMIN' WHERE role = 'ADMIN';
