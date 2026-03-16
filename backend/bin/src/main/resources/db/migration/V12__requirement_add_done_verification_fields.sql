-- V12: completed requirement verification template fields
ALTER TABLE pm_requirement
    ADD COLUMN verification_scenario TEXT;

ALTER TABLE pm_requirement
    ADD COLUMN verification_steps TEXT;

ALTER TABLE pm_requirement
    ADD COLUMN verification_result TEXT;

ALTER TABLE pm_requirement
    ADD COLUMN verification_conclusion TEXT;

ALTER TABLE pm_requirement
    ADD COLUMN verification_method VARCHAR(64);

