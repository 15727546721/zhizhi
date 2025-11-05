package cn.xu.domain.favorite.repository;

import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;

import java.util.List;
import java.util.Optional;

/**
 * 收藏夹仓储接口
 */
public interface IFavoriteFolderRepository {

    /**
     * 保存收藏夹
     *
     * @param favoriteFolderEntity 收藏夹实体
     * @return 收藏夹ID
     */
    Long save(FavoriteFolderEntity favoriteFolderEntity);

    /**
     * 更新收藏夹
     *
     * @param favoriteFolderEntity 收藏夹实体
     */
    void update(FavoriteFolderEntity favoriteFolderEntity);

    /**
     * 根据ID删除收藏夹
     *
     * @param id 收藏夹ID
     */
    void deleteById(Long id);

    /**
     * 根据ID查询收藏夹
     *
     * @param id 收藏夹ID
     * @return 收藏夹实体
     */
    Optional<FavoriteFolderEntity> findById(Long id);

    /**
     * 根据用户ID查询收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    List<FavoriteFolderEntity> findByUserId(Long userId);

    /**
     * 根据用户ID和名称查询收藏夹
     *
     * @param userId 用户ID
     * @param name   收藏夹名称
     * @return 收藏夹实体
     */
    Optional<FavoriteFolderEntity> findByUserIdAndName(Long userId, String name);

    /**
     * 查询用户的默认收藏夹
     *
     * @param userId 用户ID
     * @return 默认收藏夹实体
     */
    Optional<FavoriteFolderEntity> findDefaultFolderByUserId(Long userId);

    /**
     * 更新收藏夹内容数量
     *
     * @param folderId 收藏夹ID
     * @param count    内容数量
     */
    void updateContentCount(Long folderId, Integer count);
}