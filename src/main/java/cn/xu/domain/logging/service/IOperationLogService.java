package cn.xu.domain.logging.service;

import cn.xu.infrastructure.persistent.po.OperationLogs;

public interface IOperationLogService {
    void saveLog(OperationLogs log);

    OperationLogs readLog(Long id);

    void updateLog(OperationLogs log);

    void deleteLog(Long id);
}
