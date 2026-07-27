package com.mhd.boot.web.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zhao-hao-dong
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "log_id")
    private Long logId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作描述
     */
    @TableField("operate_description")
    private String operateDescription;

    /**
     * 操作类型：1-查询 2-新增 3-修改 4-删除 5-导出 6-导入 0-其他（字典获取）
     */
    @TableField("operate_type")
    private Integer operateType;

    /**
     * 操作模块
     */
    @TableField("operate_module")
    private String operateModule;

    /**
     * 操作结果：0-成功 1-失败
     */
    @TableField("operate_result")
    private Integer operateResult;

    /**
     * 操作异常信息(堆栈)
     */
    @TableField(value = "operate_exception_detail", jdbcType = org.apache.ibatis.type.JdbcType.LONGVARCHAR)
    private String operateExceptionDetail;

    /**
     * 请求方法(如: GET, POST)
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数(JSON格式)
     */
    @TableField(value = "request_params", jdbcType = org.apache.ibatis.type.JdbcType.LONGVARCHAR)
    private String requestParams;

    /**
     * 请求IP
     */
    @TableField("request_ip")
    private String requestIp;

    /**
     * 请求地址(物理位置)
     */
    @TableField("request_address")
    private String requestAddress;

    /**
     * 请求浏览器/客户端
     */
    @TableField("request_browser")
    private String requestBrowser;

    /**
     * 请求结果(JSON格式)
     */
    @TableField(value = "request_result", jdbcType = org.apache.ibatis.type.JdbcType.LONGVARCHAR)
    private String requestResult;

    /**
     * 请求耗时(单位:毫秒)
     */
    @TableField("duration")
    private Long duration;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;
}
