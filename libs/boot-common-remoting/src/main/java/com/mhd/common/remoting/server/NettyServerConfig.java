package com.mhd.common.remoting.server;

import lombok.Data;

/**
 * Netty 服务端配置
 *
 * @author zhao-hao-dong
 */
@Data
public class NettyServerConfig {
    /**
     * Netty 服务端端口
     */
    private Integer port;
    /**
     * Netty 服务端 空闲检测事件触发时间，默认100秒
     */
    private Integer idleStateEventTriggerTime = 100;
}
