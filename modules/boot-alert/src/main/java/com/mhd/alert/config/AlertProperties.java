package com.mhd.alert.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhao-hao-dong
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.mhd.alert")
public class AlertProperties {
    //private String
    private InhibitProperties inhibit;

    /**
     * 抑制配置
     */
    @Getter
    @Setter
    public static class InhibitProperties {

        /**
         * 抑制规则的过期时间，单位毫秒，默认4小时
         */
        private long ttl = 4 * 60 * 60 * 1000L;
    }
}
