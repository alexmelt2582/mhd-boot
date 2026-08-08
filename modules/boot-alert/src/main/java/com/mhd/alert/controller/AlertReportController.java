package com.mhd.alert.controller;

import com.mhd.alert.extern.ExternAlertService;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.responsedata.BaseResultUtils;
import com.mhd.boot.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author zhao-hao-dong
 **/
@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class AlertReportController {
    private Map<String, ExternAlertService> externAlertServiceMap;

    public AlertReportController(List<ExternAlertService> externAlertServiceList) {
        this.externAlertServiceMap = new HashMap<>(externAlertServiceList.size());
        for (ExternAlertService externAlertService : externAlertServiceList) {
            if (StringUtils.isBlank(externAlertService.supportSource()))
                throw new IllegalArgumentException("ExternAlertService supportSource is blank");
            externAlertServiceMap.put(externAlertService.supportSource(), externAlertService);
        }
    }

    @PostMapping("/api/alerts/report/{source}")
    public ResponseEntity<BaseResponse<Void>> receiveExternAlert(@PathVariable(value = "source") String source,
                                                                 @RequestBody String content) {
        log.info("Receive extern alert from source: {}, content: {}", source, content);
        if (StringUtils.isBlank(source)) {
            source = "default";
        }
        ExternAlertService externAlertService = externAlertServiceMap.get(source);
        if (externAlertService != null) {
            try {
                externAlertService.addExternAlert(content);
                return ResponseEntity.ok(BaseResultUtils.success("Add extern alert success"));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(BaseResultUtils.error(
                                "Add extern alert failed: " + e.getMessage()));
            }
        }
        log.warn("Not support extern alert from source: {}", source);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResultUtils.error("Not support the " + source + " source alert"));
    }

    @PostMapping("/api/alerts/report")
    public ResponseEntity<BaseResponse<Void>> receiveDefaultExternAlert(@RequestBody String content) {
        log.info("Receive default extern alert content: {}", content);
        ExternAlertService externAlertService = externAlertServiceMap.get("default");
        if (externAlertService != null) {
            try {
                externAlertService.addExternAlert(content);
                return ResponseEntity.ok(BaseResultUtils.success("Add extern alert success"));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(BaseResultUtils.error(
                                "Add extern alert failed: " + e.getMessage()));
            }
        }
        log.error("Not support default extern alert");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResultUtils.error("Not support the default source alert"));
    }
}
