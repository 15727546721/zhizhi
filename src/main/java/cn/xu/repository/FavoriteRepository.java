package cn.xu.repository;

import cn.xu.model.entity.Favorite;

import java.util.List;

/**
 * 收藏仓储接口
 */
public interface FavoriteRepository {
    
    /**
     * 保存收藏记录
     */
    void save(Favorite favorite);

    /**
     * 根据用户ID和内容ID查找收藏记录
     */
    Favorite findByUserIdAndTargetId(Long userId, Long targetId, String targetType);

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
    List<Long> findFavoritedTargetIdsByUserIdWithPage(Long userId, String targetType, int offset, int limit);

    /**
     * 统计用户收藏的内容数量
     */
    int countFavoritedItemsByUserId(Long userId, String targetType);
    
    /**
     * 统计特定目标的收藏数量
     */
    int countFavoritedItemsByTarget(Long targetId, String targetType);

    /**
     * 获取收藏某个目标的用户ID列表
     */
    List<Long> findUserIdsByTarget(Long targetId, String targetType);
    
    /**
     * 统计用户收藏总数
     */
    long countByUserId(Long userId);
    
    /**
     * 按收藏夹分页获取收藏的内容ID列表
     */
    List<Long> findFavoritedTargetIdsByFolderWithPage(Long userId, String targetType, Long folderId, int offset, int limit);
    
    /**
     * 统计收藏夹中的收藏数量
     */
    int countFavoritedItemsByFolder(Long userId, String targetType, Long folderId);
    
    /**
     * 更新收藏的收藏夹
     */
    void updateFolderId(Long id, Long userId, Long newFolderId);
    
    /**
     * 批量移动收藏到新收藏夹
     */
    int moveFavoritesToFolder(Long userId, Long oldFolderId, Long newFolderId);
}
