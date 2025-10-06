package cn.xu.common.annotation;

import cn.xu.common.utils.JsonUtils;
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

@Aspect
@Slf4j
@Component
public class ApiOperationLogAspect {

    /** 以自定义 @ApiOperationLog 注解为切点，凡是添加 @ApiOperationLog 的方法，都会执行环绕中的代码 */
    @Pointcut("@annotation(cn.xu.common.annotation.ApiOperationLog)")
    public void apiOperationLog() {}

    /**
     * 环绕通知，记录方法执行过程中的请求、响应及相关信息
     * @param joinPoint 连接点对象
     * @return 方法的返回结果
     * @throws Throwable 异常
     */
    @Around("apiOperationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 请求开始时间
        long startTime = System.nanoTime();

        // 获取被请求的类和方法
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 获取请求入参，并将其转化为 JSON 字符串
        String argsJsonStr = formatArgsToJson(joinPoint.getArgs());

        // 获取接口的功能描述
        String description = getApiOperationLogDescription(joinPoint);

        // 打印请求相关参数
        log.info("====== 请求开始: [{}], 入参: {}, 请求类: {}, 请求方法: {} =================================== ",
                description, argsJsonStr, className, methodName);

        // 执行目标方法
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("请求方法执行异常: [{}] - {}.{}，异常信息: {}",
                    description, className, methodName, throwable.getMessage());
            throw throwable; // 重新抛出异常
        }

        // 执行耗时
        long executionTime = (System.nanoTime() - startTime) / 1000000; // 毫秒

        // 打印出参等相关信息
        log.info("====== 请求结束: [{}], 耗时: {}ms, 出参: {} =================================== ",
                description, executionTime, JsonUtils.toJsonString(result));

        return result;
    }

    /**
     * 获取注解的描述信息
     * @param joinPoint 连接点对象
     * @return 注解中的 description
     */
    private String getApiOperationLogDescription(ProceedingJoinPoint joinPoint) {
        // 1. 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 2. 获取当前方法的 Method 对象
        Method method = signature.getMethod();

        // 3. 从 Method 对象中获取 @ApiOperationLog 注解
        ApiOperationLog apiOperationLog = method.getAnnotation(ApiOperationLog.class);

        // 4. 获取描述信息
        if (apiOperationLog != null) {
            return apiOperationLog.description();
        }
        return "";
    }

    /**
     * 格式化方法入参为 JSON 字符串
     * @param args 方法参数数组
     * @return 格式化后的 JSON 字符串
     */
    private String formatArgsToJson(Object[] args) {
        return Arrays.stream(args)
                .map(this::toJsonStr)
                .collect(Collectors.joining(", "));
    }

    /**
     * 将对象转换为 JSON 字符串
     * @param object 需要转换的对象
     * @return JSON 字符串
     */
    private String toJsonStr(Object object) {
        if (object instanceof MultipartFile) {
            // 如果是 MultipartFile，则仅记录文件的元数据
            MultipartFile file = (MultipartFile) object;
            return String.format("{\"fileName\":\"%s\", \"size\":%d}", file.getOriginalFilename(), file.getSize());
        }
        
        // 过滤掉不适合序列化的对象
        if (object instanceof HttpServletRequest || object instanceof HttpServletResponse || object instanceof org.apache.catalina.connector.RequestFacade) {
            return "{\"filtered\":\"request/response object\"}";
        }

        try {
            return JsonUtils.toJsonString(object);
        } catch (Exception e) {
            log.warn("无法将参数转化为 JSON 字符串: {}", object.getClass().getSimpleName(), e);
            return "{}"; // 出现异常时返回空的 JSON
        }
    }
}