package com.mhd.boot.common.idempotent;

import com.mhd.boot.common.idempotent.core.annotation.Idempotent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhao-hao-dong
 **/
@RestController
public class DemoController {
    @GetMapping("/get")
    @Idempotent(uniqueExpression = "#key", duration = 3, info = "请勿重复查询")
    public String get(String key) throws Exception {
        Thread.sleep(2000L);
        return "success";
    }

    @GetMapping("/noKey")
    @Idempotent(duration = 3, info = "请勿重复查询")
    public String noKey() throws Exception {
        Thread.sleep(2000L);
        return "success";
    }
}
