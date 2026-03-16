-- 人员表扩展：与主数据同步字段（部门、小组、工号、姓名、外部主键）
ALTER TABLE sys_user ADD COLUMN employee_no   VARCHAR(64);
ALTER TABLE sys_user ADD COLUMN real_name     VARCHAR(64);
ALTER TABLE sys_user ADD COLUMN department    VARCHAR(128);
ALTER TABLE sys_user ADD COLUMN team          VARCHAR(128);
ALTER TABLE sys_user ADD COLUMN external_id   VARCHAR(128);

CREATE UNIQUE INDEX IF NOT EXISTS idx_sys_user_employee_no ON sys_user(employee_no) WHERE employee_no IS NOT NULL AND deleted = 0;
CREATE INDEX IF NOT EXISTS idx_sys_user_external_id ON sys_user(external_id) WHERE external_id IS NOT NULL;
