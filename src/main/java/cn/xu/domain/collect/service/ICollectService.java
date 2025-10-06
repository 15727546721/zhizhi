package cn.xu.domain.collect.service;

import cn.xu.domain.collect.model.entity.CollectEntity;
import cn.xu.domain.collect.model.entity.CollectFolderEntity;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface ICollectService {
    /**
     * 收藏内容
     */
    void collect(Long userId, Long targetId, String targetType);

    /**
     * 取消收藏
     */
    void uncollect(Long userId, Long targetId, String targetType);

    /**
     * 检查是否已收藏
     */
    boolean isCollected(Long userId, Long targetId, String targetType);

    /**
     * 获取用户收藏的内容ID列表
     */
    List<Long> getCollectedTargetIds(Long userId, String targetType);

    /**
     * 统计用户收藏的内容数量
     */
    int countCollectedItems(Long userId, String targetType);

    /**
     * 创建收藏夹
     */
    Long createFolder(Long userId, String name, String description);

    /**
     * 更新收藏夹
     */
    void updateFolder(Long folderId, String name, String description);

    /**
     * 删除收藏夹
     */
    void deleteFolder(Long userId, Long folderId);

    /**
     * 获取用户的收藏夹列表
     */
    List<CollectFolderEntity> getFoldersByUserId(Long userId);

    /**
     * 将内容添加到收藏夹
     */
    void addTargetToFolder(Long userId, Long targetId, String targetType, Long folderId);

    /**
     * 从收藏夹中移除内容
     */
    void removeTargetFromFolder(Long userId, Long targetId, String targetType, Long folderId);

    /**
     * 获取收藏夹中的内容列表
     */
    List<CollectEntity> getTargetsInFolder(Long userId, Long folderId);
}