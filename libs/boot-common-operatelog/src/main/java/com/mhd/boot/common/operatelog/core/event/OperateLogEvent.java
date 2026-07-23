package com.mhd.boot.common.operatelog.core.event;

import com.mhd.boot.common.operatelog.core.enums.OperateResultEnum;
import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;
import lombok.Data;

import java.util.Map;

/**
 * 操作日志 Event 事件对象
 *
 * @author zhao-hao-dong
 **/
@Data
public class OperateLogEvent {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 操作模块
     */
    private String operateModule;

    /**
     * 操作描述
     */
    private String operateDescription;

    /**
     * 操作类型，{@link OperateTypeEnum}
     */
    private Integer operateType;

    /**
     * 操作结果，{@link OperateResultEnum}
     */
    private Integer operateResult;

    /**
     * 操作异常信息
     */
    private byte[] operateExceptionDetail;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 请求地址
     */
    private String requestAddress;

    /**
     * 请求浏览器
     */
    private String requestBrowser;

    /**
     * 请求结果
     */
    private String requestResult;

    /**
     * 耗时，单位毫秒
     */
    private Long duration;

    /**
     * 其他参数
     */
    private Map<String, Object> otherParams;
}
