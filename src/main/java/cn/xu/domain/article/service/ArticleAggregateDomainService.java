package cn.xu.domain.article.service;

import cn.xu.api.web.model.vo.article.ArticleDetailVO;
import cn.xu.domain.article.model.aggregate.ArticleAndAuthorAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.model.valobj.ArticleStatus;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.IArticleCollectService;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章聚合领域服务
 * 负责文章聚合根的组装和复杂业务逻辑处理，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleAggregateDomainService {

    private final IUserService userService;
    private final ITagRepository tagRepository;
    private final ILikeService likeService;
    private final IArticleCollectService articleCollectService;
    private final IFollowService followService;

    /**
     * 构建文章详情VO（包含用户状态信息）
     * @param article 文章实体
     * @param currentUserId 当前用户ID（可为null）
     * @return 文章详情VO
     */
    public ArticleDetailVO buildArticleDetailVO(ArticleEntity article, Long currentUserId) {
        if (article == null) {
            throw new IllegalArgumentException("文章不能为空");
        }

        // 验证文章状态
        validateArticleStatus(article);

        // 获取作者信息
        UserEntity author = userService.getUserById(article.getUserId());
        if (author == null) {
            throw new BusinessException("作者信息不存在");
        }

        // 获取文章标签
        List<TagEntity> tags = tagRepository.getTagsByArticleId(article.getId());

        // 构建用户状态信息
        ArticleUserState userState = buildUserState(article, author, currentUserId);

        return ArticleDetailVO.builder()
                .article(article)
                .user(author)
                .tags(tags)
                .isLiked(userState.isLiked())
                .isCollected(userState.isCollected())
                .isFollowed(userState.isFollowed())
                .isAuthor(userState.isAuthor())
                .build();
    }

    /**
     * 构建文章和作者聚合
     * @param article 文章实体
     * @return 文章和作者聚合
     */
    public ArticleAndAuthorAggregate buildArticleAndAuthorAggregate(ArticleEntity article) {
        if (article == null) {
            throw new IllegalArgumentException("文章不能为空");
        }

        // 获取作者信息
        UserEntity author = userService.getUserById(article.getUserId());
        if (author == null) {
            throw new BusinessException("作者信息不存在");
        }

        // 获取文章标签
        List<TagEntity> tags = tagRepository.getTagsByArticleId(article.getId());

        return ArticleAndAuthorAggregate.builder()
                .article(article)
                .author(author)
                .tags(tags)
                .build();
    }

    /**
     * 验证文章发布权限
     * @param article 文章实体
     * @param userId 用户ID
     * @return 是否有权限
     */
    public boolean validatePublishPermission(ArticleEntity article, Long userId) {
        if (article == null || userId == null) {
            return false;
        }

        return article.getUserId().equals(userId);
    }

    /**
     * 验证文章删除权限
     * @param article 文章实体
     * @param userId 用户ID
     * @return 是否有权限
     */
    public boolean validateDeletePermission(ArticleEntity article, Long userId) {
        if (article == null || userId == null) {
            return false;
        }

        return article.getUserId().equals(userId);
    }

    /**
     * 更新文章浏览数
     * @param article 文章实体
     * @return 更新后的文章实体
     */
    public ArticleEntity incrementViewCount(ArticleEntity article) {
        if (article == null) {
            throw new IllegalArgumentException("文章不能为空");
        }

        long currentViewCount = article.getViewCount() != null ? article.getViewCount() : 0;
        article.setViewCount(currentViewCount + 1);
        
        log.info("文章浏览数更新 - articleId: {}, viewCount: {}", article.getId(), article.getViewCount());
        return article;
    }

    /**
     * 验证文章状态（是否已发布）
     * @param article 文章实体
     */
    private void validateArticleStatus(ArticleEntity article) {
        if (article.getStatus() == null || ArticleStatus.DRAFT.equals(article.getStatus())) {
            throw new BusinessException("文章未发布");
        }
    }

    /**
     * 构建用户状态信息
     * @param article 文章实体
     * @param author 作者信息
     * @param currentUserId 当前用户ID
     * @return 用户状态信息
     */
    private ArticleUserState buildUserState(ArticleEntity article, UserEntity author, Long currentUserId) {
        boolean isLiked = false;
        boolean isCollected = false;
        boolean isFollowed = false;
        boolean isAuthor = false;

        if (currentUserId != null) {
            try {
                isLiked = likeService.checkStatus(currentUserId, LikeType.ARTICLE.getCode(), article.getId());
                isCollected = articleCollectService.checkStatus(currentUserId, article.getId());
                isFollowed = followService.checkStatus(currentUserId, author.getId());
                isAuthor = author.getId().equals(currentUserId);
            } catch (Exception e) {
                log.warn("获取用户状态信息失败 - userId: {}, articleId: {}", currentUserId, article.getId(), e);
                // 失败时使用默认值false
            }
        }

        return new ArticleUserState(isLiked, isCollected, isFollowed, isAuthor);
    }

    /**
     * 用户状态信息内部类
     */
    private static class ArticleUserState {
        private final boolean liked;
        private final boolean collected;
        private final boolean followed;
        private final boolean author;

        public ArticleUserState(boolean liked, boolean collected, boolean followed, boolean author) {
            this.liked = liked;
            this.collected = collected;
            this.followed = followed;
            this.author = author;
        }

        public boolean isLiked() {
            return liked;
        }

        public boolean isCollected() {
            return collected;
        }

        public boolean isFollowed() {
            return followed;
        }

        public boolean isAuthor() {
            return author;
        }
    }
}