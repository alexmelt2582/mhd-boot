package com.mhd.alert.notice.impl.sms;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;

/**
 * 短信渠道客户端接口。
 *
 * <p>不同短信服务商（阿里云、腾讯云、AWS、Twilio 等）实现该接口，由
 * {@link SmsClientFactory} 根据配置的 provider 类型实例化。{@code SmsAlertNoticeHandlerImpl}
 * 通过工厂获取当前生效的客户端，并委托其完成实际短信发送。
 *
 * <p>实现类应保持无状态或线程安全，因为客户端实例会被多线程并发调用
 * （通知派发在 {@code AlertThreadPoolConfig#executeNotify} 线程池中异步执行）。
 *
 * @author zhao-hao-dong
 */
public interface SmsClient {

    /**
     * 发送短信通知。
     *
     * @param receiver       通知接收人，{@code phone} 字段为手机号
     * @param noticeTemplate 通知模板（部分服务商需要模板 ID，可从模板 name 或 content 中获取）
     * @param alert          告警组，作为短信内容数据来源
     */
    void sendMessage(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert);

    /**
     * 获取短信服务商类型标识，对应配置中的 {@code type} 字段。
     *
     * @return 服务商类型字符串，如 {@code alibaba}、{@code tencent}
     */
    String getType();

    /**
     * 校验客户端配置是否完整可用。
     *
     * <p>发送前由 {@code SmsAlertNoticeHandlerImpl} 调用，配置缺失时抛出
     * {@link com.mhd.alert.notice.AlertNoticeException} 避免无效请求。
     *
     * @return true 表示配置完整可用；false 表示配置缺失或无效
     */
    boolean checkConfig();
}
