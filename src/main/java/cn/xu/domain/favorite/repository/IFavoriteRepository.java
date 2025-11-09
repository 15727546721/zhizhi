package cn.xu.domain.favorite.repository;

import cn.xu.domain.favorite.model.entity.FavoriteEntity;

import java.util.List;

/**
 * 收藏仓储接口
 */
public interface IFavoriteRepository {
    /**
     * 保存收藏记录
     */
    void save(FavoriteEntity favoriteEntity);

    /**
     * 根据用户ID和内容ID查找收藏记录
     */
    FavoriteEntity findByUserIdAndTargetId(Long userId, Long targetId, String targetType);

    /**
     * 删除收藏记录
     */
    void deleteByUserIdAndTargetId(Long userId, Long targetId, String targetType);

    /**
     * 获取用户收藏的内容ID列表
     */
    List<Long> findFavoritedTargetIdsByUserId(Long userId, String targetType);

    /**
     * 分页获取用户收藏的内容ID列表
     */
    List<Long> findFavoritedTargetIdsByUserIdWithPage(Long userId, String targetType, Long folderId, int offset, int limit);

    /**
     * 统计用户收藏的内容数量
     */
    int countFavoritedItemsByUserId(Long userId, String targetType);
    
    /**
     * 统计特定目标的收藏数量（所有用户）
     */
    int countFavoritedItemsByTarget(Long targetId, String targetType);

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
    List<FavoriteEntity> findTargetsInFolder(Long userId, Long folderId);
    
    /**
     * 获取收藏某个目标的用户ID列表
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 用户ID列表
     */
    List<Long> findUserIdsByTarget(Long targetId, String targetType);
}