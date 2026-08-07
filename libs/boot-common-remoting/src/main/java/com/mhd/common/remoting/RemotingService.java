package com.mhd.common.remoting;

/**
 * 远程调用接口
 * 派生自 Apache RocketMQ 的接口 org.apache.rocketmq.remoting.RemotingService
 *
 * @author zhao-hao-dong
 * @see <a href="https://github.com/apache/rocketmq/blob/develop/remoting/src/main/java/org/apache/rocketmq/remoting/RemotingService.java">RemotingService</a>
 */
public interface RemotingService {
    /**
     * 启动服务
     */
    void start();

    /**
     * 关闭服务
     */
    void shutdown();

    /**
     * 是否启动服务
     *
     * @return true表示已启动，false表示未启动
     */
    boolean isStart();
}
