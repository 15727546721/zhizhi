package cn.xu.interceptor;

import cn.xu.service.statistics.VisitStatisticsService;
import cn.xu.support.util.IpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 访问统计拦截器
 * 
 * <p>记录网站的 UV/PV 数据</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitStatisticsInterceptor implements HandlerInterceptor {

    private final VisitStatisticsService visitStatisticsService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 只统计 GET 请求的页面访问，排除静态资源和API调用
            String method = request.getMethod();
            String uri = request.getRequestURI();
            
            // 排除不需要统计的路径
            if (shouldSkip(uri)) {
                return true;
            }
            
            // 生成访客标识：优先使用用户ID，否则使用IP
            String visitorId = getVisitorId(request);
            
            // 记录访问
            visitStatisticsService.recordVisit(visitorId);
            
        } catch (Exception e) {
            // 统计失败不影响正常请求
            log.debug("访问统计记录失败", e);
        }
        return true;
    }

    /**
     * 判断是否跳过统计
     */
    private boolean shouldSkip(String uri) {
        // 跳过静态资源
        if (uri.contains("/webjars/") || uri.contains("/favicon.ico") || 
            uri.contains("/doc.html") || uri.contains("/swagger") ||
            uri.contains("/v3/api-docs")) {
            return true;
        }
        
        // 跳过健康检查等内部接口
        if (uri.contains("/actuator/") || uri.contains("/health")) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取访客标识
     */
    private String getVisitorId(HttpServletRequest request) {
        // 尝试从请求属性中获取用户ID（如果已登录）
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return "user:" + userId;
        }
        
        // 使用IP + User-Agent 组合作为访客标识
        String ip = IpUtils.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        // 简单hash，避免存储过长的字符串
        int hash = (ip + (userAgent != null ? userAgent : "")).hashCode();
        return "visitor:" + ip + ":" + Math.abs(hash);
    }
}
