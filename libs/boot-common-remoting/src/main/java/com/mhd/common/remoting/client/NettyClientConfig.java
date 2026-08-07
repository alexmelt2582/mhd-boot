package com.mhd.common.remoting.client;

import lombok.Data;

/**
 * Netty client config
 *
 * @author zhao-hao-dong
 */
@Data
public class NettyClientConfig {
    /**
     * Server host
     */
    private String serverHost;
    /**
     * Server port
     */
    private int serverPort;
    /**
     * Connect timeout in milliseconds. default is 10s
     */
    private int connectTimeoutMillis = 10000;
}
