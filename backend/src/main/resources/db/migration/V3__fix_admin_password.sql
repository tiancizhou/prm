-- 修复管理员密码 BCrypt hash（密码: Admin@123）
UPDATE sys_user SET password = '$2a$10$d2GYzgRgLrVidaEj0xwVUuCSFHYsUhYqh4mlEKOL4wo0PbFD/qr1y'
WHERE username = 'admin';
