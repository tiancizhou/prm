-- 角色分组标签样式：用于前端展示时 el-tag 的 type，新增分组时可在后台选择，无需改前端代码
ALTER TABLE sys_role ADD COLUMN tag_type VARCHAR(16) DEFAULT 'info';

UPDATE sys_role SET tag_type = 'danger'  WHERE code = 'SUPER_ADMIN' AND deleted = 0;
UPDATE sys_role SET tag_type = 'warning' WHERE code = 'PROJECT_ADMIN' AND deleted = 0;
UPDATE sys_role SET tag_type = 'primary' WHERE code = 'DEV' AND deleted = 0;

-- 其余未设置的保持默认 'info'
UPDATE sys_role SET tag_type = 'info' WHERE (tag_type IS NULL OR tag_type = '') AND deleted = 0;
