package cn.xu.api.web.controller.article;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.request.article.CreateCollectFolderRequest;
import cn.xu.api.web.model.request.article.UpdateCollectFolderRequest;
import cn.xu.api.web.model.vo.article.CollectFolderVO;
import cn.xu.application.service.CollectFolderApplicationService;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏夹控制器
 * 提供收藏夹相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/article/collect/folder")
@SaCheckLogin
@Tag(name = "收藏夹管理")
@RequiredArgsConstructor
public class CollectFolderController {

    @Resource
    private CollectFolderApplicationService collectFolderApplicationService;

    /**
     * 创建收藏夹
     *
     * @param request 创建收藏夹请求
     * @return 收藏夹ID
     */
    @PostMapping
    @Operation(summary = "创建收藏夹", description = "创建一个新的收藏夹")
    public ResponseEntity<Long> createFolder(@Valid @RequestBody CreateCollectFolderRequest request) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            Long folderId = collectFolderApplicationService.createFolder(
                    currentUserId,
                    request.getName(),
                    request.getDescription(),
                    request.getIsPublic() != null && request.getIsPublic() == 1
            );
            return ResponseEntity.success(folderId);
        } catch (BusinessException e) {
            return ResponseEntity.fail(e.getMessage());
        } catch (Exception e) {
            log.error("创建收藏夹失败", e);
            return ResponseEntity.fail("创建收藏夹失败");
        }
    }

    /**
     * 更新收藏夹
     *
     * @param folderId 收藏夹ID
     * @param request  更新收藏夹请求
     * @return 是否成功
     */
    @PostMapping("/update/{folderId}")
    @Operation(summary = "更新收藏夹", description = "更新收藏夹信息")
    public ResponseEntity<Boolean> updateFolder(
            @Parameter(description = "收藏夹ID") @PathVariable Long folderId,
            @Valid @RequestBody UpdateCollectFolderRequest request) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            collectFolderApplicationService.updateFolder(
                    folderId,
                    currentUserId,
                    request.getName(),
                    request.getDescription(),
                    request.getIsPublic() != null && request.getIsPublic() == 1
            );
            return ResponseEntity.success(true);
        } catch (BusinessException e) {
            return ResponseEntity.fail(e.getMessage());
        } catch (Exception e) {
            log.error("更新收藏夹失败 - folderId: {}", folderId, e);
            return ResponseEntity.fail("更新收藏夹失败");
        }
    }

    /**
     * 删除收藏夹
     *
     * @param folderId 收藏夹ID
     * @return 是否成功
     */
    @PostMapping("/delete/{folderId}")
    @Operation(summary = "删除收藏夹", description = "删除指定的收藏夹")
    public ResponseEntity<Boolean> deleteFolder(
            @Parameter(description = "收藏夹ID") @PathVariable Long folderId) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            collectFolderApplicationService.deleteFolder(folderId, currentUserId);
            return ResponseEntity.success(true);
        } catch (BusinessException e) {
            return ResponseEntity.fail(e.getMessage());
        } catch (Exception e) {
            log.error("删除收藏夹失败 - folderId: {}", folderId, e);
            return ResponseEntity.fail("删除收藏夹失败");
        }
    }

    /**
     * 获取用户的收藏夹列表
     *
     * @return 收藏夹列表
     */
    @GetMapping
    @Operation(summary = "获取用户收藏夹列表", description = "获取当前用户的所有收藏夹")
    public ResponseEntity<List<CollectFolderVO>> getUserFolders() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            List<CollectFolderEntity> folders = collectFolderApplicationService.getUserFolders(currentUserId);
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