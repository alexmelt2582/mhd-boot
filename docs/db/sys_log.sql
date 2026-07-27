CREATE TABLE `sys_oper_log`
(
    `log_id`                   BIGINT(20)   NOT NULL COMMENT '主键ID',
    `user_id`                  BIGINT(20)   NOT NULL COMMENT '用户ID',
    `operate_description`      VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
    `operate_type`             TINYINT(4)   DEFAULT NULL COMMENT '操作类型：1-查询 2-新增 3-修改 4-删除 5-导出 6-导入 0-其他（字典获取）',
    `operate_module`           VARCHAR(255) DEFAULT NULL COMMENT '操作模块',
    `operate_result`           TINYINT(4)   NOT NULL COMMENT '操作结果：0-成功 1-失败',
    `operate_exception_detail` LONGTEXT     DEFAULT NULL COMMENT '操作异常信息(堆栈)',
    `request_method`           VARCHAR(255) DEFAULT NULL COMMENT '请求方法(如: GET, POST)',
    `request_params`           TEXT         DEFAULT NULL COMMENT '请求参数(JSON格式)',
    `request_ip`               VARCHAR(255) DEFAULT NULL COMMENT '请求IP',
    `request_address`          VARCHAR(255) DEFAULT NULL COMMENT '请求地址(物理位置)',
    `request_browser`          VARCHAR(255) DEFAULT NULL COMMENT '请求浏览器/客户端',
    `request_result`           TEXT         DEFAULT NULL COMMENT '请求结果(JSON格式)',
    `duration`                 BIGINT(20)   DEFAULT NULL COMMENT '请求耗时(单位:毫秒)',
    `create_time`              DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间(精确到毫秒)',
    PRIMARY KEY (`log_id`),
    -- 核心索引：按时间范围查询（日志表最常用的查询，必须加）
    INDEX                      `idx_create_time` (`create_time`),
    -- 核心索引：按用户查询其操作记录
    INDEX                      `idx_user_id` (`user_id`),
    -- 联合索引：按操作类型和结果过滤（例如：查询某模块下所有失败的请求）
    INDEX                      `idx_type_result` (`operate_type`, `operate_result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统-操作日志表';