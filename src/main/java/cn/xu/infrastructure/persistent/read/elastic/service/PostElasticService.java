package cn.xu.infrastructure.persistent.read.elastic.service;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostHotScorePolicy;
import cn.xu.domain.post.model.valobj.PostTitle;
import cn.xu.infrastructure.persistent.po.Post;
import cn.xu.infrastructure.persistent.read.elastic.model.PostIndex;
import cn.xu.infrastructure.persistent.read.elastic.repository.PostElasticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class PostElasticService {

    private final PostElasticRepository postElasticRepository;

    @PostConstruct
    public void init() {
        // 初始化检查
        try {
            // 尝试执行一个简单的操作来验证连接
            postElasticRepository.count();
        } catch (Exception e) {
            // 如果连接失败，记录日志但不抛出异常
        }
    }

    public void indexPost(PostEntity post) {
        try {
            PostIndex index = PostIndexConverter.from(post);
            postElasticRepository.save(index);
        } catch (Exception e) {
            // 忽略Elasticsearch操作失败，不影响主流程
        }
    }

    public void updateIndexedPost(PostEntity post) {
        try {
            PostIndex index = PostIndexConverter.from(post);
            postElasticRepository.save(index);
        } catch (Exception e) {
            // 忽略Elasticsearch操作失败，不影响主流程
        }
    }

    public void removeIndexedPost(Long postId) {
        try {
            postElasticRepository.deleteById(postId);
        } catch (Exception e) {
            // 忽略Elasticsearch操作失败，不影响主流程
        }
    }

    // 初始化或更新索引
    public void indexPost(Post post) {
        try {
            PostIndex index = new PostIndex();
            index.setId(post.getId());
            index.setTitle(post.getTitle());
            index.setDescription(post.getDescription());
            index.setCoverUrl(post.getCoverUrl());
            index.setUserId(post.getUserId());
            index.setCategoryId(post.getCategoryId());
            index.setViewCount(post.getViewCount());
            index.setCollectCount(post.getCollectCount());
            index.setCommentCount(post.getCommentCount());
            index.setLikeCount(post.getLikeCount());
            index.setPublishTime(post.getPublishTime());
            index.setUpdateTime(post.getUpdateTime());

            double hotScore = PostHotScorePolicy.calculate(
                    post.getLikeCount() != null ? post.getLikeCount() : 0L,
                    post.getCollectCount() != null ? post.getCollectCount() : 0L,
                    post.getCommentCount() != null ? post.getCommentCount() : 0L,
                    post.getPublishTime());
            index.setHotScore(hotScore);

            postElasticRepository.save(index);
        } catch (Exception e) {
            // 忽略Elasticsearch操作失败，不影响主流程
        }
    }

    // 获取热度排行（日榜，周榜，月榜）
    public Page<PostIndex> getHotRank(String rankType, Pageable pageable) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start;
            switch (rankType) {
                case "day":
                    start = now.minusDays(1);
                    break;
                case "week":
                    start = now.minusWeeks(1);
                    break;
                case "month":
                    start = now.minusMonths(1);
                    break;
                default:
                    start = now.minusDays(1);
            }
            return postElasticRepository.findByPublishTimeBetweenOrderByHotScoreDesc(start, now, pageable);
        } catch (Exception e) {
            // 如果Elasticsearch不可用，抛出异常让调用方处理
            throw new RuntimeException("Elasticsearch服务不可用", e);
        }
    }

    // 简单时间降序搜索
    public Page<PostIndex> searchByTitleTimeDesc(String keyword, Pageable pageable) {
        try {
            return postElasticRepository.findByTitleContainingOrderByPublishTimeDesc(keyword, pageable);
        } catch (Exception e) {
            // 如果Elasticsearch不可用，抛出异常让调用方处理
            throw new RuntimeException("Elasticsearch服务不可用", e);
        }
    }

    // 搜索帖子（示例）
    public List<PostEntity> searchPosts(String title) {
        try {
            Page<PostIndex> page = postElasticRepository.findByTitleContainingOrderByPublishTimeDesc(title, Pageable.unpaged());
            return page.stream()
                    .map(this::toPostEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 如果Elasticsearch不可用，抛出异常让调用方处理
            throw new RuntimeException("Elasticsearch服务不可用", e);
        }
    }

    private PostEntity toPostEntity(PostIndex index) {
        return PostEntity.builder()
                .id(index.getId())
                .title(index.getTitle() != null ? new PostTitle(index.getTitle()) : null)
                .description(index.getDescription())
                .coverUrl(index.getCoverUrl())
                .userId(index.getUserId())
                .categoryId(index.getCategoryId())
                .viewCount(index.getViewCount())
                .collectCount(index.getCollectCount())
                .commentCount(index.getCommentCount())
                .likeCount(index.getLikeCount())
                .createTime(index.getPublishTime())
                .updateTime(index.getUpdateTime())
                .build();
    }
}