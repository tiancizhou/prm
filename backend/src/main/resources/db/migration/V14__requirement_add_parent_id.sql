-- V14: 需求支持父子关系（大需求拆分为子需求，替代原任务概念）
ALTER TABLE pm_requirement ADD COLUMN parent_id INTEGER REFERENCES pm_requirement(id);
CREATE INDEX IF NOT EXISTS idx_requirement_parent_id ON pm_requirement(parent_id);
