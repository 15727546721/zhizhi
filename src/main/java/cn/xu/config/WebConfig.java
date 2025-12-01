package cn.xu.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置允许的跨域请求
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:*", 
                        "https://*.zhizhi.cn", 
                        "http://127.0.0.1:*",
                        "http://localhost:517[0-9]",  // Vite端口范围 5170-5179
                        "http://127.0.0.1:517[0-9]"   // Vite端口范围 5170-5179
                )  // 允许的域名模式
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的请求方法
                .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With")  // 允许的请求头
                .allowCredentials(true)  // 允许发送Cookie
                .maxAge(3600);  // 预检请求的有效期，单位为秒
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 后台管理登录
                        "/api/system/login", 
                        "/api/system/logout",
                        // 前端用户登录注册
                        "/api/user/login",
                        "/api/user/register",
                        // 公开API（首页、帖子列表、标签等）
                        "/api/home/**",
                        "/api/post/list",
                        "/api/post/detail/**",
                        "/api/post/search/**",
                        "/api/tag/**",
                        "/api/ranking/**",
                        // 文件访问
                        "/api/file/**",
                        // 文档
                        "/doc.html", 
                        "/webjars/**",
                        "/favicon.ico",
                        "/error",
                        "/swagger-resources/**",
                        "/v3/api-docs/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
    }
}