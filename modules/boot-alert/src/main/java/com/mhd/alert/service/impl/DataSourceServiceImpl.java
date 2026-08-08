package com.mhd.alert.service.impl;

import com.mhd.alert.service.DataSourceService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author zhao-hao-dong
 **/
@Component
public class DataSourceServiceImpl implements DataSourceService {
    @Override
    public List<Map<String, Object>> calculate(String datasource, String expr) {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> query(String datasource, String expr) {
        return List.of();
    }

    @Override
    public Map<String, Object> getAvailableExecutors() {
        return Map.of();
    }
}
