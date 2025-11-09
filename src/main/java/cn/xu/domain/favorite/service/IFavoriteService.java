package cn.xu.domain.favorite.service;

import cn.xu.domain.favorite.model.entity.FavoriteEntity;
import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface IFavoriteService {
    /**
     * 收藏内容
     */
    void favorite(Long userId, Long targetId, String targetType);

    /**
     * 取消收藏
     */
    void unfavorite(Long userId, Long targetId, String targetType);

    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long userId, Long targetId, String targetType);

    /**
     * 获取用户收藏的内容ID列表
     */
    List<Long> getFavoritedTargetIds(Long userId, String targetType);

    /**
     * 分页获取用户收藏的内容ID列表
     */
    List<Long> getFavoritedTargetIdsWithPage(Long userId, String targetType, Long folderId, int offset, int limit);

    /**
     * 统计用户收藏的内容数量
     */
    int countFavoritedItems(Long userId, String targetType);

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
    List<FavoriteFolderEntity> getFoldersByUserId(Long userId);

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
    List<FavoriteEntity> getTargetsInFolder(Long userId, Long folderId);
    
    /**
     * 根据目标ID和类型统计收藏数
     */
    int countFavoritedItemsByTarget(Long targetId, String targetType);
}