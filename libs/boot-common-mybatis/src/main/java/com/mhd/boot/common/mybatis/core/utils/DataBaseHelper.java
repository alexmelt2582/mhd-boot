package com.mhd.boot.common.mybatis.core.utils;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.mhd.boot.common.mybatis.core.enums.DataBaseType;
import com.mhd.boot.common.web.utils.SpringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库助手
 *
 * @author zhao-hao-dong
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataBaseHelper {
    private static final DynamicRoutingDataSource DS = SpringUtils.getBean(DynamicRoutingDataSource.class);

    /**
     * 获取当前数据源对应的数据库类型
     * <p>
     * 通过 DynamicRoutingDataSource 获取当前线程绑定的数据源，
     * 然后从数据源获取数据库连接，利用连接的元数据获取数据库产品名称，
     * 最后调用 DataBaseType.find 方法将数据库名称转换为对应的枚举类型
     *
     * @return 当前数据库对应的 DataBaseType 枚举，找不到时默认返回 MY_SQL
     * @throws RuntimeException 当获取数据库连接或元数据出现异常时抛出业务异常
     */
    public static DataBaseType getDataBaseType() {
        DataSource dataSource = DS.determineDataSource();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            return DataBaseType.find(databaseProductName);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取当前加载的数据库名
     */
    public static List<String> getDataSourceNameList() {
        return new ArrayList<>(DS.getDataSources().keySet());
    }
}
