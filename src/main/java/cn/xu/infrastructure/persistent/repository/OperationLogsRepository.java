package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.logging.repository.IOperationLogRepository;
import cn.xu.infrastructure.persistent.dao.IOperationLogsDao;
import cn.xu.infrastructure.persistent.po.OperationLogs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OperationLogsRepository implements IOperationLogRepository {

    @Autowired
    private IOperationLogsDao operationLogsDao;

    public void saveLog(OperationLogs log) {
        operationLogsDao.insertLog(log);
    }

    public OperationLogs readLog(Long id) {
        return operationLogsDao.selectLogById(id);
    }

    public void updateLog(OperationLogs log) {
        operationLogsDao.updateLog(log);
    }

    public void deleteLog(Long id) {
        operationLogsDao.deleteLogById(id);
    }
}
