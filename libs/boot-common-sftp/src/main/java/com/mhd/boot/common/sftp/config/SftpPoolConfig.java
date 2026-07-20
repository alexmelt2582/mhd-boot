package com.mhd.boot.common.sftp.config;

import lombok.Data;

/**
 * SFTP连接池配置参数
 * 集中管理所有与连接池相关的配置项，方便统一修改和维护
 *
 * @author zhao-hao-dong
 **/
@Data
public class SftpPoolConfig {
    /**
     * 默认SFTP端口
     */
    public static final int DEFAULT_PORT = 22;

    /**
     * 默认连接超时时间（毫秒）
     */
    public static final int DEFAULT_TIMEOUT = 30000;

    /**
     * 默认最大连接数
     */
    public static final int DEFAULT_MAX_TOTAL = 10;

    /**
     * 默认最大空闲连接数
     */
    public static final int DEFAULT_MAX_IDLE = 5;

    /**
     * 默认最小空闲连接数
     */
    public static final int DEFAULT_MIN_IDLE = 2;

    /**
     * 默认借出校验开关
     */
    public static final boolean DEFAULT_TEST_ON_BORROW = true;

    /**
     * 默认归还校验开关
     */
    public static final boolean DEFAULT_TEST_ON_RETURN = true;

    /**
     * 默认空闲检测开关
     */
    public static final boolean DEFAULT_TEST_WHILE_IDLE = true;

    /**
     * 默认空闲检测间隔（毫秒）
     */
    public static final long DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 60000L;

    /**
     * 默认最小可回收空闲时间（毫秒）
     */
    public static final long DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS = 300000L;

    /**
     * 默认获取连接最大等待时间（毫秒）
     */
    public static final long DEFAULT_MAX_WAIT_MILLIS = 10000L;

    /**
     * 默认服务器心跳间隔（毫秒）
     */
    public static final int DEFAULT_SERVER_ALIVE_INTERVAL = 30000;

    /**
     * 默认服务器心跳最大失败次数
     */
    public static final int DEFAULT_SERVER_ALIVE_COUNT_MAX = 3;

    /**
     * SFTP服务器主机地址
     */
    private String host;

    /**
     * SFTP服务器端口，默认22
     */
    private int port = DEFAULT_PORT;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 连接超时时间，单位毫秒，默认30秒
     */
    private int timeout = DEFAULT_TIMEOUT;

    /**
     * 最大连接数，即连接池中最多能同时存在的连接数，默认 10
     */
    private int maxTotal = DEFAULT_MAX_TOTAL;

    /**
     * 最大空闲连接数，连接池中最多允许保留的空闲连接数，默认 5
     */
    private int maxIdle = DEFAULT_MAX_IDLE;

    /**
     * 最小空闲连接数，连接池中始终保持的最少空闲连接数，默认 2
     * 即使长时间没有传输任务，也会保留这个数量的连接
     */
    private int minIdle = DEFAULT_MIN_IDLE;

    /**
     * 借出连接前是否校验连接有效性，默认 true
     * 设置为true可以确保调用方拿到的连接一定是可用的
     */
    private boolean testOnBorrow = DEFAULT_TEST_ON_BORROW;

    /**
     * 归还连接时是否校验连接有效性，默认 true
     */
    private boolean testOnReturn = DEFAULT_TEST_ON_RETURN;

    /**
     * 后台线程是否定期检测空闲连接的有效性，默认 true
     */
    private boolean testWhileIdle = DEFAULT_TEST_WHILE_IDLE;

    /**
     * 后台检测线程的运行间隔，单位毫秒，默认60秒
     */
    private long timeBetweenEvictionRunsMillis = DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;

    /**
     * 空闲连接被回收的最小空闲时间，单位毫秒，默认5分钟
     */
    private long minEvictableIdleTimeMillis = DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

    /**
     * 从池中借出连接的最大等待时间，单位毫秒，默认 10秒
     * 如果超过这个时间还没有空闲连接，抛出异常，防止线程无限阻塞
     */
    private long maxWaitMillis = DEFAULT_MAX_WAIT_MILLIS;

    /**
     * 服务器保活心跳间隔，单位毫秒，默认30秒
     * 防止长时间空闲导致服务器或中间设备断开连接
     */
    private int serverAliveInterval = DEFAULT_SERVER_ALIVE_INTERVAL;

    /**
     * 服务器保活心跳最大失败次数，默认3次
     */
    private int serverAliveCountMax = DEFAULT_SERVER_ALIVE_COUNT_MAX;

    /**
     * 校验配置参数是否合法
     * 在构建连接池前调用，避免运行时才暴露配置错误
     *
     * @throws IllegalStateException 当配置不合法时抛出
     */
    public void valid() {
        // 校验必填字符串参数
        requireNotBlank(host, "host");
        requireNotBlank(username, "username");
        requireNotBlank(password, "password");

        // 校验端口范围
        if (port <= 0 || port > 65535) {
            throw new IllegalStateException("Invalid SFTP config: port must be between 1 and 65535.");
        }

        // 校验连接与池化参数
        requirePositive(timeout, "timeout");
        requirePositive(maxTotal, "maxTotal");
        requireNonNegative(maxIdle, "maxIdle");
        requireNonNegative(minIdle, "minIdle");
        requirePositive(timeBetweenEvictionRunsMillis, "timeBetweenEvictionRunsMillis");
        requireNonNegative(minEvictableIdleTimeMillis, "minEvictableIdleTimeMillis");
        requirePositive(maxWaitMillis, "maxWaitMillis");
        requirePositive(serverAliveInterval, "serverAliveInterval");
        requireNonNegative(serverAliveCountMax, "serverAliveCountMax");

        // 校验连接池容量关系
        if (maxIdle > maxTotal) {
            throw new IllegalStateException("Invalid SFTP config: maxIdle must be less than or equal to maxTotal.");
        }
        if (minIdle > maxIdle) {
            throw new IllegalStateException("Invalid SFTP config: minIdle must be less than or equal to maxIdle.");
        }
    }

    /**
     * 构建启动日志摘要
     * 用于在模块初始化时清晰展示配置项及默认值使用情况
     *
     * @return 多行配置摘要字符串
     */
    public String buildStartupConfigSummary() {
        // 汇总关键配置并标注是否使用默认值
        StringBuilder builder = new StringBuilder();
        builder.append("SFTP module configuration summary\n")
                .append("- targetHost: ").append(host).append('\n')
                .append("- port: ").append(port).append(markDefault(port, DEFAULT_PORT)).append('\n')
                .append("- username: ").append(username).append('\n')
                .append("- password: ").append(maskPassword(password)).append('\n')
                .append("- timeout(ms): ").append(timeout).append(markDefault(timeout, DEFAULT_TIMEOUT)).append('\n')
                .append("- maxTotal: ").append(maxTotal).append(markDefault(maxTotal, DEFAULT_MAX_TOTAL)).append('\n')
                .append("- maxIdle: ").append(maxIdle).append(markDefault(maxIdle, DEFAULT_MAX_IDLE)).append('\n')
                .append("- minIdle: ").append(minIdle).append(markDefault(minIdle, DEFAULT_MIN_IDLE)).append('\n')
                .append("- testOnBorrow: ").append(testOnBorrow).append(markDefault(testOnBorrow, DEFAULT_TEST_ON_BORROW)).append('\n')
                .append("- testOnReturn: ").append(testOnReturn).append(markDefault(testOnReturn, DEFAULT_TEST_ON_RETURN)).append('\n')
                .append("- testWhileIdle: ").append(testWhileIdle).append(markDefault(testWhileIdle, DEFAULT_TEST_WHILE_IDLE)).append('\n')
                .append("- timeBetweenEvictionRunsMillis: ").append(timeBetweenEvictionRunsMillis)
                .append(markDefault(timeBetweenEvictionRunsMillis, DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS)).append('\n')
                .append("- minEvictableIdleTimeMillis: ").append(minEvictableIdleTimeMillis)
                .append(markDefault(minEvictableIdleTimeMillis, DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS)).append('\n')
                .append("- maxWaitMillis: ").append(maxWaitMillis).append(markDefault(maxWaitMillis, DEFAULT_MAX_WAIT_MILLIS)).append('\n')
                .append("- serverAliveInterval: ").append(serverAliveInterval)
                .append(markDefault(serverAliveInterval, DEFAULT_SERVER_ALIVE_INTERVAL)).append('\n')
                .append("- serverAliveCountMax: ").append(serverAliveCountMax)
                .append(markDefault(serverAliveCountMax, DEFAULT_SERVER_ALIVE_COUNT_MAX));
        return builder.toString();
    }

    /**
     * 校验字符串参数非空白
     *
     * @param value 参数值
     * @param name  参数名
     */
    private void requireNotBlank(String value, String name) {
        // 使用trim后长度判断，避免只传空格绕过校验
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Invalid SFTP config: " + name + " must not be blank.");
        }
    }

    /**
     * 校验数值参数为正数
     *
     * @param value 参数值
     * @param name  参数名
     */
    private void requirePositive(long value, String name) {
        // 正数校验用于超时、容量等必须大于0的参数
        if (value <= 0) {
            throw new IllegalStateException("Invalid SFTP config: " + name + " must be greater than 0.");
        }
    }

    /**
     * 校验数值参数为非负数
     *
     * @param value 参数值
     * @param name  参数名
     */
    private void requireNonNegative(long value, String name) {
        // 非负校验用于可为0的参数
        if (value < 0) {
            throw new IllegalStateException("Invalid SFTP config: " + name + " must be greater than or equal to 0.");
        }
    }

    /**
     * 标注配置是否使用默认值
     *
     * @param value        当前值
     * @param defaultValue 默认值
     * @return 标识字符串
     */
    private String markDefault(Object value, Object defaultValue) {
        // 当当前值与默认值一致时标记为default，便于启动时快速识别
        return value != null && value.equals(defaultValue) ? " (default)" : " (custom)";
    }

    /**
     * 对密码进行脱敏展示
     *
     * @param rawPassword 原始密码
     * @return 脱敏后的密码
     */
    private String maskPassword(String rawPassword) {
        // 避免在日志中输出明文密码
        if (rawPassword == null || rawPassword.isEmpty()) {
            return "<empty>";
        }
        return "******";
    }
}
