package cn.xu.api.web.controller.article;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.request.article.CollectArticleToFolderRequest;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.CollectFolderVO;
import cn.xu.application.service.ArticleCollectionApplicationService;
import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import cn.xu.domain.article.model.entity.CollectFolderEntity;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章收藏控制器
 * 提供文章收藏相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/article/collect")
@SaCheckLogin
@Tag(name = "文章收藏管理")
@RequiredArgsConstructor
public class ArticleCollectController {

    @Resource
    private ArticleCollectionApplicationService articleCollectionApplicationService;

    /**
     * 收藏文章到默认收藏夹
     *
     * @param articleId 文章ID
     * @return 文章收藏实体
     */
    @PostMapping
    @Operation(summary = "收藏文章到默认收藏夹", description = "将指定文章收藏到用户的默认收藏夹")
    public ResponseEntity<ArticleCollectEntity> collectArticle(
            @Parameter(description = "文章ID") @RequestParam Long articleId) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            ArticleCollectEntity result = articleCollectionApplicationService.collectArticleToDefaultFolder(
                    currentUserId, articleId);
            return ResponseEntity.success(result);
        } catch (BusinessException e) {
            return ResponseEntity.fail(e.getMessage());
        } catch (Exception e) {
            log.error("收藏文章到默认收藏夹失败 - articleId: {}", articleId, e);
            return ResponseEntity.fail("收藏文章失败");
        }
    }

    /**
     * 收藏文章到指定收藏夹
     *
     * @param request 收藏文章请求
     * @return 是否成功
     */
    @PostMapping("/to-folder")
    @Operation(summary = "收藏文章到指定收藏夹", description = "将指定文章收藏到指定收藏夹")
    public ResponseEntity<Boolean> collectArticleToFolder(@Valid @RequestBody CollectArticleToFolderRequest request) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            articleCollectionApplicationService.collectArticleToFolder(
                    currentUserId,
                    request.getArticleId(),
                    request.getFolderId()
            );
            return ResponseEntity.success(true);
        } catch (BusinessException e) {
            return ResponseEntity.fail(e.getMessage());
        } catch (Exception e) {
            log.error("收藏文章到指定收藏夹失败 - folderId: {}, articleId: {}", request.getFolderId(), request.getArticleId(), e);
            return ResponseEntity.fail("收藏文章失败");
        }
    }

    /**
     * 取消收藏文章
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    @PostMapping("/uncollect")
    @Operation(summary = "取消收藏文章", description = "取消收藏指定文章")
    public ResponseEntity<Boolean> uncollectArticle(
            @Parameter(description = "文章ID") @RequestParam Long articleId) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            articleCollectionApplicationService.uncollectArticle(currentUserId, articleId);
            return ResponseEntity.success(true);
        } catch (BusinessException e) {
            return ResponseEntity.fail(e.getMessage());
        } catch (Exception e) {
            log.error("取消收藏文章失败 - articleId: {}", articleId, e);
            return ResponseEntity.fail("取消收藏文章失败");
        }
    }

    /**
     * 检查文章是否已收藏
     *
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    @GetMapping("/status")
    @Operation(summary = "检查文章是否已收藏", description = "检查当前用户是否已收藏指定文章")
    public ResponseEntity<Boolean> isArticleCollected(
            @Parameter(description = "文章ID") @RequestParam Long articleId) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            boolean isCollected = articleCollectionApplicationService.isArticleCollected(currentUserId, articleId);
            return ResponseEntity.success(isCollected);
        } catch (BusinessException e) {
            return ResponseEntity.fail(e.getMessage());
        } catch (Exception e) {
            log.error("检查文章收藏状态失败 - articleId: {}", articleId, e);
            return ResponseEntity.fail("检查收藏状态失败");
        }
    }

    /**
     * 获取用户收藏的文章列表
     *
     * @return 文章列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户收藏的文章列表", description = "获取当前用户收藏的所有文章")
    public ResponseEntity<List<ArticleListVO>> getCollectArticles() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            // 获取用户收藏的文章ID列表
            List<Long> articleIds = articleCollectionApplicationService.getCollectArticleIds(currentUserId);
            // 这里需要根据文章ID列表获取文章详细信息
            // 暂时返回空列表，后续需要实现完整的逻辑
            return ResponseEntity.success(Collections.emptyList());
        } catch (BusinessException e) {
            return ResponseEntity.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取收藏文章列表失败", e);
            return ResponseEntity.fail("获取收藏文章列表失败");
        }
    }

    /**
     * 获取用户的收藏夹列表
     *
     * @return 收藏夹列表
     */
    @GetMapping("/folders")
    @Operation(summary = "获取用户收藏夹列表", description = "获取当前用户的所有收藏夹")
    public ResponseEntity<List<CollectFolderVO>> getUserFolders() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            List<CollectFolderEntity> folders = articleCollectionApplicationService.getUserFolders(currentUserId);
            List<CollectFolderVO> folderVOs = folders.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return ResponseEntity.success(folderVOs);
        } catch (BusinessException e) {
            return ResponseEntity.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取用户收藏夹列表失败", e);
            return ResponseEntity.fail("获取收藏夹列表失败");
        }
    }

    /**
     * 将收藏夹实体转换为VO对象
     *
     * @param entity 收藏夹实体
     * @return 收藏夹VO对象
     */
    private CollectFolderVO convertToVO(CollectFolderEntity entity) {
        if (entity == null) {
            return null;
        }

        CollectFolderVO vo = new CollectFolderVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setIsDefault(entity.getIsDefault() != null && entity.getIsDefault() ? 1 : 0);
        vo.setArticleCount(entity.getArticleCount());
        vo.setIsPublic(entity.getIsPublic() != null && entity.getIsPublic() ? 1 : 0);
        vo.setSort(entity.getSort());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}