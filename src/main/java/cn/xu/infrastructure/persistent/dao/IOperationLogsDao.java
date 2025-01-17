package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.OperationLogs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IOperationLogsDao {
    void insertLog(OperationLogs log);
    
    OperationLogs selectLogById(@Param("id") Long id);
    
    void updateLog(OperationLogs log);
    
    void deleteLogById(@Param("id") Long id);
}
