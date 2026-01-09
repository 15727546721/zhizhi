package cn.xu.service.post;

import cn.xu.model.entity.Post;
import cn.xu.repository.PostRepository;
import cn.xu.repository.impl.PostRepositoryImpl.PostWithTags;
import cn.xu.repository.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 帖子查询服务
 * 负责所有帖子相关的查询操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    // ==================== 单条查询 ====================

    public Optional<Post> getById(Long postId) {
        return postRepository.findById(postId);
    }

    public PostWithTags getWithTags(Long postId) {
        return postRepository.findByIdWithTags(postId);
    }

    // ==================== 列表查询 ====================

    public List<Post> getAll(int pageNo, int pageSize) {
        int offset = calcOffset(pageNo, pageSize);
        return postRepository.findAll(offset, pageSize);
    }

    public List<Post> getAllPublished() {
        return postRepository.findAllPublished();
    }

    public List<Post> getByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    public List<Post> getByUserIdAndStatus(Long userId, Integer status, int pageNo, int pageSize) {
        int offset = calcOffset(pageNo, pageSize);
        return postRepository.findByUserIdAndStatus(userId, status, offset, pageSize);
    }

    public List<Post> getByUserIdWithKeyword(Long userId, Integer status, String keyword, int pageNo, int pageSize) {
        int offset = calcOffset(pageNo, pageSize);
        return postMapper.findByUserIdWithKeyword(userId, status, keyword, offset, pageSize);
    }

    public List<Post> getByTagId(Long tagId, int pageNo, int pageSize) {
        int offset = calcOffset(pageNo, pageSize);
        return postRepository.findByTagId(tagId, offset, pageSize);
    }

    public List<Post> getByIds(List<Long> postIds) {
        return postRepository.findByIds(postIds);
    }

    public List<Post> getByUserIds(List<Long> userIds, int pageNo, int pageSize) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        int offset = calcOffset(pageNo, pageSize);
        return postMapper.findPostsByUserIds(userIds, offset, pageSize);
    }

    // ==================== 热门/精选查询 ====================

    public List<Post> getHotPosts(int pageNo, int pageSize) {
        int offset = calcOffset(pageNo, pageSize);
        return postRepository.findHotPosts(offset, pageSize);
    }

    public List<Post> getHotPostsByTag(Long tagId, int pageNo, int pageSize) {
        int offset = calcOffset(pageNo, pageSize);
        return postMapper.findHotPostsByTagId(tagId, offset, pageSize);
    }

    public List<Post> getFeaturedPosts(int pageNo, int pageSize) {
        int offset = calcOffset(pageNo, pageSize);
        return postMapper.findFeaturedPosts(offset, pageSize);
    }

    public List<Post> getFeaturedPostsByTag(Long tagId, int pageNo, int pageSize) {
        int offset = calcOffset(pageNo, pageSize);
        return postMapper.findFeaturedPostsByTagId(tagId, offset, pageSize);
    }

    public List<Post> getByFavoriteCount(int limit) {
        return postMapper.findPostsByFavoriteCount(limit);
    }

    // ==================== 搜索 ====================

    public List<Post> search(String keyword, int offset, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return postMapper.searchPosts(keyword.trim(), offset, limit);
        } catch (Exception e) {
            log.error("搜索帖子失败: keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    // ==================== 私有方法 ====================

    private int calcOffset(int pageNo, int pageSize) {
        return Math.max(0, (pageNo - 1) * pageSize);
    }
}
