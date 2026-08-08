package com.mhd.alert.controller;

import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.service.NoticeReceiverService;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.responsedata.BaseResultUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhao-hao-dong
 */
@RestController
@RequestMapping(path = "/api/notice")
@RequiredArgsConstructor
public class NoticeReceiverController {
    private final NoticeReceiverService noticeReceiverService;

    @PostMapping(path = "/receiver/send-test-msg")
    public BaseResponse<Void> sendTestMsg(@Valid @RequestBody NoticeReceiver noticeReceiver) {
        boolean sendFlag = noticeReceiverService.sendTestMsg(noticeReceiver);
        return sendFlag ? BaseResultUtils.success() : BaseResultUtils.error("通知服务发送失败，请检查配置是否正确");
    }
}
