package com.mhd.boot.common.sftp.pool;

import com.jcraft.jsch.ChannelSftp;
import com.mhd.boot.common.sftp.config.SftpPoolConfig;
import com.mhd.boot.common.sftp.exception.SftpTransferException;
import com.mhd.boot.common.sftp.factory.SftpPooledFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;

/**
 * SFTP连接池管理器
 * 负责连接池的创建、配置、管理和生命周期控制
 * 提供借出连接、归还连接、废弃连接等核心操作
 *
 * @author zhao-hao-dong
 **/
@Slf4j
public class SftpPoolManager {
    /**
     * Apache Commons Pool2的连接池对象
     */
    private final GenericObjectPool<ChannelSftp> pool;

    /**
     * 连接池配置
     */
    private final SftpPoolConfig config;

    /**
     * 连接池工厂，持有对工厂的引用以便重置熔断状态
     */
    private final SftpPooledFactory factory;

    /**
     * 构造SFTP连接池管理器
     * 根据配置创建连接池，设置各项参数
     *
     * @param config SFTP连接池配置
     */
    public SftpPoolManager(SftpPoolConfig config) {
        this.config = config;
        this.config.valid();
        this.factory = new SftpPooledFactory(config);

        // 创建连接池配置对象
        GenericObjectPoolConfig<ChannelSftp> poolConfig = new GenericObjectPoolConfig<>();

        // 设置连接池容量参数
        poolConfig.setMaxTotal(config.getMaxTotal());       // 最大连接数，防止并发过高时耗尽服务器资源
        poolConfig.setMaxIdle(config.getMaxIdle());         // 最大空闲连接数
        poolConfig.setMinIdle(config.getMinIdle());         // 最小空闲连接数，即使长时间无传输也保留

        // 设置连接校验参数
        poolConfig.setTestOnBorrow(config.isTestOnBorrow());   // 借出前校验，确保拿到的连接可用
        poolConfig.setTestOnReturn(config.isTestOnReturn());   // 归还时校验
        poolConfig.setTestWhileIdle(config.isTestWhileIdle()); // 后台线程定期检测空闲连接

        // 设置空闲连接回收参数
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(config.getTimeBetweenEvictionRunsMillis()));
        poolConfig.setMinEvictableIdleDuration(Duration.ofMillis(config.getMinEvictableIdleTimeMillis()));

        // 设置获取连接的超时时间
        poolConfig.setMaxWait(Duration.ofMillis(config.getMaxWaitMillis()));

        // 创建连接池实例
        this.pool = new GenericObjectPool<>(factory, poolConfig);
    }

    /**
     * 从连接池中借出一个SFTP连接
     * 如果池中有空闲连接且校验通过，直接返回
     * 如果没有空闲连接且未达到最大连接数，创建新连接
     * 如果达到最大连接数，阻塞等待直到有连接归还
     *
     * @return 可用的ChannelSftp连接
     * @throws Exception 如果无法获取连接（如认证失败、网络不通等）
     */
    public ChannelSftp borrow() throws Exception {
        try {
            ChannelSftp channel = pool.borrowObject();
            log.debug("Borrowed SFTP connection from pool. active={}, idle={}", pool.getNumActive(), pool.getNumIdle());
            return channel;
        } catch (java.util.NoSuchElementException e) {
            // 这种情况通常是池子耗尽且超时了
            log.error("Timed out while borrowing SFTP connection. active={}, maxTotal={}", pool.getNumActive(), config.getMaxTotal());
            throw new RuntimeException(new SftpTransferException(
                    SftpTransferException.POOL_EXHAUSTED,
                    "SFTP connection pool is exhausted. Please retry later.",
                    e
            ));
        }
    }

    /**
     * 将使用完毕的SFTP连接归还到连接池
     * 归还后连接可以被其他请求复用
     *
     * @param channel 需要归还的ChannelSftp连接
     */
    public void returnObject(ChannelSftp channel) {
        if (channel != null) {
            pool.returnObject(channel);
            log.debug("Returned SFTP connection to pool. active={}, idle={}",
                    pool.getNumActive(), pool.getNumIdle());
        }
    }

    /**
     * 废弃一个无效的SFTP连接
     * 当连接在传输过程中发生异常时调用，通知连接池销毁此连接
     * 而不是将其归还到池中，避免其他请求拿到失效连接
     *
     * @param channel 需要废弃的ChannelSftp连接
     */
    public void invalidateObject(ChannelSftp channel) {
        if (channel != null) {
            try {
                pool.invalidateObject(channel);
                log.info("Invalidated and destroyed broken SFTP connection.");
            } catch (Exception e) {
                log.warn("Failed to invalidate SFTP connection.", e);
            }
        }
    }

    /**
     * 获取连接池的当前状态信息
     * 用于监控和调试
     *
     * @return 连接池状态字符串
     */
    public String getPoolStatus() {
        return String.format("active=%d, idle=%d, created=%d, destroyed=%d",
                pool.getNumActive(),
                pool.getNumIdle(),
                pool.getCreatedCount(),
                pool.getDestroyedCount());
    }

    /**
     * 获取SFTP配置摘要
     * 用于由上层服务统一输出启动参数日志
     *
     * @return 配置摘要字符串
     */
    public String getConfigSummary() {
        // 返回配置摘要，避免上层直接依赖底层配置对象
        return config.buildStartupConfigSummary();
    }

    /**
     * 重置认证熔断状态
     * 当修改了SFTP密码后调用，立即解除熔断
     */
    public void resetAuthBreaker() {
        factory.resetAuthBreaker();
    }

    /**
     * 关闭连接池，释放所有资源
     * 在应用关闭时调用，确保所有连接都被正确关闭
     */
    public void close() {
        log.info("Closing SFTP connection pool.");
        pool.close();
        log.info("SFTP connection pool closed.");
    }

}
