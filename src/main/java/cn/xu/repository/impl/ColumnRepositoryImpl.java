package cn.xu.repository.impl;

import cn.xu.model.entity.Column;
import cn.xu.repository.ColumnRepository;
import cn.xu.repository.mapper.ColumnMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 专栏仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ColumnRepositoryImpl implements ColumnRepository {

    private final ColumnMapper columnMapper;

    @Override
    public void save(Column column) {
        columnMapper.insert(column);
    }

    @Override
    public void update(Column column) {
        columnMapper.update(column);
    }

    @Override
    public Column findById(Long id) {
        return columnMapper.selectById(id);
    }

    @Override
    public List<Column> findByUserId(Long userId) {
        return columnMapper.selectByUserId(userId);
    }

    @Override
    public List<Column> findByUserIdAndStatus(Long userId, Integer status) {
        return columnMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    public void deleteById(Long id) {
        columnMapper.deleteById(id);
    }

    @Override
    public void incrementPostCount(Long id) {
        columnMapper.incrementPostCount(id);
    }

    @Override
    public void decrementPostCount(Long id) {
        columnMapper.decrementPostCount(id);
    }

    @Override
    public void incrementSubscribeCount(Long id) {
        columnMapper.incrementSubscribeCount(id);
    }

    @Override
    public void decrementSubscribeCount(Long id) {
        columnMapper.decrementSubscribeCount(id);
    }

    @Override
    public void updateLastPostTime(Long id, LocalDateTime time) {
        columnMapper.updateLastPostTime(id, time);
    }

    @Override
    public int countByUserId(Long userId) {
        return columnMapper.countByUserId(userId);
    }

    @Override
    public List<Column> findPublishedByLastPostTime(int offset, int limit) {
        return columnMapper.selectPublishedByLastPostTime(offset, limit);
    }

    @Override
    public List<Column> findPublishedBySubscribeCount(int offset, int limit) {
        return columnMapper.selectPublishedBySubscribeCount(offset, limit);
    }

    @Override
    public int countPublished() {
        return columnMapper.countPublished();
    }

    @Override
    public List<Column> searchByKeyword(String keyword, int offset, int limit) {
        return columnMapper.searchByKeyword(keyword, offset, limit);
    }

    @Override
    public int countSearchByKeyword(String keyword) {
        return columnMapper.countSearchByKeyword(keyword);
    }

    @Override
    public List<Column> findRecommended(int limit) {
        return columnMapper.selectRecommended(limit);
    }

    @Override
    public List<Column> findByPostId(Long postId) {
        return columnMapper.selectByPostId(postId);
    }

    @Override
    public List<Column> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return columnMapper.selectByIds(ids);
    }

    @Override
    public List<Column> findByConditions(Integer status, Integer isRecommended, Long userId, int offset, int limit) {
        return columnMapper.findByConditions(status, isRecommended, userId, offset, limit);
    }

    @Override
    public int countByConditions(Integer status, Integer isRecommended, Long userId) {
        return columnMapper.countByConditions(status, isRecommended, userId);
    }

    @Override
    public int countAll() {
        return columnMapper.countAll();
    }

    @Override
    public int countByStatus(Integer status) {
        return columnMapper.countByStatus(status);
    }

    @Override
    public int countRecommended() {
        return columnMapper.countRecommended();
    }

    @Override
    public long sumSubscribeCount() {
        return columnMapper.sumSubscribeCount();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        columnMapper.updateStatus(id, status);
    }

    @Override
    public void updateRecommended(Long id, Integer isRecommended) {
        columnMapper.updateRecommended(id, isRecommended);
    }
}
