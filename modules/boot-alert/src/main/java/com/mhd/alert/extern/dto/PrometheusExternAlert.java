package com.mhd.alert.extern.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;


/**
 * Prometheus Alert Content Entity
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrometheusExternAlert {
    
    private Map<String, String> labels;
    
    private Map<String, String> annotations;

    private String status;

    private Instant startsAt;

    private Instant endsAt;
    
    private String generatorURL;
}
