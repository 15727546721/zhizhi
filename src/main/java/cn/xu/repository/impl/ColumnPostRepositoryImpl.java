package cn.xu.repository.impl;

import cn.xu.model.entity.Column;
import cn.xu.model.entity.ColumnPost;
import cn.xu.repository.ColumnPostRepository;
import cn.xu.repository.mapper.ColumnPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 专栏文章关联仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ColumnPostRepositoryImpl implements ColumnPostRepository {

    private final ColumnPostMapper columnPostMapper;

    @Override
    public void save(ColumnPost columnPost) {
        columnPostMapper.insert(columnPost);
    }

    @Override
    public void delete(Long columnId, Long postId) {
        columnPostMapper.delete(columnId, postId);
    }

    @Override
    public void updateSort(Long columnId, Long postId, Integer sort) {
        columnPostMapper.updateSort(columnId, postId, sort);
    }

    @Override
    public void batchUpdateSort(Long columnId, List<ColumnPost> columnPosts) {
        columnPostMapper.batchUpdateSort(columnId, columnPosts);
    }

    @Override
    public ColumnPost findByColumnAndPost(Long columnId, Long postId) {
        return columnPostMapper.selectByColumnAndPost(columnId, postId);
    }

    @Override
    public List<ColumnPost> findByColumnId(Long columnId) {
        return columnPostMapper.selectByColumnId(columnId);
    }

    @Override
    public List<ColumnPost> findByColumnIdWithPage(Long columnId, int offset, int limit) {
        return columnPostMapper.selectByColumnIdWithPage(columnId, offset, limit);
    }

    @Override
    public List<Column> findColumnsByPostId(Long postId) {
        return columnPostMapper.selectColumnsByPostId(postId);
    }

    @Override
    public int countByColumnId(Long columnId) {
        return columnPostMapper.countByColumnId(columnId);
    }

    @Override
    public int countByPostId(Long postId) {
        return columnPostMapper.countByPostId(postId);
    }

    @Override
    public boolean exists(Long columnId, Long postId) {
        return columnPostMapper.exists(columnId, postId) > 0;
    }

    @Override
    public ColumnPost findPreviousBySort(Long columnId, Integer currentSort) {
        return columnPostMapper.selectPreviousBySort(columnId, currentSort);
    }

    @Override
    public ColumnPost findNextBySort(Long columnId, Integer currentSort) {
        return columnPostMapper.selectNextBySort(columnId, currentSort);
    }

    @Override
    public void deleteByColumnId(Long columnId) {
        columnPostMapper.deleteByColumnId(columnId);
    }

    @Override
    public void deleteByPostId(Long postId) {
        columnPostMapper.deleteByPostId(postId);
    }
}
