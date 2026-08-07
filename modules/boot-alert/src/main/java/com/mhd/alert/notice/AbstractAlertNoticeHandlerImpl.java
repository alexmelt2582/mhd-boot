package com.mhd.alert.notice;

import com.mhd.alert.config.AlertProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

/**
 * @author zhao-hao-dong
 */
public abstract class AbstractAlertNoticeHandlerImpl implements AlertNoticeHandler {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected AlertProperties alertProperties;
}
