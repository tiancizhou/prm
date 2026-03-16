UPDATE pm_project_member
SET role = 'MEMBER'
WHERE role IS NULL OR role <> 'MEMBER';
