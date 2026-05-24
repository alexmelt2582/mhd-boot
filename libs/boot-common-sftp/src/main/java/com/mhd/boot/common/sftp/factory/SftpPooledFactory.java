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
     * 认证失败熔断标记
     * 当用户名密码错误时，避免反复创建连接造成资源浪费
     */
    private volatile boolean authFailed = false;

    /**
     * 认证失败熔断时间戳，单位毫秒
     * 熔断后等待一定时间再尝试重新连接
     */
    private volatile long authFailedTimestamp = 0;

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
        if (authFailed) {
            long elapsed = System.currentTimeMillis() - authFailedTimestamp;
            if (elapsed < AUTH_BREAKER_TIMEOUT) {
                throw new RuntimeException("SFTP认证已熔断，距离下次尝试还有 "
                        + (AUTH_BREAKER_TIMEOUT - elapsed) / 1000 + " 秒，请检查用户名密码配置");
            } else {
                // 熔断时间已过，重置熔断标记，允许重新尝试
                authFailed = false;
                log.info("SFTP认证熔断已解除，准备重新尝试连接");
            }
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
            log.debug("SSH Session已建立，连接到 {}@{}:{}",
                    config.getUsername(), config.getHost(), config.getPort());

            // 步骤6：打开SFTP通道
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            log.debug("SFTP通道已打开");

            return channel;

        } catch (JSchException e) {
            // 判断是否为认证失败，如果是则触发熔断机制
            if (e.getMessage() != null && e.getMessage().contains("Auth fail")) {
                authFailed = true;
                authFailedTimestamp = System.currentTimeMillis();
                log.error("SFTP认证失败，已触发熔断机制，将在5分钟后重试");
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
        try {
            // 执行stat(".")命令，这是一个轻量操作，不会产生大量数据传输
            // 如果连接已断开，此操作会抛出异常
            p.getObject().stat(".");
            return true;
        } catch (Exception e) {
            log.warn("SFTP连接校验失败，连接已失效，将被销毁重建");
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
                log.debug("SFTP通道已关闭");
            }
        } catch (Exception e) {
            log.warn("关闭SFTP通道时发生异常", e);
        }

        try {
            // 步骤2：再关闭SSH Session
            Session session = channel.getSession();
            if (session != null && session.isConnected()) {
                session.disconnect();
                log.debug("SSH Session已关闭");
            }
        } catch (Exception e) {
            log.warn("关闭SSH Session时发生异常", e);
        }
    }

    /**
     * 重置认证熔断状态
     * 当用户修改了密码配置后，可以调用此方法立即解除熔断
     */
    public void resetAuthBreaker() {
        this.authFailed = false;
        this.authFailedTimestamp = 0;
        log.info("SFTP认证熔断状态已手动重置");
    }
}
