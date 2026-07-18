package com.mhd.boot.common.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Network 工具类
 *
 * @author zhao-hao-dong
 * @see <a href="https://github.com/apache/rocketmq/blob/develop/common/src/main/java/org/apache/rocketmq/common/utils/NetworkUtil.java">NettyRemotingAbstract</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NetworkUtils {
    /**
     * 操作系统名称
     */
    public static final String OS_NAME = System.getProperty("os.name");
    /**
     * 是否是 Linux 平台
     */
    @Getter
    private static boolean isLinuxPlatform = false;
    /**
     * 是否是 Windows 平台
     */
    @Getter
    private static boolean isWindowsPlatform = false;

    static {
        // 检测操作系统类型
        if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
            isLinuxPlatform = true;
        }

        if (OS_NAME != null && OS_NAME.toLowerCase().contains("windows")) {
            isWindowsPlatform = true;
        }
    }
}
