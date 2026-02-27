-- V8: 需求状态简化为三态（小团队/轻流程）
-- 待办(DRAFT) / 进行中(IN_PROGRESS) / 已完成(DONE)
UPDATE pm_requirement SET status = 'DRAFT'   WHERE status IN ('REVIEWING', 'APPROVED');
UPDATE pm_requirement SET status = 'DONE'    WHERE status = 'CLOSED';
