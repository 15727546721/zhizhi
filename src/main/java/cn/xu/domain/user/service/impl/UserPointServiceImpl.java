package cn.xu.domain.user.service.impl;

import cn.xu.domain.user.service.UserPointService;
import cn.xu.infrastructure.transaction.TransactionParticipant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户积分服务实现类
 * 实现事务参与者接口以支持分布式事务
 */
@Slf4j
@Service
public class UserPointServiceImpl implements UserPointService, TransactionParticipant {
    
    // 使用内存存储模拟积分存储，实际项目中应该使用数据库
    private final ConcurrentHashMap<Long, AtomicInteger> userPoints = new ConcurrentHashMap<>();
    
    // 用于事务管理的临时存储
    private final ThreadLocal<ConcurrentHashMap<Long, Integer>> tempPoints = new ThreadLocal<>();
    
    @Override
    public void addUserPoints(Long userId, int points) {
        if (userId == null || points <= 0) {
            log.warn("无效的用户ID或积分数量: userId={}, points={}", userId, points);
            return;
        }
        
        // 如果在事务中，先保存到临时存储
        ConcurrentHashMap<Long, Integer> temp = tempPoints.get();
        if (temp != null) {
            temp.put(userId, points);
            log.debug("在事务中暂存用户积分变更: userId={}, points={}", userId, points);
            return;
        }
        
        // 直接更新
        userPoints.computeIfAbsent(userId, k -> new AtomicInteger(0))
                  .addAndGet(points);
        
        log.info("为用户增加积分: userId={}, points={}, 总积分={}", 
                userId, points, userPoints.get(userId).get());
    }
    
    @Override
    public void deductUserPoints(Long userId, int points) {
        if (userId == null || points <= 0) {
            log.warn("无效的用户ID或积分数量: userId={}, points={}", userId, points);
            return;
        }
        
        // 如果在事务中，先保存到临时存储（负值表示扣除）
        ConcurrentHashMap<Long, Integer> temp = tempPoints.get();
        if (temp != null) {
            temp.put(userId, -points);
            log.debug("在事务中暂存用户积分变更: userId={}, points={}", userId, -points);
            return;
        }
        
        // 直接更新
        userPoints.computeIfAbsent(userId, k -> new AtomicInteger(0))
                  .addAndGet(-points);
        
        log.info("为用户扣除积分: userId={}, points={}, 总积分={}", 
                userId, points, userPoints.get(userId).get());
    }
    
    @Override
    public int getUserPoints(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        return userPoints.getOrDefault(userId, new AtomicInteger(0)).get();
    }
    
    @Override
    public void commit() throws Exception {
        ConcurrentHashMap<Long, Integer> temp = tempPoints.get();
        if (temp != null) {
            for (java.util.Map.Entry<Long, Integer> entry : temp.entrySet()) {
                Long userId = entry.getKey();
                Integer points = entry.getValue();
                
                userPoints.computeIfAbsent(userId, k -> new AtomicInteger(0))
                          .addAndGet(points);
                
                log.info("提交用户积分变更: userId={}, points={}", userId, points);
            }
            tempPoints.remove();
        }
    }
    
    @Override
    public void rollback() throws Exception {
        ConcurrentHashMap<Long, Integer> temp = tempPoints.get();
        if (temp != null) {
            log.info("回滚用户积分变更");
            tempPoints.remove();
        }
    }
    
    /**
     * 开始事务
     */
    public void beginTransaction() {
        tempPoints.set(new ConcurrentHashMap<>());
        log.debug("开始用户积分事务");
    }
}