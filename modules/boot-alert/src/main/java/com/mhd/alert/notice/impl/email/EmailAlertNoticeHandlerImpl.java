package com.mhd.alert.notice.impl.email;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * @author zhao-hao-dong
 */
@Component
@RequiredArgsConstructor
public class EmailAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {
    //private final JavaMailSender javaMailSender;

    @Value("${spring.mail.host:smtp.demo.com}")
    private String host;

    @Value("${spring.mail.username:demo}")
    private String username;

    @Value("${spring.mail.password:demo}")
    private String password;

    @Value("${spring.mail.port:465}")
    private Integer port;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable:true}")
    private boolean sslEnable = true;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private boolean starttlsEnable = false;

    //public EmailAlertNoticeHandlerImpl(JavaMailSender javaMailSender) {
    //    this.javaMailSender = javaMailSender;
    //}

    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            //JavaMailSenderImpl sender = (JavaMailSenderImpl) javaMailSender;
        } catch (Exception e) {
            throw new AlertNoticeException("[Email Notify Error] " + e.getMessage());
        }
    }

    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.EMAIL;
    }
}
