-- 初始化管理员账号（若 account='admin' 已存在则不创建）
INSERT INTO `system_user` (`username`, `account`, `password`, `email`, `role`, `permission`, `status`)
SELECT '管理员', 'admin', 'admin123', 'admin@example.com', 'ADMIN', 'ALL', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `system_user` WHERE `account` = 'admin');
