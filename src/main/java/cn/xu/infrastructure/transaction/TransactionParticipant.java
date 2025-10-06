package cn.xu.infrastructure.transaction;

/**
 * 事务参与者接口
 * 定义分布式事务中参与者的操作
 */
public interface TransactionParticipant {
    
    /**
     * 提交事务
     * @throws Exception 提交过程中可能发生的异常
     */
    void commit() throws Exception;
    
    /**
     * 回滚事务
     * @throws Exception 回滚过程中可能发生的异常
     */
    void rollback() throws Exception;
}