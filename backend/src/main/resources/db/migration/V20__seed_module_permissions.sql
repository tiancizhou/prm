INSERT IGNORE INTO sys_permission (name, code, type, parent_id, sort)
VALUES
    ('工作台', 'dashboard:view', 'MODULE', 0, 10),
    ('项目集', 'projects:view', 'MODULE', 0, 20),
    ('文档', 'docs:view', 'MODULE', 0, 30),
    ('组织', 'organization:view', 'MODULE', 0, 40),
    ('后台', 'admin:view', 'MODULE', 0, 50),
    ('项目概览', 'project.overview:view', 'MODULE', 0, 60),
    ('需求', 'requirement:view', 'MODULE', 0, 70),
    ('Bug', 'bug:view', 'MODULE', 0, 80),
    ('迭代', 'sprint:view', 'MODULE', 0, 90),
    ('项目成员', 'project.member:view', 'MODULE', 0, 100);
