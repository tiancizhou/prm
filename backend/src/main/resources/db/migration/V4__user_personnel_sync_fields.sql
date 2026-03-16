-- 人员表扩展：与主数据同步字段（部门、小组、工号、姓名、外部主键）
ALTER TABLE sys_user ADD COLUMN employee_no   VARCHAR(64);
ALTER TABLE sys_user ADD COLUMN real_name     VARCHAR(64);
ALTER TABLE sys_user ADD COLUMN department    VARCHAR(128);
ALTER TABLE sys_user ADD COLUMN team          VARCHAR(128);
ALTER TABLE sys_user ADD COLUMN external_id   VARCHAR(128);

-- MySQL 不支持带 WHERE 子句的部分索引，NULL 值在唯一索引中允许重复，语义等价
CREATE UNIQUE INDEX idx_sys_user_employee_no ON sys_user(employee_no);
CREATE INDEX idx_sys_user_external_id ON sys_user(external_id);
