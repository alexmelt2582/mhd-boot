package com.mhd.boot.common.operatelog.core.service;

import com.mhd.boot.common.operatelog.core.vo.OperateLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

/**
 * 操作日志处理器默认接口实现类
 *
 * @author zhao-hao-dong
 **/
@Slf4j
public class OperateLogDefaultHandlerServiceImpl implements OperateLogHandlerService {
    @Override
    @Async
    public void handleLog(OperateLogVO logObject) {
        log.warn("[operateLog][记录日志：{}]", logObject);
    }
}
