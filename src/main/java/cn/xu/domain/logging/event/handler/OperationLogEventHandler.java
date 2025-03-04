package cn.xu.domain.logging.event.handler;

import cn.xu.domain.logging.event.OperationLogEvent;
import cn.xu.domain.logging.service.IOperationLogService;
import cn.xu.infrastructure.persistent.po.OperationLogs;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OperationLogEventHandler implements EventHandler<OperationLogEvent> {

    @Autowired
    private IOperationLogService operationLogService;

    @Override
    public void onEvent(OperationLogEvent event, long sequence, boolean endOfBatch) {
        try {
            OperationLogs logs = event.getOperationLogs();
            operationLogService.saveLog(logs);
            log.info("Operation log saved successfully: {}", logs);
        } catch (Exception e) {
            log.error("Failed to handle operation log event", e);
        }
    }
} 