package cn.xu.domain.article.service;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.domain.article.model.aggregate.ArticleAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.repository.IArticleAggregateRepository;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章查询领域服务
 * 负责文章查询相关的核心业务逻辑，遵循DDD原则
 * 支持使用策略模式在Elasticsearch和MySQL之间切换查询方式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleQueryDomainService {

    private final IArticleAggregateRepository articleAggregateRepository;
    @SuppressWarnings("deprecation")
    private final IArticleRepository articleRepository; // 兼容旧接口，用于VO查询
    private final IUserService userService;
    private final ArticleQueryStrategyFactory strategyFactory; // 策略工厂

    /**
     * 根据ID查询文章
     * @param articleId 文章ID
     * @return 文章实体
     */
    public ArticleEntity findArticleById(Long articleId) {
        if (articleId == null) {
            log.warn("查询文章失败：文章ID为空");
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        ArticleAggregate aggregate = articleAggregateRepository.findById(articleId)
                .orElse(null);
        if (aggregate == null) {
            log.warn("文章不存在 - articleId: {}", articleId);
            throw new BusinessException("文章不存在");
        }
        
        ArticleEntity article = aggregate.getArticleEntity();
        log.debug("文章查询成功 - articleId: {}, title: {}", articleId, article.getTitleValue());
        return article;
    }

    /**
     * 分页查询文章列表
     * @param request 查询请求
     * @return 文章页面VO列表
     */
    @SuppressWarnings("deprecation")
    public List<ArticlePageVO> queryArticleByPage(ArticleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("查询请求不能为空");
        }
        
        validatePageRequest(request);
        return articleRepository.queryArticle(request);
    }

    /**
     * 根据用户ID查询文章列表
     * @param userId 用户ID
     * @return 文章列表
     */
    @SuppressWarnings("deprecation")
    public List<ArticleListVO> queryArticlesByUserId(Long userId) {
        if (userId == null) {
            log.warn("查询用户文章失败：用户ID为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        List<ArticleListVO> articles = articleRepository.queryArticleByUserId(userId);
        log.debug("用户文章查询成功 - userId: {}, count: {}", userId, articles != null ? articles.size() : 0);
        return articles;
    }

    /**
     * 查询用户的草稿文章列表
     * @param userId 用户ID
     * @return 草稿文章列表
     */
    @SuppressWarnings("deprecation")
    public List<ArticleListVO> queryDraftArticlesByUserId(Long userId) {
        if (userId == null) {
            log.warn("查询草稿文章失败：用户ID为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        List<ArticleListVO> drafts = articleRepository.queryDraftArticleListByUserId(userId);
        log.debug("草稿文章查询成功 - userId: {}, count: {}", userId, drafts != null ? drafts.size() : 0);
        return drafts;
    }

    /**
     * 根据分类ID分页查询文章
     * @param categoryId 分类ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 文章实体列表
     */
    public List<ArticleEntity> queryArticleEntitiesByCategoryId(Long categoryId, Integer pageNo, Integer pageSize) {
        validatePaginationParams(pageNo, pageSize);
        
        if (categoryId == null) {
            return convertToArticleEntities(articleAggregateRepository.findPublishedArticles(pageNo, pageSize));
        } else {
            return convertToArticleEntities(articleAggregateRepository.findPublishedByCategoryId(categoryId, pageNo, pageSize));
        }
    }

    /**
     * 查询所有已发布文章
     * @return 已发布文章列表
     */
    public List<ArticleEntity> findAllPublishedArticles() {
        List<ArticleAggregate> aggregates = articleAggregateRepository.findAllPublished();
        return convertToArticleEntities(aggregates);
    }

    /**
     * 查询所有文章（包括草稿）
     * @return 所有文章列表
     */
    @SuppressWarnings("deprecation")
    public List<ArticleEntity> findAllArticles() {
        List<ArticleEntity> articles = articleRepository.findAll();
        return articles != null ? articles : Collections.emptyList();
    }

    /**
     * 根据分类ID查询文章列表（无分页）
     * @param categoryId 分类ID
     * @return 文章列表VO
     */
    @SuppressWarnings("deprecation")
    public List<ArticleListVO> queryArticlesByCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        return articleRepository.queryArticleByCategoryId(categoryId);
    }

    /**
     * 批量查询文章（根据ID列表）
     * @param articleIds 文章ID列表
     * @return 文章实体列表
     */
    public List<ArticleEntity> findArticlesByIds(List<Long> articleIds) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return Collections.emptyList();
        }
        
        // 目前仓储接口没有批量查询方法，这里先用单个查询
        // 后续可以在仓储中添加批量查询方法以提高性能
        return articleIds.stream()
                .map(this::findArticleByIdSafely)
                .filter(article -> article != null)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 安全地根据ID查询文章（不抛异常）
     * @param articleId 文章ID
     * @return 文章实体，不存在返回null
     */
    private ArticleEntity findArticleByIdSafely(Long articleId) {
        try {
            ArticleAggregate aggregate = articleAggregateRepository.findById(articleId)
                    .orElse(null);
            return aggregate != null ? aggregate.getArticleEntity() : null;
        } catch (Exception e) {
            log.warn("查询文章失败: articleId={}", articleId, e);
            return null;
        }
    }

    /**
     * 验证分页请求参数
     */
    private void validatePageRequest(ArticleRequest request) {
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            log.warn("分页参数验证失败：页码无效 - pageNo: {}", request.getPageNo());
            throw new IllegalArgumentException("页码必须大于0");
        }
        
        if (request.getPageSize() == null || request.getPageSize() < 1 || request.getPageSize() > 100) {
            log.warn("分页参数验证失败：页面大小无效 - pageSize: {}", request.getPageSize());
            throw new IllegalArgumentException("页面大小必须在1-100之间");
        }
        
        log.debug("分页参数验证通过 - pageNo: {}, pageSize: {}", request.getPageNo(), request.getPageSize());
    }

    /**
     * 验证分页参数
     */
    private void validatePaginationParams(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 1) {
            log.warn("分页参数验证失败：页码无效 - pageNo: {}", pageNo);
            throw new IllegalArgumentException("页码必须大于0");
        }
        
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            log.warn("分页参数验证失败：页面大小无效 - pageSize: {}", pageSize);
            throw new IllegalArgumentException("页面大小必须在1-100之间");
        }
        
        log.debug("分页参数验证通过 - pageNo: {}, pageSize: {}", pageNo, pageSize);
    }
    
    /**
     * 将文章聚合根列表转换为文章实体列表
     */
    private List<ArticleEntity> convertToArticleEntities(List<ArticleAggregate> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return Collections.emptyList();
        }
        
        return aggregates.stream()
                .map(ArticleAggregate::getArticleEntity)
                .filter(article -> article != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 将文章实体列表转换为ArticleListVO列表
     */
    private List<ArticleListVO> convertToArticleListVOs(List<ArticleEntity> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return Collections.emptyList();
        }
        
        // 获取所有作者ID
        List<Long> userIds = articles.stream()
                .map(ArticleEntity::getUserId)
                .filter(userId -> userId != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量获取用户信息
        Map<Long, UserEntity> userMap = userService.getUserMapByIds(userIds.stream().collect(Collectors.toSet()));
        
        return articles.stream()
                .map(article -> {
                    ArticleListVO vo = new ArticleListVO();
                    vo.setArticle(article);
                    vo.setUser(userMap.get(article.getUserId()));
                    // 标签信息需要另外处理
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 根据标题搜索文章（使用策略模式）
     * @param title 标题关键词
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    public Page<ArticleEntity> searchArticlesByTitle(String title, Pageable pageable) {
        ArticleQueryStrategy strategy = strategyFactory.getCurrentStrategy();
        return strategy.searchByTitle(title, pageable);
    }
    
    /**
     * 获取热门文章排行（使用策略模式）
     * @param rankType 排行类型（日榜、周榜、月榜）
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    public Page<ArticleEntity> getHotRank(String rankType, Pageable pageable) {
        ArticleQueryStrategy strategy = strategyFactory.getCurrentStrategy();
        return strategy.getHotRank(rankType, pageable);
    }
    
    /**
     * 根据分类ID获取文章列表（使用策略模式）
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    public Page<ArticleEntity> getArticlesByCategory(Long categoryId, Pageable pageable) {
        ArticleQueryStrategy strategy = strategyFactory.getCurrentStrategy();
        return strategy.getArticlesByCategory(categoryId, pageable);
    }
    
    /**
     * 根据用户ID获取文章列表（使用策略模式）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    public Page<ArticleEntity> getArticlesByUser(Long userId, Pageable pageable) {
        ArticleQueryStrategy strategy = strategyFactory.getCurrentStrategy();
        return strategy.getArticlesByUser(userId, pageable);
    }
    
    /**
     * 获取文章详情（使用策略模式）
     * @param articleId 文章ID
     * @return 文章实体
     */
    public ArticleEntity getArticleDetail(Long articleId) {
        ArticleQueryStrategy strategy = strategyFactory.getCurrentStrategy();
        return strategy.getArticleDetail(articleId);
    }
}