package com.mhd.boot.common.operatelog.core.service;

import com.mhd.boot.common.operatelog.core.vo.OperateLogVO;
import jakarta.validation.constraints.NotNull;

/**
 * 操作日志处理器接口
 *
 * @author zhao-hao-dong
 **/
public interface OperateLogHandlerService {
    /**
     * 处理日志
     *
     * @param logObject 日志 OperateLogVO 对象，对象不会为空
     */
    void handleLog(@NotNull(message = "OperateLogVO 对象不能为空") OperateLogVO logObject);
}
