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
     * SFTP服务器主机地址
     */
    private String host;

    /**
     * SFTP服务器端口，默认22
     */
    private int port = 22;

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
    private int timeout = 30000;

    /**
     * 最大连接数，即连接池中最多能同时存在的连接数，默认 10
     */
    private int maxTotal = 10;

    /**
     * 最大空闲连接数，连接池中最多允许保留的空闲连接数，默认 5
     */
    private int maxIdle = 5;

    /**
     * 最小空闲连接数，连接池中始终保持的最少空闲连接数，默认 2
     * 即使长时间没有传输任务，也会保留这个数量的连接
     */
    private int minIdle = 2;

    /**
     * 借出连接前是否校验连接有效性，默认 true
     * 设置为true可以确保调用方拿到的连接一定是可用的
     */
    private boolean testOnBorrow = true;

    /**
     * 归还连接时是否校验连接有效性，默认 true
     */
    private boolean testOnReturn = true;

    /**
     * 后台线程是否定期检测空闲连接的有效性，默认 true
     */
    private boolean testWhileIdle = true;

    /**
     * 后台检测线程的运行间隔，单位毫秒，默认60秒
     */
    private long timeBetweenEvictionRunsMillis = 60000;

    /**
     * 空闲连接被回收的最小空闲时间，单位毫秒，默认5分钟
     */
    private long minEvictableIdleTimeMillis = 300000;

    /**
     * 服务器保活心跳间隔，单位毫秒，默认30秒
     * 防止长时间空闲导致服务器或中间设备断开连接
     */
    private int serverAliveInterval = 30000;

    /**
     * 服务器保活心跳最大失败次数，默认3次
     */
    private int serverAliveCountMax = 3;
}
