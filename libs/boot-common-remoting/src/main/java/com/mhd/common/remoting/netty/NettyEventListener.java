package com.mhd.common.remoting.netty;

import io.netty.channel.Channel;

/**
 * Netty 事件监听器
 *
 * @author zhao-hao-dong
 */
public interface NettyEventListener {
    /**
     * 当 channel 连接激活时触发
     *
     * @param channel netty channel
     */
    default void onChannelActive(final Channel channel) throws Exception {
    }

    /**
     * 当 channel 空闲时触发
     *
     * @param channel netty channel
     */
    default void onChannelIdle(final Channel channel) throws Exception {
    }
}
