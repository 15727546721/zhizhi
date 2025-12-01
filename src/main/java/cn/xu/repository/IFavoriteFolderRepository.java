package cn.xu.repository;

import cn.xu.model.entity.FavoriteFolder;

import java.util.List;
import java.util.Optional;

/**
 * 收藏夹仓储接口
 *
 * @author xu
 */
public interface IFavoriteFolderRepository {

    /**
     * 保存收藏夹
     */
    Long save(FavoriteFolder favoriteFolder);

    /**
     * 更新收藏夹
     */
    void update(FavoriteFolder favoriteFolder);

    /**
     * 根据ID删除收藏夹
     */
    void deleteById(Long id);

    /**
     * 根据ID查询收藏夹
     */
    Optional<FavoriteFolder> findById(Long id);

    /**
     * 根据用户ID查询收藏夹列表
     */
    List<FavoriteFolder> findByUserId(Long userId);

    /**
     * 根据用户ID和名称查询收藏夹
     */
    Optional<FavoriteFolder> findByUserIdAndName(Long userId, String name);

    /**
     * 查询用户的默认收藏夹
     */
    Optional<FavoriteFolder> findDefaultFolderByUserId(Long userId);

    /**
     * 更新收藏夹内容数量
     */
    void updateContentCount(Long folderId, Integer count);
}
