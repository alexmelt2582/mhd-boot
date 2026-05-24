package com.mhd.boot.common.sftp.pool;

import com.jcraft.jsch.ChannelSftp;
import com.mhd.boot.common.sftp.config.SftpPoolConfig;
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

        // 创建连接池实例
        this.pool = new GenericObjectPool<>(factory, poolConfig);

        log.info("SFTP连接池已初始化，配置：maxTotal={}, maxIdle={}, minIdle={}, host={}:{}",
                config.getMaxTotal(), config.getMaxIdle(), config.getMinIdle(),
                config.getHost(), config.getPort());
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
        ChannelSftp channel = pool.borrowObject();
        log.debug("从连接池借出SFTP连接，当前池状态：活跃={}, 空闲={}",
                pool.getNumActive(), pool.getNumIdle());
        return channel;
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
            log.debug("SFTP连接已归还到连接池，当前池状态：活跃={}, 空闲={}",
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
                log.info("SFTP连接已标记为无效并销毁");
            } catch (Exception e) {
                log.warn("废弃SFTP连接时发生异常", e);
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
        return String.format("活跃连接数: %d, 空闲连接数: %d, 总创建数: %d, 总销毁数: %d",
                pool.getNumActive(),
                pool.getNumIdle(),
                pool.getCreatedCount(),
                pool.getDestroyedCount());
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
        log.info("正在关闭SFTP连接池...");
        pool.close();
        log.info("SFTP连接池已关闭");
    }

}
