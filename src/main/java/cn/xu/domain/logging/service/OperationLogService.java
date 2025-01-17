package cn.xu.domain.logging.service;

import cn.xu.domain.logging.event.OperationLogEvent;
import cn.xu.infrastructure.persistent.po.OperationLogs;
import cn.xu.infrastructure.persistent.dao.IOperationLogsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class OperationLogService implements IOperationLogService {

    @Autowired
    private IOperationLogsDao operationLogsDao;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void saveLog(OperationLogs log) {
        operationLogsDao.insertLog(log);
        eventPublisher.publishEvent(new OperationLogEvent(this, log));
    }

    @Override
    public OperationLogs readLog(Long id) {
        return operationLogsDao.selectLogById(id);
    }

    @Override
    public void updateLog(OperationLogs log) {
        operationLogsDao.updateLog(log);
    }

    @Override
    public void deleteLog(Long id) {
        operationLogsDao.deleteLogById(id);
    }
}
