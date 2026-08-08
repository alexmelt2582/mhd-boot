package com.mhd.alert.service;

import java.util.List;
import java.util.Map;

/**
 * @author zhao-hao-dong
 **/
public interface DataSourceService {
    List<Map<String, Object>> calculate(String datasource, String expr);

    List<Map<String, Object>> query(String datasource, String expr);
    Map<String, Object> getAvailableExecutors();
}
