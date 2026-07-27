CREATE TABLE `sys_dict_type`
(
    `dict_id`          BIGINT NOT NULL COMMENT '主键ID',
    `dict_name`   VARCHAR(100) DEFAULT NULL COMMENT '字典名称',
    `dict_type`   VARCHAR(100) DEFAULT NULL COMMENT '字典类型（唯一标识，用于关联字典数据）',
    `create_by`   BIGINT       DEFAULT NULL COMMENT '创建者ID',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   BIGINT       DEFAULT NULL COMMENT '更新者ID',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    PRIMARY KEY (`dict_id`),
    KEY           `idx_dict_type` (`dict_type`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统字典类型表';


CREATE TABLE `sys_dict_item`
(
    `dict_item_id`          BIGINT NOT NULL COMMENT '主键ID',
    `dict_sort`   INT          DEFAULT 0 COMMENT '字典排序（数值越小越靠前）',
    `dict_label`  VARCHAR(100) DEFAULT NULL COMMENT '字典标签（前端展示的名称）',
    `dict_value`  VARCHAR(100) DEFAULT NULL COMMENT '字典键值（后端存储的实际值）',
    `dict_type`   VARCHAR(100) DEFAULT NULL COMMENT '字典类型（关联 sys_dict_type 表的 dict_type）',
    `css_class`   VARCHAR(100) DEFAULT NULL COMMENT '样式属性（其他CSS样式扩展）',
    `list_class`  VARCHAR(100) DEFAULT NULL COMMENT '表格回显样式（如 primary, success, warning 等）',
    `is_default`  CHAR(1)      DEFAULT 'N' COMMENT '是否默认（Y是 N否，默认N）',
    `create_by`   BIGINT       DEFAULT NULL COMMENT '创建者ID',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`   BIGINT       DEFAULT NULL COMMENT '更新者ID',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    PRIMARY KEY (`dict_item_id`),
    KEY           `idx_dict_type` (`dict_type`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统字典数据表';