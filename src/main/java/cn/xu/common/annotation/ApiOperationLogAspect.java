package cn.xu.common.annotation;

import cn.xu.support.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * API操作日志切面
 * 用于处理带有 @ApiOperationLog 注解的方法，记录请求的相关信息
 *
 */
@Aspect
@Slf4j
@Component
public class ApiOperationLogAspect {

    /** 定义切点，匹配所有带有 @ApiOperationLog 注解的方法 */
    @Pointcut("@annotation(cn.xu.common.annotation.ApiOperationLog)")
    public void apiOperationLog() {}

    /**
     * 环绕通知：记录请求的详细信息并执行目标方法
     * @param joinPoint 连接点，表示当前被拦截的方法
     * @return 方法的执行结果
     * @throws Throwable 异常
     */
    @Around("apiOperationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录请求开始时间
        long startTime = System.nanoTime();

        // 获取类名和方法名
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 格式化请求参数为 JSON 字符串
        String argsJsonStr = formatArgsToJson(joinPoint.getArgs());

        // 获取 API 操作日志的描述
        String description = getApiOperationLogDescription(joinPoint);

        // 打印请求日志
        log.info("====== 请求开始: [{}], 方法: {}, 请求参数: {}, 请求方法: {} =================================== ",
                description, argsJsonStr, className, methodName);

        // 执行目标方法
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("请求方法执行异常: [{}] - {}.{}: {}", description, className, methodName, throwable.getMessage());
            throw throwable; // 重新抛出异常
        }

        // 计算方法执行时间
        long executionTime = (System.nanoTime() - startTime) / 1000000; // 毫秒

        // 打印返回结果日志
        log.info("====== 请求结束: [{}], 执行时间: {}ms, 返回结果: {} =================================== ",
                description, executionTime, JsonUtils.toJsonString(result));

        return result;
    }

    /**
     * 获取方法的 @ApiOperationLog 注解中的 description
     * @param joinPoint 连接点
     * @return description 描述信息
     */
    private String getApiOperationLogDescription(ProceedingJoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 获取方法对象
        Method method = signature.getMethod();

        // 获取方法上的 @ApiOperationLog 注解
        ApiOperationLog apiOperationLog = method.getAnnotation(ApiOperationLog.class);

        // 返回描述信息
        if (apiOperationLog != null) {
            return apiOperationLog.description();
        }
        return "";
    }

    /**
     * 格式化方法参数为 JSON 字符串
     * @param args 方法参数数组
     * @return JSON 字符串
     */
    private String formatArgsToJson(Object[] args) {
        return Arrays.stream(args)
                .map(this::toJsonStr)
                .collect(Collectors.joining(", "));
    }

    /**
     * 将对象转换为 JSON 字符串
     * @param object 要转换的对象
     * @return JSON 字符串
     */
    private String toJsonStr(Object object) {
        // 处理 MultipartFile 数组
        if (object instanceof MultipartFile[]) {
            MultipartFile[] files = (MultipartFile[]) object;
            String filesInfo = Arrays.stream(files)
                    .map(file -> String.format("{\"fileName\":\"%s\", \"size\":%d}",
                            file.getOriginalFilename(), file.getSize()))
                    .collect(Collectors.joining(", "));
            return "[" + filesInfo + "]";
        }

        // 处理单个 MultipartFile
        if (object instanceof MultipartFile) {
            MultipartFile file = (MultipartFile) object;
            return String.format("{\"fileName\":\"%s\", \"size\":%d}", file.getOriginalFilename(), file.getSize());
        }

        // 处理 HttpServletRequest 或 HttpServletResponse 对象
        if (object instanceof HttpServletRequest || object instanceof HttpServletResponse || object instanceof org.apache.catalina.connector.RequestFacade) {
            return "{\"filtered\":\"request/response object\"}";
        }

        // 将其他对象转换为 JSON
        try {
            return JsonUtils.toJsonString(object);
        } catch (Exception e) {
            log.warn("无法将对象转为 JSON 字符串: {}", object.getClass().getSimpleName(), e);
            return "{}"; // 如果转换失败，返回空 JSON 对象
        }
    }
}
