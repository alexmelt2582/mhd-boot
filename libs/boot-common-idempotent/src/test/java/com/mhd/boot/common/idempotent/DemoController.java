package com.mhd.boot.common.idempotent;

import com.mhd.boot.common.idempotent.annotation.RepeatSubmit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhao-hao-dong
 **/
@RestController
public class DemoController {
    @GetMapping("/get")
    @RepeatSubmit()
    public String get() throws Exception {
        Thread.sleep(2000L);
        return "success";
    }

    @GetMapping("/noKey")
    @RepeatSubmit(interval = 3000, message = "请勿重复查询")
    public String noKey() throws Exception {
        Thread.sleep(2000L);
        return "success";
    }
}
