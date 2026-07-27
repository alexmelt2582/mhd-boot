package com.mhd.boot.web.system.model.vo;

import com.mhd.boot.web.system.entity.SysOperLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author zhao-hao-dong
 */
@Data
@AutoMapper(target = SysOperLog.class)
public class SysOperLogVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    private Long logId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 操作描述
     */
    private String operateDescription;
    /**
     * 操作类型：1-查询 2-新增 3-修改 4-删除 5-导出 6-导入 0-其他（字典获取）
     */
    private Integer operateType;
    /**
     * 操作模块
     */
    private String operateModule;
    /**
     * 操作结果：0-成功 1-失败
     */
    private Integer operateResult;
    /**
     * 操作异常信息(堆栈)
     */
    private String operateExceptionDetail;
    /**
     * 请求方法(如: GET, POST)
     */
    private String requestMethod;
    /**
     * 请求参数(JSON格式)
     */
    private String requestParams;
    /**
     * 请求IP
     */
    private String requestIp;
    /**
     * 请求地址(物理位置)
     */
    private String requestAddress;
    /**
     * 请求浏览器/客户端
     */
    private String requestBrowser;
    /**
     * 请求结果(JSON格式)
     */
    private String requestResult;
    /**
     * 请求耗时(单位:毫秒)
     */
    private Long duration;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
