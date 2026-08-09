package com.mhd.alert.notice.impl.sms;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信渠道通知处理器。
 *
 * <p>不直接实现短信发送逻辑，而是通过 {@link SmsClientFactory} 获取当前配置的
 * {@link SmsClient}（阿里云、HTTP 网关等）委托发送。这种委托模式将「渠道路由」
 * 与「服务商协议」解耦：本类只负责 SMS 渠道的统一入口与异常包装，
 * 具体短信发送细节由各 {@link SmsClient} 实现承载。
 *
 * <p>典型流程：{@code AlertNoticeDispatch} 按接收人 type=0 路由到本类 →
 * 本类从工厂获取客户端 → 委托客户端发送短信 → 异常统一包装为
 * {@link AlertNoticeException} 由上层异步任务捕获。
 *
 * @author zhao-hao-dong
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 短信客户端工厂，按 yml 配置实例化对应服务商客户端
     */
    private final SmsClientFactory smsClientFactory;

    /**
     * 发送短信通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>从工厂获取当前生效的 {@link SmsClient}，为 null 表示短信服务未启用；</li>
     *   <li>调用 {@link SmsClient#checkConfig()} 校验配置完整性，不通过则抛异常；</li>
     *   <li>委托 {@link SmsClient#sendMessage} 完成实际发送；</li>
     *   <li>捕获所有异常统一包装为 {@link AlertNoticeException}。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code phone} 字段为手机号
     * @param noticeTemplate 通知模板（短信渠道由服务商模板承担，此参数供客户端按需使用）
     * @param alert          告警组，作为短信内容数据来源
     * @throws AlertNoticeException 短信服务未启用、配置无效或发送失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 获取当前生效的短信客户端，为 null 表示短信服务未启用
            SmsClient smsClient = smsClientFactory.getSmsClient();
            if (smsClient == null) {
                throw new AlertNoticeException("No SMS Service available, please check the configuration");
            }
            // 2. 校验客户端配置完整性，避免无效请求
            if (!smsClient.checkConfig()) {
                throw new AlertNoticeException(smsClient.getType()
                        + " SMS Service configuration is invalid, please check the configuration");
            }
            // 3. 委托具体客户端发送短信
            smsClient.sendMessage(receiver, noticeTemplate, alert);
        } catch (AlertNoticeException e) {
            // 已包装的异常直接抛出，保留原始错误信息
            throw e;
        } catch (Exception e) {
            // 4. 兜底捕获：将其他异常统一包装为 AlertNoticeException
            throw new AlertNoticeException("[Sms Notify Error] " + e.getMessage());
        }
    }

    /**
     * 返回短信渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#SMS}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.SMS;
    }
}
