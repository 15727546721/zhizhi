package cn.xu.service.column;

import cn.xu.model.entity.Column;
import cn.xu.model.entity.ColumnPost;
import cn.xu.model.entity.Post;
import cn.xu.repository.ColumnPostRepository;
import cn.xu.repository.ColumnRepository;
import cn.xu.repository.PostRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 专栏文章管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnPostService {

    private final ColumnPostRepository columnPostRepository;
    private final ColumnRepository columnRepository;
    private final PostRepository postRepository;
    private final ColumnService columnService;
    
    private static final int MAX_POST_COUNT = 50;
    private static final int MAX_COLUMN_PER_POST = 3;

    /**
     * 添加文章到专栏
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPostToColumn(Long userId, Long columnId, Long postId, Integer sort) {
        // 1. 验证专栏权限
        Column column = columnRepository.findById(columnId);
        if (column == null) {
            throw new BusinessException("专栏不存在");
        }
        if (!column.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此专栏");
        }
        
        // 2. 验证文章
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只能添加自己的帖子");
        }
        if (!post.isPublished()) {
            throw new BusinessException("只能添加已发布的帖子");
        }
        
        // 3. 检查专栏文章数量限制
        int postCount = columnPostRepository.countByColumnId(columnId);
        if (postCount >= MAX_POST_COUNT) {
            throw new BusinessException("专栏文章数量已达上限(" + MAX_POST_COUNT + "篇)");
        }
        
        // 4. 检查文章专栏数量限制
        int columnCount = columnPostRepository.countByPostId(postId);
        if (columnCount >= MAX_COLUMN_PER_POST) {
            throw new BusinessException("文章最多只能添加到" + MAX_COLUMN_PER_POST + "个专栏");
        }
        
        // 5. 检查是否已存在
        if (columnPostRepository.exists(columnId, postId)) {
            throw new BusinessException("文章已在该专栏中");
        }
        
        // 6. 添加关联
        int sortValue = sort != null ? sort : postCount + 1;
        ColumnPost columnPost = ColumnPost.create(columnId, postId, sortValue);
        columnPostRepository.save(columnPost);
        
        // 7. 更新专栏统计
        columnService.incrementPostCount(columnId);
        columnService.updateLastPostTime(columnId, post.getCreateTime());
        
        log.info("[专栏] 添加文章成功 - columnId: {}, postId: {}, sort: {}", columnId, postId, sortValue);
    }

    /**
     * 从专栏移除文章
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePostFromColumn(Long userId, Long columnId, Long postId) {
        // 1. 验证专栏权限
        Column column = columnRepository.findById(columnId);
        if (column == null) {
            throw new BusinessException("专栏不存在");
        }
        if (!column.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此专栏");
        }
        
        // 2. 检查是否存在
        if (!columnPostRepository.exists(columnId, postId)) {
            throw new BusinessException("文章不在该专栏中");
        }
        
        // 3. 删除关联
        columnPostRepository.delete(columnId, postId);
        
        // 4. 更新专栏统计
        columnService.decrementPostCount(columnId);
        
        log.info("[专栏] 移除文章成功 - columnId: {}, postId: {}", columnId, postId);
    }

    /**
     * 调整文章顺序
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePostSort(Long userId, Long columnId, Long postId, Integer newSort) {
        // 1. 验证专栏权限
        Column column = columnRepository.findById(columnId);
        if (column == null) {
            throw new BusinessException("专栏不存在");
        }
        if (!column.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此专栏");
        }
        
        // 2. 检查是否存在
        if (!columnPostRepository.exists(columnId, postId)) {
            throw new BusinessException("文章不在该专栏中");
        }
        
        // 3. 更新排序
        columnPostRepository.updateSort(columnId, postId, newSort);
        
        log.info("[专栏] 调整文章顺序 - columnId: {}, postId: {}, newSort: {}", columnId, postId, newSort);
    }

    /**
     * 批量调整顺序
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSort(Long userId, Long columnId, List<ColumnPost> sortList) {
        // 1. 验证专栏权限
        Column column = columnRepository.findById(columnId);
        if (column == null) {
            throw new BusinessException("专栏不存在");
        }
        if (!column.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此专栏");
        }
        
        // 2. 批量更新
        columnPostRepository.batchUpdateSort(columnId, sortList);
        
        log.info("[专栏] 批量调整文章顺序 - columnId: {}, count: {}", columnId, sortList.size());
    }

    /**
     * 获取专栏的文章列表
     */
    public List<ColumnPost> getColumnPosts(Long columnId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        int offset = (page - 1) * size;
        return columnPostRepository.findByColumnIdWithPage(columnId, offset, size);
    }

    /**
     * 获取专栏最近的文章（用于专栏详情页展示）
     */
    public List<ColumnPost> getRecentPosts(Long columnId, int limit) {
        return columnPostRepository.findByColumnIdWithPage(columnId, 0, limit);
    }

    /**
     * 获取文章所属的专栏列表
     */
    public List<Column> getPostColumns(Long postId) {
        return columnPostRepository.findColumnsByPostId(postId);
    }

    /**
     * 获取文章在专栏中的上一篇
     */
    public ColumnPost getPreviousPost(Long columnId, Long currentPostId) {
        ColumnPost current = columnPostRepository.findByColumnAndPost(columnId, currentPostId);
        if (current == null) {
            return null;
        }
        return columnPostRepository.findPreviousBySort(columnId, current.getSort());
    }

    /**
     * 获取文章在专栏中的下一篇
     */
    public ColumnPost getNextPost(Long columnId, Long currentPostId) {
        ColumnPost current = columnPostRepository.findByColumnAndPost(columnId, currentPostId);
        if (current == null) {
            return null;
        }
        return columnPostRepository.findNextBySort(columnId, current.getSort());
    }

    /**
     * 检查文章是否在专栏中
     */
    public boolean isPostInColumn(Long columnId, Long postId) {
        return columnPostRepository.exists(columnId, postId);
    }

    /**
     * 统计文章所属专栏数
     */
    public int countPostColumns(Long postId) {
        return columnPostRepository.countByPostId(postId);
    }

    /**
     * 删除帖子时移除所有专栏关联
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePostFromAllColumns(Long postId) {
        // 获取文章所属的所有专栏
        List<Column> columns = columnPostRepository.findColumnsByPostId(postId);
        
        // 删除所有关联
        columnPostRepository.deleteByPostId(postId);
        
        // 更新所有专栏的文章计数
        for (Column column : columns) {
            columnService.decrementPostCount(column.getId());
        }
        
        log.info("[专栏] 移除文章的所有专栏关联 - postId: {}, columnCount: {}", postId, columns.size());
    }
}
