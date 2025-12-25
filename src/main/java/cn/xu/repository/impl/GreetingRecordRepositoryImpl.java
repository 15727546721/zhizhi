package cn.xu.repository.impl;

import cn.xu.model.entity.GreetingRecord;
import cn.xu.repository.GreetingRecordRepository;
import cn.xu.repository.mapper.GreetingRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 打招呼记录仓储实现
 * <p>负责打招呼记录的持久化操作</p>

 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class GreetingRecordRepositoryImpl implements GreetingRecordRepository {
    
    private final GreetingRecordMapper greetingRecordMapper;
    
    @Override
    public void markGreetingSent(Long userId, Long targetId) {
        if (userId == null || targetId == null) {
            return;
        }
        
        // 幂等处理
        if (hasSentGreeting(userId, targetId)) {
            log.debug("[打招呼] 已存在记录 - userId:{}, targetId:{}", userId, targetId);
            return;
        }
        
        GreetingRecord record = GreetingRecord.create(userId, targetId);
        greetingRecordMapper.insert(record);
        log.info("[打招呼] 记录已创建 - {}", record.getSimpleInfo());
    }
    
    @Override
    public boolean hasSentGreeting(Long userId, Long targetId) {
        if (userId == null || targetId == null) {
            return false;
        }
        return greetingRecordMapper.existsByUserIdAndTargetId(userId, targetId);
    }
    
    @Override
    public void deleteGreeting(Long userId, Long targetId) {
        if (userId == null || targetId == null) {
            return;
        }
        greetingRecordMapper.deleteByUserIdAndTargetId(userId, targetId);
        log.debug("[打招呼] 记录已删除 - userId:{}, targetId:{}", userId, targetId);
    }
    
    @Override
    public void deleteBidirectionalGreeting(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return;
        }
        int deleted = greetingRecordMapper.deleteBidirectional(userId1, userId2);
        log.info("[打招呼] 双向记录已清理 - userId1:{}, userId2:{}, 删除数:{}", userId1, userId2, deleted);
    }
}
