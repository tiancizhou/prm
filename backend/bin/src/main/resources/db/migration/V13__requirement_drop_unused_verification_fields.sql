-- V13: remove deprecated verification fields
ALTER TABLE pm_requirement
    DROP COLUMN verification_conclusion;

ALTER TABLE pm_requirement
    DROP COLUMN verification_method;

