package com.mhd.alert.notice.impl.email;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Properties;

/**
 * 邮件渠道通知处理器。
 *
 * <p>基于 Spring {@link JavaMailSender} 渲染 HTML 邮件并通过 SMTP 发送。SMTP 服务器配置
 * 来源优先级：{@code application.yml} 中 {@code spring.mail.*} 配置项（由 Spring Boot 自动
 * 注入 {@link JavaMailSender}），本类额外保留 host/port/username 等字段用于在运行时
 * 覆写发件人信息（如多租户场景切换发件账号）。
 *
 * <p>邮件正文由 {@link #renderContent(NoticeTemplate, AlertGroup)} 渲染为 HTML，
 * 主题固定为 {@link #NOTIFY_TITLE}。发送失败抛 {@link AlertNoticeException}，
 * 由 {@code AlertNoticeDispatch} 的异步任务捕获并记录，不影响其他接收人。
 *
 * @author zhao-hao-dong
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 默认 SMTP 主机，yml 未配置 {@code spring.mail.host} 时使用
     */
    @Value("${spring.mail.host:smtp.demo.com}")
    private String host;

    /**
     * 默认 SMTP 登录用户名，yml 未配置 {@code spring.mail.username} 时使用
     */
    @Value("${spring.mail.username:demo}")
    private String username;

    /**
     * 默认 SMTP 登录密码，yml 未配置 {@code spring.mail.password} 时使用
     */
    @Value("${spring.mail.password:demo}")
    private String password;

    /**
     * 默认 SMTP 端口，yml 未配置 {@code spring.mail.port} 时使用 465（SSL 标准端口）
     */
    @Value("${spring.mail.port:465}")
    private Integer port;

    /**
     * 是否启用 SSL，默认 true
     */
    @Value("${spring.mail.properties.mail.smtp.ssl.enable:true}")
    private boolean sslEnable;

    /**
     * 是否启用 STARTTLS，默认 false（与 SSL 互斥使用）
     */
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private boolean starttlsEnable;

    /**
     * Spring 自动注入的邮件发送器，承载 yml 中 {@code spring.mail.*} 的配置
     */
    private final JavaMailSender javaMailSender;

    /**
     * 发送邮件通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>将 {@link JavaMailSender} 强转为 {@link JavaMailSenderImpl}，并按本类配置
     *       覆写 host/port/账号/SSL 等参数，确保发件配置可用；</li>
     *   <li>构建 {@link MimeMessage}：设置主题、发件人、收件人、发送时间；</li>
     *   <li>渲染通知模板为 HTML 并设为邮件正文；</li>
     *   <li>调用 {@link JavaMailSender#send} 实际发送邮件。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code email} 字段为收件邮箱地址
     * @param noticeTemplate 通知模板，{@code content} 为 FreeMarker HTML 模板
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException 邮件发送失败时抛出，由上层异步任务捕获并记录
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 强转为 JavaMailSenderImpl 以便运行时调整 SMTP 参数
            JavaMailSenderImpl sender = (JavaMailSenderImpl) javaMailSender;
            // 用本类的 yml 配置覆写 sender 参数，保证发件账号与 SSL 策略一致
            sender.setHost(host);
            sender.setPort(port);
            sender.setUsername(username);
            sender.setPassword(password);
            Properties props = sender.getJavaMailProperties();
            props.put("mail.smtp.ssl.enable", sslEnable);
            props.put("mail.smtp.starttls.enable", starttlsEnable);

            // 2. 构建 MimeMessage 并填充邮件元信息
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setSubject(NOTIFY_TITLE);
            // 发件人取 yml 配置的用户名，与 SMTP 登录账号保持一致以避免被拒收
            messageHelper.setFrom(username);
            messageHelper.setTo(receiver.getEmail());
            messageHelper.setSentDate(new Date());

            // 3. 渲染通知模板为 HTML 并作为邮件正文（第二个参数 true 表示 HTML 格式）
            String process = renderContent(noticeTemplate, alert);
            messageHelper.setText(process, true);

            // 4. 实际发送邮件
            javaMailSender.send(mimeMessage);
            log.debug("Send email to {} success", receiver.getEmail());
        } catch (Exception e) {
            throw new AlertNoticeException("[Email Notify Error] " + e.getMessage());
        }
    }

    /**
     * 返回邮件渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#EMAIL}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.EMAIL;
    }
}
