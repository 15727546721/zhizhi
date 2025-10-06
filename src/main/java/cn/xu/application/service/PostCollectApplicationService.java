package cn.xu.application.service;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.collect.model.entity.CollectEntity;
import cn.xu.domain.collect.model.entity.CollectFolderEntity;
import cn.xu.domain.collect.service.ICollectService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 帖子收藏应用服务
 * 负责协调领域服务完成复杂的业务流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostCollectApplicationService {

    private final ICollectService collectService;
    private final IUserService userService;

    /**
     * 收藏帖子
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否收藏成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean collectPost(Long userId, Long postId) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }

        // 验证用户状态
        user.validateCanPerformAction();

        // 执行收藏操作
        collectService.collect(userId, postId, "post");
        return true;
    }

    /**
     * 取消收藏帖子
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否取消收藏成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean uncollectPost(Long userId, Long postId) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }

        // 验证用户状态
        user.validateCanPerformAction();

        // 执行取消收藏操作
        collectService.uncollect(userId, postId, "post");
        return true;
    }

    /**
     * 检查用户是否已收藏指定帖子
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否已收藏
     */
    public boolean isPostCollected(Long userId, Long postId) {
        return collectService.isCollected(userId, postId, "post");
    }

    /**
     * 获取用户收藏的帖子数量
     *
     * @param userId 用户ID
     * @return 收藏的帖子数量
     */
    public int getCollectCount(Long userId) {
        return collectService.countCollectedItems(userId, "post");
    }

    /**
     * 创建收藏夹
     *
     * @param userId      用户ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     * @return 收藏夹实体
     */
    @Transactional(rollbackFor = Exception.class)
    public CollectFolderEntity createFolder(Long userId, String name, String description, Boolean isPublic) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }

        // 验证用户状态
        user.validateCanPerformAction();

        // 执行创建收藏夹操作
        Long folderId = collectService.createFolder(userId, name, description);
        
        // 获取创建的收藏夹
        List<CollectFolderEntity> folders = collectService.getFoldersByUserId(userId);
        return folders.stream()
                .filter(folder -> folder.getId().equals(folderId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 更新收藏夹
     *
     * @param userId      用户ID
     * @param folderId    收藏夹ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFolder(Long userId, Long folderId, String name, String description, Boolean isPublic) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }

        // 验证用户状态
        user.validateCanPerformAction();

        // 执行更新收藏夹操作
        collectService.updateFolder(folderId, name, description);
        return true;
    }

    /**
     * 删除收藏夹
     *
     * @param userId   用户ID
     * @param folderId 收藏夹ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFolder(Long userId, Long folderId) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }

        // 验证用户状态
        user.validateCanPerformAction();

        // 执行删除收藏夹操作
        collectService.deleteFolder(userId, folderId);
        return true;
    }

    /**
     * 获取用户的收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    public List<CollectFolderEntity> getUserFolders(Long userId) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }

        return collectService.getFoldersByUserId(userId);
    }

    /**
     * 获取默认收藏夹
     *
     * @param userId 用户ID
     * @return 默认收藏夹
     */
    public Optional<CollectFolderEntity> getDefaultFolder(Long userId) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }

        List<CollectFolderEntity> folders = collectService.getFoldersByUserId(userId);
        return folders.stream()
                .filter(folder -> folder.getIsDefault() != null && folder.getIsDefault() == 1)
                .findFirst();
    }

    /**
     * 收藏帖子到指定收藏夹
     *
     * @param userId   用户ID
     * @param folderId 收藏夹ID
     * @param postId   帖子ID
     * @return 是否收藏成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean collectPostToFolder(Long userId, Long folderId, Long postId) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }

        // 验证用户状态
        user.validateCanPerformAction();

        // 执行收藏操作
        collectService.addTargetToFolder(userId, postId, "post", folderId);
        return true;
    }

    /**
     * 从收藏夹中移除帖子
     *
     * @param userId   用户ID
     * @param folderId 收藏夹ID
     * @param postId   帖子ID
     * @return 是否移除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean uncollectPostFromFolder(Long userId, Long folderId, Long postId) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }

        // 验证用户状态
        user.validateCanPerformAction();

        // 执行取消收藏操作
        collectService.removeTargetFromFolder(userId, postId, "post", folderId);
        return true;
    }

    /**
     * 获取收藏夹中的帖子列表
     *
     * @param folderId 收藏夹ID
     * @return 收藏夹中的帖子列表
     */
    public List<CollectEntity> getPostsInFolder(Long folderId) {
        // 获取当前用户ID（需要从上下文获取，这里简化处理）
        Long userId = 1L; // 实际应用中需要从安全上下文获取
        return collectService.getTargetsInFolder(userId, folderId);
    }

    /**
     * 检查帖子是否已收藏到指定收藏夹
     *
     * @param folderId 收藏夹ID
     * @param postId   帖子ID
     * @return 是否已收藏
     */
    public boolean isPostInFolder(Long folderId, Long postId) {
        // 获取当前用户ID（需要从上下文获取，这里简化处理）
        Long userId = 1L; // 实际应用中需要从安全上下文获取
        List<CollectEntity> collectEntities = collectService.getTargetsInFolder(userId, folderId);
        return collectEntities.stream()
                .anyMatch(entity -> entity.getTargetId().equals(postId));
    }
}