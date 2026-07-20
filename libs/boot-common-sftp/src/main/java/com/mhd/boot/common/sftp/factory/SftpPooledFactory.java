package com.mhd.boot.common.sftp.factory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mhd.boot.common.sftp.config.SftpPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 * SFTP连接池对象工厂
 * 负责创建、验证、销毁和包装SFTP连接对象
 * 继承自Apache Commons Pool2的BasePooledObjectFactory，实现连接生命周期管理
 *
 * @author zhao-hao-dong
 **/
@Slf4j
public class SftpPooledFactory extends BasePooledObjectFactory<ChannelSftp> {
    /**
     * SFTP服务器配置
     */
    private final SftpPoolConfig config;

    /**
     * 认证熔断截止时间戳，单位毫秒
     * 当前时间小于该值时，连接创建会被快速失败
     */
    private volatile long authBlockedUntilTimestamp = 0L;

    /**
     * 熔断等待时间，默认5分钟
     */
    private static final long AUTH_BREAKER_TIMEOUT = 5 * 60 * 1000L;

    /**
     * 构造SFTP连接池工厂
     *
     * @param config SFTP服务器配置，包含主机、端口、用户名、密码等信息
     */
    public SftpPooledFactory(SftpPoolConfig config) {
        this.config = config;
    }

    /**
     * 创建一个新的SFTP连接
     * 包含完整的连接建立流程：创建Session、配置参数、建立连接、打开SFTP通道
     *
     * @return 新创建的ChannelSftp对象
     * @throws Exception 如果连接创建失败，抛出异常
     */
    @Override
    public ChannelSftp create() throws Exception {
        // 检查认证熔断状态，如果处于熔断期则直接抛出异常，避免重复尝试
        long now = System.currentTimeMillis();
        long blockedUntil = authBlockedUntilTimestamp;
        if (blockedUntil > now) {
            long waitSeconds = (blockedUntil - now) / 1000;
                throw new RuntimeException("SFTP authentication circuit breaker is active. "
                        + "Next attempt available in " + waitSeconds
                        + " seconds. Please verify username and password configuration.");
        }
        if (blockedUntil > 0L) {
            // 熔断时间已过，重置熔断截止时间，允许重新尝试
            authBlockedUntilTimestamp = 0L;
            log.info("SFTP authentication circuit breaker has been reset. Preparing to retry connection.");
        }

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;

        try {
            // 步骤1：创建SSH Session对象，指定用户名、主机和端口
            session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
            session.setPassword(config.getPassword().getBytes(StandardCharsets.UTF_8));

            // 步骤2：配置Session属性，关闭主机密钥检查（生产环境建议使用证书认证）
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);

            // 步骤3：设置连接超时时间，防止网络不通时线程长时间阻塞
            session.setTimeout(config.getTimeout());

            // 步骤4：启用服务器保活机制，定期发送心跳包
            // 防止长时间空闲导致服务器或中间防火墙断开连接
            session.setServerAliveInterval(config.getServerAliveInterval());
            session.setServerAliveCountMax(config.getServerAliveCountMax());

            // 步骤5：建立SSH连接
            session.connect();
                log.debug("SSH session connected: {}@{}:{}",
                    config.getUsername(), config.getHost(), config.getPort());

            // 步骤6：打开SFTP通道
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
                log.debug("SFTP channel opened.");

            return channel;

        } catch (JSchException e) {
            // 判断是否为认证失败，如果是则触发熔断机制
            if (e.getMessage() != null && e.getMessage().contains("Auth fail")) {
                authBlockedUntilTimestamp = System.currentTimeMillis() + AUTH_BREAKER_TIMEOUT;
                log.error("SFTP authentication failed. Auth circuit breaker enabled for 300 seconds.");
            }
            // 清理已创建的部分资源
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            throw e;
        }
    }

    /**
     * 包装SFTP连接对象为Pool2所需的PooledObject
     *
     * @param channelSftp 需要包装的ChannelSftp对象
     * @return 包装后的PooledObject
     */
    @Override
    public PooledObject<ChannelSftp> wrap(ChannelSftp channelSftp) {
        return new DefaultPooledObject<>(channelSftp);
    }

    /**
     * 校验SFTP连接是否仍然有效
     * 通过执行一个轻量级的stat命令来检测连接状态
     *
     * @param p 需要校验的连接对象
     * @return true表示连接有效，false表示连接已失效
     */
    @Override
    public boolean validateObject(PooledObject<ChannelSftp> p) {
        ChannelSftp channel = p.getObject();
        if (channel == null || !channel.isConnected()) {
            return false;
        }
        try {
            // stat(".")命令，这是一个轻量操作，不会产生大量数据传输，此处不适用，如果用户cd 到一个不存在的目录就会出现问题
            channel.pwd();
            return true;
        } catch (Exception e) {
            log.warn("SFTP connection validation failed. Connection will be destroyed and recreated.");
            return false;
        }
    }

    /**
     * 销毁SFTP连接，释放所有资源
     * 遵循先关闭Channel再关闭Session的顺序，确保资源完全释放
     *
     * @param p 需要销毁的连接对象
     */
    @Override
    public void destroyObject(PooledObject<ChannelSftp> p) {
        ChannelSftp channel = p.getObject();
        if (Objects.isNull(channel)) return;
        try {
            // 步骤1：先关闭SFTP通道
            if (channel.isConnected()) {
                channel.disconnect();
                log.debug("SFTP channel closed.");
            }
        } catch (Exception e) {
            log.warn("Failed to close SFTP channel.", e);
        }

        try {
            // 步骤2：再关闭SSH Session
            Session session = channel.getSession();
            if (session != null && session.isConnected()) {
                session.disconnect();
                log.debug("SSH session closed.");
            }
        } catch (Exception e) {
            log.warn("Failed to close SSH session.", e);
        }
    }

    /**
     * 重置认证熔断状态
     * 当用户修改了密码配置后，可以调用此方法立即解除熔断
     */
    public void resetAuthBreaker() {
        this.authBlockedUntilTimestamp = 0L;
        log.info("SFTP auth circuit breaker reset manually.");
    }
}
