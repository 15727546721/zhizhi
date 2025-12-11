package cn.xu.repository;

import cn.xu.model.entity.Favorite;

import java.util.List;

/**
 * 收藏仓储接口
 */
public interface IFavoriteRepository {
    
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
    List<Long> findFavoritedTargetIdsByUserIdWithPage(Long userId, String targetType, Long folderId, int offset, int limit);

    /**
     * 统计用户收藏的内容数量
     */
    int countFavoritedItemsByUserId(Long userId, String targetType);
    
    /**
     * 统计特定目标的收藏数量
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
     * 查询收藏夹中的所有内容
     */
    List<Favorite> findTargetsInFolder(Long userId, Long folderId);
    
    /**
     * 批量更新收藏夹ID
     * @param userId 用户ID
     * @param oldFolderId 旧收藏夹ID
     * @param newFolderId 新收藏夹ID（null表示移除）
     */
    void batchUpdateFolderId(Long userId, Long oldFolderId, Long newFolderId);

    /**
     * 获取收藏某个目标的用户ID列表
     */
    List<Long> findUserIdsByTarget(Long targetId, String targetType);
    
    /**
     * 统计用户收藏总数
     */
    long countByUserId(Long userId);
}
