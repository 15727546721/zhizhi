package cn.xu.domain.logging;

import cn.xu.domain.logging.event.OperationLogEvent;
import cn.xu.infrastructure.persistent.po.OperationLogs;
import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final RingBuffer<OperationLogEvent> ringBuffer;

    @Autowired
    public OperationLogAspect(@Qualifier("operationLogRingBuffer") RingBuffer<OperationLogEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    @Pointcut("@annotation(cn.xu.domain.logging.LogOperation)")
    public void logOperationPointcut() {}

    @AfterReturning(pointcut = "logOperationPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            createAndPublishLog(joinPoint, result, true);
        } catch (Exception e) {
            log.error("Failed to create operation log", e);
        }
    }

    @AfterThrowing(pointcut = "logOperationPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Exception e) {
        try {
            createAndPublishLog(joinPoint, e.getMessage(), false);
        } catch (Exception ex) {
            log.error("Failed to create operation log for exception", ex);
        }
    }

    private void createAndPublishLog(JoinPoint joinPoint, Object result, boolean success) {
        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            LogOperation logOperation = signature.getMethod().getAnnotation(LogOperation.class);

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

            // 创建日志对象
            OperationLogs logs = OperationLogs.builder()
                    .operationType(logOperation.value())
                    .operationDescription(signature.getMethod().getName())
                    .userId(1L) // 临时用户ID
                    .username("system") // 临时用户名
                    .createTime(new Date())
                    .ipAddress(request != null ? request.getRemoteAddr() : "unknown")
                    .status(success ? 1 : 0)
                    .additionalInfo(String.valueOf(result))
                    .build();

            // 发布事件到Disruptor
            long sequence = ringBuffer.next();
            try {
                OperationLogEvent event = ringBuffer.get(sequence);
                event = new OperationLogEvent(this, logs);
            } finally {
                ringBuffer.publish(sequence);
            }
        } catch (Exception e) {
            log.error("Failed to create and publish log", e);
        }
    }
} 