package com.mhd.alert.extern.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AlertManagerExternAlert
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertManagerExternAlert {
    
    private String groupKey;
    
    private String externalURL;
    
    private String version;
    
    private String status;
    
    private Map<String, String> groupLabels;
    
    private Map<String, String> commonLabels;
    
    private Map<String, String> commonAnnotations;
    
    private List<PrometheusExternAlert> alerts;
}
