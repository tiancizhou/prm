-- V10: 需求增加实际工时字段，用于记录完成时工时

ALTER TABLE pm_requirement
    ADD COLUMN actual_hours DECIMAL(8,2) NOT NULL DEFAULT 0;

