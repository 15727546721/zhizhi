package cn.xu.repository.impl;

import cn.xu.model.entity.Column;
import cn.xu.model.entity.ColumnSubscription;
import cn.xu.repository.ColumnSubscriptionRepository;
import cn.xu.repository.mapper.ColumnSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 专栏订阅仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ColumnSubscriptionRepositoryImpl implements ColumnSubscriptionRepository {

    private final ColumnSubscriptionMapper columnSubscriptionMapper;

    @Override
    public void save(ColumnSubscription subscription) {
        columnSubscriptionMapper.insert(subscription);
    }

    @Override
    public void update(ColumnSubscription subscription) {
        columnSubscriptionMapper.update(subscription);
    }

    @Override
    public ColumnSubscription findByUserAndColumn(Long userId, Long columnId) {
        return columnSubscriptionMapper.selectByUserAndColumn(userId, columnId);
    }

    @Override
    public List<Column> findSubscribedColumns(Long userId, int offset, int limit) {
        return columnSubscriptionMapper.selectSubscribedColumns(userId, offset, limit);
    }

    @Override
    public int countSubscribedColumns(Long userId) {
        return columnSubscriptionMapper.countSubscribedColumns(userId);
    }

    @Override
    public boolean isSubscribed(Long userId, Long columnId) {
        return columnSubscriptionMapper.isSubscribed(userId, columnId) > 0;
    }

    @Override
    public List<Long> batchCheckSubscribed(Long userId, List<Long> columnIds) {
        return columnSubscriptionMapper.batchCheckSubscribed(userId, columnIds);
    }

    @Override
    public List<Long> findSubscribedColumnIds(Long userId, int offset, int limit) {
        return columnSubscriptionMapper.selectSubscribedColumnIds(userId, offset, limit);
    }
}
