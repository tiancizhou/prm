-- 将旧 Bug 状态迁移到简化后的 3 状态体系
-- NEW / CONFIRMED / ASSIGNED  →  ACTIVE
-- VERIFIED                    →  CLOSED
-- RESOLVED / CLOSED           保持不变

UPDATE pm_bug SET status = 'ACTIVE'  WHERE status IN ('NEW', 'CONFIRMED', 'ASSIGNED');
UPDATE pm_bug SET status = 'CLOSED'  WHERE status = 'VERIFIED';
