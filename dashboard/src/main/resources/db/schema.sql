CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    `conversation_id` VARCHAR(36) NOT NULL,
    `content` TEXT NOT NULL,
    `type` ENUM('USER', 'ASSISTANT', 'SYSTEM', 'TOOL') NOT NULL,
    `timestamp` TIMESTAMP NOT NULL,
    INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX` (`conversation_id`, `timestamp`)
);

CREATE TABLE IF NOT EXISTS `conversation` (
    `id` VARCHAR(64) NOT NULL COMMENT '对话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(255) COMMENT '对话标题',
    `last_message` VARCHAR(255) COMMENT '最后一条消息摘要',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL COMMENT '更新时间',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB COMMENT='对话元数据表';

CREATE TABLE IF NOT EXISTS `search_frequency` (
    `term` VARCHAR(255) PRIMARY KEY COMMENT '搜索词',
    `frequency` BIGINT DEFAULT 0 COMMENT '搜索频率',
    `last_access_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后访问时间'
) ENGINE=InnoDB COMMENT='搜索频率统计表';

CREATE TABLE IF NOT EXISTS `system_user` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(255) DEFAULT NULL COMMENT '用户名',
    `account` VARCHAR(255) NOT NULL COMMENT '账号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `email` VARCHAR(255) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` TEXT DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(50) DEFAULT NULL COMMENT '角色',
    `permission` VARCHAR(50) COMMENT '权限信息',
    `status` VARCHAR(20) DEFAULT NULL COMMENT '状态',
    PRIMARY KEY (id),
    UNIQUE KEY uk_account (account) -- 建议账号唯一，可根据业务添加
) ENGINE=InnoDB COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(100) NOT NULL COMMENT 'Category name',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT 'Category description',
    `created_time` DATETIME NOT NULL COMMENT 'Creation time',
    `updated_time` DATETIME NOT NULL COMMENT 'Update time',
    `creator_id` BIGINT NOT NULL COMMENT 'Creator user ID',
    `updater_id` BIGINT DEFAULT NULL COMMENT 'Last updater user ID',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT 'Logical deletion flag (0-not deleted, 1-deleted)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='Document category table';

CREATE TABLE IF NOT EXISTS `doc` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `category_id` BIGINT DEFAULT NULL COMMENT 'Category ID',
    `title` VARCHAR(200) NOT NULL COMMENT 'Document title',
    `description` TEXT DEFAULT NULL COMMENT 'Document description',
    `doc_type` VARCHAR(20) DEFAULT NULL COMMENT 'Document type',
    `created_time` DATETIME NOT NULL COMMENT 'Creation time',
    `updated_time` DATETIME NOT NULL COMMENT 'Update time',
    `creator_id` BIGINT NOT NULL COMMENT 'Creator user ID',
    `updater_id` BIGINT DEFAULT NULL COMMENT 'Last updater user ID',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT 'Logical deletion flag (0-not deleted, 1-deleted)',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB COMMENT='Document table';

CREATE TABLE IF NOT EXISTS `paragraph` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `doc_id` BIGINT NOT NULL COMMENT 'Document ID this paragraph belongs to',
    `parent_id` BIGINT DEFAULT -1 COMMENT 'Parent paragraph ID for nested structure',
    `title` VARCHAR(200) DEFAULT NULL COMMENT 'Paragraph title',
    `sort` INT DEFAULT 0 COMMENT 'Sort order of the paragraph in the document',
    `content` TEXT COMMENT 'Paragraph content',
    `created_time` DATETIME NOT NULL COMMENT 'Creation time',
    `updated_time` DATETIME NOT NULL COMMENT 'Update time',
    `creator_id` BIGINT NOT NULL COMMENT 'Creator user ID',
    `updater_id` BIGINT DEFAULT NULL COMMENT 'Last updater user ID',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT 'Logical deletion flag (0-not deleted, 1-deleted)',
    PRIMARY KEY (`id`),
    KEY `idx_doc_id` (`doc_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB COMMENT='Document paragraphs table';

CREATE TABLE IF NOT EXISTS `edit_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `paragraph_id` BIGINT NOT NULL COMMENT 'Paragraph ID',
    `editor_id` BIGINT NOT NULL COMMENT 'Editor user ID',
    `paragraph_content` TEXT COMMENT 'Paragraph content',
    `edit_time` DATETIME NOT NULL COMMENT 'Edit timestamp',
    PRIMARY KEY (`id`),
    KEY `idx_paragraph_id` (`paragraph_id`)
) ENGINE=InnoDB COMMENT='Document edit history tracking';


CREATE TABLE IF NOT EXISTS `doc_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User ID with permission',
    `doc_id` BIGINT NOT NULL COMMENT 'Document ID being granted permission to',
    `permission_type` INT NOT NULL COMMENT 'Type of permission (1-Read, 2-Write, 3-Admin)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_doc` (`user_id`, `doc_id`),
    KEY `idx_doc_id` (`doc_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='Document permission management table';

CREATE TABLE IF NOT EXISTS `paragraph_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User ID with permission',
    `paragraph_id` BIGINT NOT NULL COMMENT 'Paragraph ID being granted permission to',
    `permission_type` INT NOT NULL COMMENT 'Type of permission (1-Read, 2-Write, 3-Admin)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_paragraph` (`user_id`, `paragraph_id`),
    KEY `idx_paragraph_id` (`paragraph_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='Paragraph permission management table';

CREATE TABLE IF NOT EXISTS `preset_prompt` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(100) NOT NULL COMMENT 'Preset name',
    `description` VARCHAR(500) DEFAULT NULL COMMENT 'Preset description',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT 'Icon (emoji or icon name)',
    `prompt_content` TEXT NOT NULL COMMENT 'Prompt content',
    `category` VARCHAR(50) DEFAULT NULL COMMENT 'Category',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT 'Is enabled (0-disabled, 1-enabled)',
    `is_system` TINYINT(1) DEFAULT 0 COMMENT 'Is system preset (0-user, 1-system)',
    `created_at` DATETIME NOT NULL COMMENT 'Creation time',
    `updated_at` DATETIME NOT NULL COMMENT 'Update time',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB COMMENT='Preset prompt table';

CREATE TABLE IF NOT EXISTS `platform_resource` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'Primary key',
    `resource_type` VARCHAR(50) NOT NULL COMMENT 'Resource type: Namespace, Deployment, Service, Ingress, PVC, ConfigMap, Secret, Job, MPIJob',
    `resource_name` VARCHAR(255) NOT NULL COMMENT 'Resource name in K8S',
    `namespace` VARCHAR(255) DEFAULT '' COMMENT 'K8S namespace (empty string for cluster-scoped resources)',
    `creator_id` BIGINT NOT NULL COMMENT 'Creator user ID from system_user table',
    `created_time` DATETIME NOT NULL COMMENT 'Record creation time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_resource` (`resource_type`, `namespace`, `resource_name`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB COMMENT='Tracks K8S resources created through the platform';

CREATE TABLE IF NOT EXISTS `site_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `title` VARCHAR(500) NOT NULL COMMENT 'Message title',
    `content` TEXT NOT NULL COMMENT 'Message content',
    `type` VARCHAR(50) DEFAULT 'INFO' COMMENT 'Message type: INFO, WARNING, ERROR, SUCCESS',
    `priority` INT DEFAULT 0 COMMENT 'Priority: higher number means higher priority',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT 'Is active (0-inactive, 1-active)',
    `publish_time` DATETIME COMMENT 'Publish time (if not set, use created_time)',
    `expire_time` DATETIME COMMENT 'Expire time (if not set, never expires)',
    `creator_id` BIGINT COMMENT 'Creator user ID',
    `created_time` DATETIME NOT NULL COMMENT 'Creation time',
    `updated_time` DATETIME NOT NULL COMMENT 'Update time',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT 'Logical deletion flag (0-not deleted, 1-deleted)',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_active` (`is_active`),
    KEY `idx_priority` (`priority`),
    KEY `idx_publish_time` (`publish_time`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB COMMENT='Site broadcast message table';

CREATE TABLE IF NOT EXISTS `site_message_read` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `message_id` BIGINT NOT NULL COMMENT 'Message ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID who read the message',
    `read_time` DATETIME NOT NULL COMMENT 'Read time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_user` (`message_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB COMMENT='Site message read status table';

-- 初始化欢迎消息
INSERT INTO `site_message` (`title`, `content`, `type`, `priority`, `is_active`, `created_time`, `updated_time`)
SELECT '欢迎使用 Kubernetes Dashboard For AI',
       '您好！欢迎使用 Kubernetes Dashboard For AI。\n\n这是一个 Kubernetes 集群管理平台，您可以：\n\n• 管理 K8S 集群资源\n• 查看集群监控\n• 使用 AI 助手帮助\n• 更多功能等您探索\n\n祝您使用愉快！',
       'SUCCESS',
       10,
       1,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM `site_message` WHERE `title` = '欢迎使用 Kubernetes Dashboard For AI');

CREATE TABLE IF NOT EXISTS `ai_expert` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT DEFAULT NULL COMMENT 'User ID (NULL for system defaults)',
    `name` VARCHAR(100) NOT NULL COMMENT 'Expert name',
    `description` VARCHAR(500) DEFAULT NULL COMMENT 'Expert description',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT 'Icon emoji or name',
    `system_prompt` TEXT NOT NULL COMMENT 'System prompt defining expert behavior',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT 'Whether enabled',
    `is_system` TINYINT(1) DEFAULT 0 COMMENT 'System preset (not user-editable)',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT 'Soft delete',
    `created_at` DATETIME NOT NULL COMMENT 'Create time',
    `updated_at` DATETIME NOT NULL COMMENT 'Update time',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB COMMENT='AI expert/agent table';

-- 初始化 admin 管理员账户
INSERT INTO `system_user` (`username`, `account`, `password`, `email`, `role`, `permission`, `status`)
SELECT '管理员', 'admin', 'admin123', 'admin@example.com', 'ADMIN', 'ALL', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `system_user` WHERE `account` = 'admin');

