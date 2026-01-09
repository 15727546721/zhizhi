package cn.xu.service.post;

import cn.xu.repository.PostRepository;
import cn.xu.repository.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帖子统计服务
 * 负责所有帖子相关的统计操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostStatisticsService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    // ==================== 总数统计 ====================

    public long countAll() {
        return postRepository.countAll();
    }

    public long countPublished() {
        return postMapper.countAll();
    }

    public long countHot() {
        return postMapper.countHotPosts();
    }

    public long countFeatured() {
        return postMapper.countFeaturedPosts();
    }

    // ==================== 按条件统计 ====================

    public long countByTagId(Long tagId) {
        return postRepository.countByTagId(tagId);
    }

    public long countHotByTagId(Long tagId) {
        return postMapper.countHotPostsByTagId(tagId);
    }

    public long countFeaturedByTagId(Long tagId) {
        return postMapper.countFeaturedPostsByTagId(tagId);
    }

    // ==================== 用户相关统计 ====================

    public long countPublishedByUserId(Long userId) {
        return postRepository.countPublishedByUserId(userId);
    }

    public long countDraftsByUserId(Long userId) {
        return postRepository.countDraftsByUserId(userId);
    }

    public long countByUserIdWithKeyword(Long userId, Integer status, String keyword) {
        return postMapper.countByUserIdWithKeyword(userId, status, keyword);
    }

    public long countByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0L;
        }
        return postMapper.countPostsByUserIds(userIds);
    }

    // ==================== 搜索统计 ====================

    public long countSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return 0L;
        }
        try {
            Long count = postMapper.countSearchResults(keyword.trim());
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("统计搜索结果失败: keyword={}", keyword, e);
            return 0L;
        }
    }
}
