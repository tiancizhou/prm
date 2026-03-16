-- V11: 需求增加实际开始/结束时间，用于完成时按分钟自动计算工时

ALTER TABLE pm_requirement
    ADD COLUMN actual_start_at DATETIME;

ALTER TABLE pm_requirement
    ADD COLUMN actual_end_at DATETIME;

