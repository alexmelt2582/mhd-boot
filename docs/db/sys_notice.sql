CREATE TABLE `sys_notice`
(
    `notice_id`      BIGINT      NOT NULL COMMENT '公告ID',
    `notice_title`   VARCHAR(50) NOT NULL COMMENT '公告标题',
    `notice_type`    CHAR(1)     NOT NULL COMMENT '公告类型（如：1-通知, 2-公告）字典获取',
    `notice_content` TEXT COMMENT '公告内容',
    `status`         CHAR(1)      DEFAULT '0' COMMENT '公告状态（如：0-正常, 1-关闭）',
    `create_by`      BIGINT COMMENT '创建者ID',
    `create_time`    DATETIME(6) COMMENT '创建时间',
    `update_by`      BIGINT COMMENT '更新者ID',
    `update_time`    DATETIME(6) COMMENT '更新时间',
    `remark`         VARCHAR(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`notice_id`),
    -- 关键索引：按状态过滤（如查询当前生效的公告）
    INDEX            `idx_status` (`status`),
    -- 关键索引：按创建时间排序或范围查询（如分页获取最新公告）
    INDEX            `idx_create_time` (`create_time`),
    -- 关键索引：按创建人查询
    INDEX            `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统公告表';