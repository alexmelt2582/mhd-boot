package com.mhd.boot.common.utils;

import com.mhd.boot.common.constant.NetworkConstants;
import com.mhd.boot.common.constant.SignConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.util.InetAddressUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author zhao-hao-dong
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IpDomainUtil {
    private static final Pattern DOMAIN_PATTERN =
            Pattern.compile("^[-\\w]+(\\.[-\\w]+)*$");
    private static final String LOCALHOST = "localhost";
    /**
     * HTTP header schema.
     */
    private static final Pattern DOMAIN_SCHEMA = Pattern.compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://){1}[^\\s]*");

    /**
     * 判断是否是 ip 或者 domain.
     *
     * @param ipDomain 字符串
     * @return true-是 false-否
     */
    public static boolean validateIpDomain(String ipDomain) {
        if (ipDomain == null || ipDomain.trim().isEmpty()) {
            return false;
        }
        ipDomain = ipDomain.trim();
        if (LOCALHOST.equalsIgnoreCase(ipDomain)) {
            return true;
        }
        if (InetAddressUtils.isIPv4Address(ipDomain)) {
            return true;
        }
        if (InetAddressUtils.isIPv6Address(ipDomain)) {
            return true;
        }
        return DOMAIN_PATTERN.matcher(ipDomain).matches();
    }

    /**
     * 判断传入的域名或 IP 地址字符串是否带有 http 或 https 协议头
     * @param domainIp host
     * @return true or false
     */
    public static boolean isHasSchema(String domainIp) {
        if (domainIp == null || domainIp.trim().isEmpty()) {
            return false;
        }
        return DOMAIN_SCHEMA.matcher(domainIp).matches();
    }

    /**
     * 判断传入的实例地址字符串（格式通常为 ip:port）是否包含端口号
     * @param instance instance ip:port
     * @return true if has
     */
    public static boolean isHasPortWithMark(String instance) {
        if (instance == null || instance.trim().isEmpty()) {
            return false;
        }
        String[] parts = instance.split(SignConstants.DOUBLE_MARK);
        if (parts.length >= 2) {
            String port = parts[parts.length - 1];
            return CommonUtils.isNumeric(port);
        }
        return false;
    }

    /**
     * 获取当前IP
     *
     * @return ip
     */
    public static String getLocalhostIp() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (!netInterface.isLoopback() && !netInterface.isVirtual() && netInterface.isUp()) {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    /**
     * 查看IP地址类型
     *
     * @param ipDomain IP地址
     * @return IP地址类型
     */
    public static String checkIpAddressType(String ipDomain) {
        if (ipDomain != null && !ipDomain.trim().isEmpty() && InetAddressUtils.isIPv6Address(ipDomain)) {
            return NetworkConstants.IPV6;
        }
        return NetworkConstants.IPV4;
    }

    /**
     * 获取当前的主机名
     *
     * @return 主机名
     */
    public static String getCurrentHostName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * 判断端口是否合法
     *
     * @param portStr 端口字符串
     * @return true-合法 false-不合法
     */
    public static boolean validPort(String portStr) {
        if (portStr == null || portStr.trim().isEmpty()) {
            return false;
        }
        try {
            int port = Integer.parseInt(portStr);
            return port >= 0 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取当前机器的 IP
     *
     * @return IP
     */
    public static String getLocalIp() {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                NetworkInterface anInterface = interfaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration<InetAddress> inetAddresses = anInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddresses.nextElement();
                    // 排除loopback类型地址
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr.getHostAddress();
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress.getHostAddress();
            }
            // 如果没有发现 non-loopback 地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                return "";
            }
            return jdkSuppliedAddress.getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

}
