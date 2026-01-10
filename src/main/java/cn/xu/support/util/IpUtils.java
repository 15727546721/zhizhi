package cn.xu.support.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 * <p>提供从HTTP请求中获取客户端真实IP的功能</p>
 * <p>支持通过代理服务器获取真实IP</p>

 */
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    
    /** 本地IP */
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端真实IP地址
     * 
     * <p>按以下顺序尝试获取：
     * <ol>
     *   <li>X-Forwarded-For</li>
     *   <li>Proxy-Client-IP</li>
     *   <li>WL-Proxy-Client-IP</li>
     *   <li>HTTP_CLIENT_IP</li>
     *   <li>HTTP_X_FORWARDED_FOR</li>
     *   <li>X-Real-IP</li>
     *   <li>RemoteAddr</li>
     * </ol>
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index).trim();
            }
            return ip;
        }
        
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }
        
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        ip = request.getRemoteAddr();
        
        // 处理IPv6本地地址
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }
        
        return ip;
    }

    /**
     * 检查IP是否有效
     * 
     * @param ip IP地址
     * @return true-有效，false-无效
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 检查是否为内网IP
     * 
     * @param ip IP地址
     * @return true-内网IP，false-外网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // localhost
        if (LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return true;
        }
        
        // 私有IP段
        // 10.0.0.0 - 10.255.255.255
        // 172.16.0.0 - 172.31.255.255
        // 192.168.0.0 - 192.168.255.255
        return ip.startsWith("10.") || 
               ip.startsWith("192.168.") || 
               (ip.startsWith("172.") && isIn172PrivateRange(ip));
    }

    /**
     * 检查172开头的IP是否在私有范围内
     */
    private static boolean isIn172PrivateRange(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                int second = Integer.parseInt(parts[1]);
                return second >= 16 && second <= 31;
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return false;
    }
}