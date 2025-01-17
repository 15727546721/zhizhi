package cn.xu.domain.logging.event.factory;

import cn.xu.domain.logging.event.OperationLogEvent;
import cn.xu.infrastructure.persistent.po.OperationLogs;
import com.lmax.disruptor.EventFactory;
import org.springframework.stereotype.Component;

@Component
public class OperationLogEventFactory implements EventFactory<OperationLogEvent> {
    @Override
    public OperationLogEvent newInstance() {
        return new OperationLogEvent(this, new OperationLogs());
    }
} 