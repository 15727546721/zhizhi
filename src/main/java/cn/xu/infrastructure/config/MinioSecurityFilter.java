package cn.xu.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class MinioSecurityFilter extends OncePerRequestFilter {

    @Resource
    private MinioConfig minioConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        
        // 只过滤文件上传相关的请求
        if (requestUri.contains("/upload") || requestUri.contains("/file")) {
            String clientIp = getClientIp(request);
            if (!isAllowedAccess(clientIp)) {
                log.warn("检测到未授权的访问请求 - IP: {}, URI: {}", clientIp, requestUri);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean isAllowedAccess(String ip) {
        // 如果没有配置IP白名单，则允许所有访问
        if (minioConfig.getAllowedIps() == null || minioConfig.getAllowedIps().isEmpty()) {
            return true;
        }

        // 检查是否是内部IP
        if (isInternalIp(ip)) {
            return true;
        }

        // 检查是否在白名单中
        return minioConfig.getAllowedIps().contains(ip);
    }

    private boolean isInternalIp(String ip) {
        return ip.startsWith("10.") ||
               ip.startsWith("192.168.") ||
               ip.startsWith("172.16.") ||
               ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") ||
               ip.startsWith("172.19.") ||
               ip.startsWith("172.20.") ||
               ip.startsWith("172.21.") ||
               ip.startsWith("172.22.") ||
               ip.startsWith("172.23.") ||
               ip.startsWith("172.24.") ||
               ip.startsWith("172.25.") ||
               ip.startsWith("172.26.") ||
               ip.startsWith("172.27.") ||
               ip.startsWith("172.28.") ||
               ip.startsWith("172.29.") ||
               ip.startsWith("172.30.") ||
               ip.startsWith("172.31.");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 