package com.mhd.boot.common.sse.controller;

import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.common.respnsedata.BaseResultUtils;
import com.mhd.boot.common.sse.core.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author zhao-hao-dong
 */
@RestController
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SseController implements DisposableBean {
    private final SseEmitterManager sseEmitterManager;

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "${sse.path}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        // TODO 获取当前登录用户的 userId 和 tokenValue
        //if (!StpUtil.isLogin()) {
        //    return null;
        //}
        //String tokenValue = StpUtil.getTokenValue();
        //Long userId = LoginHelper.getUserId();
        //return sseEmitterManager.connect(userId, tokenValue);
        return null;
    }

    /**
     * 关闭 SSE 连接
     */
    @GetMapping(value = "${sse.path}/close")
    public BaseResponse<Void> close() {
        // TODO 获取当前登录用户的 userId 和 tokenValue
        //String tokenValue = StpUtil.getTokenValue();
        //Long userId = LoginHelper.getUserId();
        //sseEmitterManager.disconnect(userId, tokenValue);
        return BaseResultUtils.success();
    }

    // 以下为demo仅供参考 禁止使用 请在业务逻辑中使用工具发送而不是用接口发送
//    /**
//     * 向特定用户发送消息
//     *
//     * @param userId 目标用户的 ID
//     * @param msg    要发送的消息内容
//     */
//    @GetMapping(value = "${sse.path}/send")
//    public R<Void> send(Long userId, String msg) {
//        SseMessageDto dto = new SseMessageDto();
//        dto.setUserIds(List.of(userId));
//        dto.setMessage(msg);
//        sseEmitterManager.publishMessage(dto);
//        return R.ok();
//    }
//
//    /**
//     * 向所有用户发送消息
//     *
//     * @param msg 要发送的消息内容
//     */
//    @GetMapping(value = "${sse.path}/sendAll")
//    public R<Void> send(String msg) {
//        sseEmitterManager.publishAll(msg);
//        return R.ok();
//    }

    @Override
    public void destroy() throws Exception {
        // 销毁时不需要做什么 此方法避免无用操作报错
    }
}
