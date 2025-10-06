package cn.xu.domain.collect.repository;

import cn.xu.domain.collect.model.entity.CollectEntity;
import cn.xu.domain.collect.model.entity.CollectFolderEntity;

import java.util.List;

/**
 * 收藏仓储接口
 */
public interface ICollectRepository {
    /**
     * 保存收藏记录
     */
    void save(CollectEntity collectEntity);

    /**
     * 根据用户ID和内容ID查找收藏记录
     */
    CollectEntity findByUserIdAndTargetId(Long userId, Long targetId, String targetType);

    /**
     * 删除收藏记录
     */
    void deleteByUserIdAndTargetId(Long userId, Long targetId, String targetType);

    /**
     * 获取用户收藏的内容ID列表
     */
    List<Long> findCollectedTargetIdsByUserId(Long userId, String targetType);

    /**
     * 统计用户收藏的内容数量
     */
    int countCollectedItemsByUserId(Long userId, String targetType);

    /**
     * 保存收藏夹
     */
    void saveFolder(CollectFolderEntity folderEntity);

    /**
     * 根据ID查找收藏夹
     */
    CollectFolderEntity findFolderById(Long folderId);

    /**
     * 根据用户ID查找收藏夹列表
     */
    List<CollectFolderEntity> findFoldersByUserId(Long userId);

    /**
     * 删除收藏夹
     */
    void deleteFolderById(Long folderId);

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
    List<CollectEntity> findTargetsInFolder(Long userId, Long folderId);
}