package cn.xu.domain.logging.event;

import cn.xu.infrastructure.persistent.po.OperationLogs;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OperationLogEvent extends ApplicationEvent {
    private final OperationLogs operationLogs;

    public OperationLogEvent(Object source, OperationLogs operationLogs) {
        super(source);
        this.operationLogs = operationLogs;
    }

    public OperationLogs getOperationLogs() {
        return operationLogs;
    }
} 