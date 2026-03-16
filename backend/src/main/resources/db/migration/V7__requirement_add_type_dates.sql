-- V7: pm_requirement 表补充 type / start_date / due_date 字段
ALTER TABLE pm_requirement ADD COLUMN type       VARCHAR(32) NOT NULL DEFAULT 'STORY';
ALTER TABLE pm_requirement ADD COLUMN start_date DATE;
ALTER TABLE pm_requirement ADD COLUMN due_date   DATE;

CREATE INDEX idx_requirement_due_date ON pm_requirement(due_date);
