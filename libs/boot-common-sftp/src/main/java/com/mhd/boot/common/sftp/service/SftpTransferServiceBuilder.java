package com.mhd.boot.common.sftp.service;

import com.mhd.boot.common.sftp.config.SftpPoolConfig;
import com.mhd.boot.common.sftp.pool.SftpPoolManager;

/**
 * SFTP传输服务构建器
 * 使用建造者模式简化SftpTransferService的创建过程
 * 提供链式调用和默认值
 *
 * @author zhao-hao-dong
 **/
public class SftpTransferServiceBuilder {
    /** SFTP连接池配置 */
    private final SftpPoolConfig config;

    /**
     * 创建构建器实例
     */
    public SftpTransferServiceBuilder() {
        this.config = new SftpPoolConfig();
    }

    /**
     * 设置SFTP服务器主机地址
     *
     * @param host 主机地址
     * @return 构建器实例
     */
    public SftpTransferServiceBuilder host(String host) {
        config.setHost(host);
        return this;
    }

    /**
     * 设置SFTP服务器端口
     *
     * @param port 端口号，默认22
     * @return 构建器实例
     */
    public SftpTransferServiceBuilder port(int port) {
        config.setPort(port);
        return this;
    }

    /**
     * 设置登录用户名
     *
     * @param username 用户名
     * @return 构建器实例
     */
    public SftpTransferServiceBuilder username(String username) {
        config.setUsername(username);
        return this;
    }

    /**
     * 设置登录密码
     *
     * @param password 密码
     * @return 构建器实例
     */
    public SftpTransferServiceBuilder password(String password) {
        config.setPassword(password);
        return this;
    }

    /**
     * 设置连接超时时间
     *
     * @param timeoutMillis 超时时间，单位毫秒
     * @return 构建器实例
     */
    public SftpTransferServiceBuilder timeout(int timeoutMillis) {
        config.setTimeout(timeoutMillis);
        return this;
    }

    /**
     * 设置最大连接数
     *
     * @param maxTotal 最大连接数
     * @return 构建器实例
     */
    public SftpTransferServiceBuilder maxTotal(int maxTotal) {
        config.setMaxTotal(maxTotal);
        return this;
    }

    /**
     * 设置最小空闲连接数
     *
     * @param minIdle 最小空闲连接数
     * @return 构建器实例
     */
    public SftpTransferServiceBuilder minIdle(int minIdle) {
        config.setMinIdle(minIdle);
        return this;
    }

    /**
     * 构建SftpTransferService实例
     *
     * @return 配置完成的SftpTransferService
     * @throws IllegalStateException 如果必要参数未设置
     */
    public SftpTransferService build() {
        // 校验必要参数
        if (config.getHost() == null || config.getHost().isEmpty()) {
            throw new IllegalStateException("SFTP主机地址不能为空");
        }
        if (config.getUsername() == null || config.getUsername().isEmpty()) {
            throw new IllegalStateException("SFTP用户名不能为空");
        }
        if (config.getPassword() == null || config.getPassword().isEmpty()) {
            throw new IllegalStateException("SFTP密码不能为空");
        }
        SftpPoolManager poolManager = new SftpPoolManager(config);
        return new SftpTransferService(poolManager);
    }
}
