-- V9: 需求取消类型区分（小团队/轻流程，不需要 Epic/Story 区分）
-- 使用 INFORMATION_SCHEMA + Prepared Statement 实现幂等删列，兼容所有 MySQL 版本
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'pm_requirement'
      AND COLUMN_NAME  = 'type'
);
SET @sql = IF(@col_exists > 0,
    'ALTER TABLE pm_requirement DROP COLUMN `type`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
