package cn.xu.service.column;

import cn.xu.model.entity.Column;
import cn.xu.model.entity.ColumnSubscription;
import cn.xu.repository.ColumnRepository;
import cn.xu.repository.ColumnSubscriptionRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 专栏订阅服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnSubscriptionService {

    private final ColumnSubscriptionRepository subscriptionRepository;
    private final ColumnRepository columnRepository;
    private final ColumnService columnService;

    /**
     * 订阅专栏
     */
    @Transactional(rollbackFor = Exception.class)
    public void subscribe(Long userId, Long columnId) {
        // 1. 验证专栏
        Column column = columnRepository.findById(columnId);
        if (column == null) {
            throw new BusinessException("专栏不存在");
        }
        if (!column.isPublished()) {
            throw new BusinessException("该专栏暂不可订阅");
        }
        
        // 2. 检查是否已订阅
        ColumnSubscription existing = subscriptionRepository.findByUserAndColumn(userId, columnId);
        
        if (existing != null) {
            if (existing.isSubscribed()) {
                // 已订阅,幂等操作
                log.info("[专栏] 用户已订阅 - userId: {}, columnId: {}", userId, columnId);
                return;
            } else {
                // 重新订阅
                existing.subscribe();
                subscriptionRepository.update(existing);
                log.info("[专栏] 重新订阅 - userId: {}, columnId: {}", userId, columnId);
            }
        } else {
            // 新订阅
            ColumnSubscription subscription = ColumnSubscription.create(userId, columnId);
            subscriptionRepository.save(subscription);
            log.info("[专栏] 新订阅 - userId: {}, columnId: {}", userId, columnId);
        }
        
        // 3. 更新专栏订阅数
        columnService.incrementSubscribeCount(columnId);
        
        log.info("[专栏] 订阅成功 - userId: {}, columnId: {}", userId, columnId);
    }

    /**
     * 取消订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public void unsubscribe(Long userId, Long columnId) {
        // 1. 查询订阅记录
        ColumnSubscription existing = subscriptionRepository.findByUserAndColumn(userId, columnId);
        
        if (existing == null || !existing.isSubscribed()) {
            // 未订阅,幂等操作
            log.info("[专栏] 用户未订阅 - userId: {}, columnId: {}", userId, columnId);
            return;
        }
        
        // 2. 取消订阅
        existing.unsubscribe();
        subscriptionRepository.update(existing);
        
        // 3. 更新专栏订阅数
        columnService.decrementSubscribeCount(columnId);
        
        log.info("[专栏] 取消订阅成功 - userId: {}, columnId: {}", userId, columnId);
    }

    /**
     * 检查是否已订阅
     */
    public boolean isSubscribed(Long userId, Long columnId) {
        if (userId == null || columnId == null) {
            return false;
        }
        return subscriptionRepository.isSubscribed(userId, columnId);
    }

    /**
     * 获取用户订阅的专栏列表
     */
    public List<Column> getUserSubscriptions(Long userId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        int offset = (page - 1) * size;
        return subscriptionRepository.findSubscribedColumns(userId, offset, size);
    }

    /**
     * 统计用户订阅的专栏数
     */
    public int countUserSubscriptions(Long userId) {
        if (userId == null) {
            return 0;
        }
        return subscriptionRepository.countSubscribedColumns(userId);
    }

    /**
     * 批量检查订阅状态
     */
    public List<Long> batchCheckSubscribed(Long userId, List<Long> columnIds) {
        if (userId == null || columnIds == null || columnIds.isEmpty()) {
            return List.of();
        }
        return subscriptionRepository.batchCheckSubscribed(userId, columnIds);
    }

    /**
     * 获取用户订阅的专栏ID列表（分页）
     */
    public List<Long> getUserSubscribedColumnIds(Long userId, int offset, int size) {
        if (userId == null) {
            return List.of();
        }
        return subscriptionRepository.findSubscribedColumnIds(userId, offset, size);
    }

    /**
     * 获取用户订阅总数
     */
    public int getUserSubscriptionCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return subscriptionRepository.countSubscribedColumns(userId);
    }
}
