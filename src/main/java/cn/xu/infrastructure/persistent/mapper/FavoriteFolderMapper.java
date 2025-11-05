package cn.xu.infrastructure.persistent.mapper;

import cn.xu.infrastructure.persistent.po.FavoriteFolder;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 收藏夹Mapper
 */
@Mapper
public interface FavoriteFolderMapper {

    /**
     * 插入收藏夹
     */
    @Insert("INSERT INTO favorite_folder (user_id, name, description, is_public, content_count, create_time, update_time) " +
            "VALUES (#{userId}, #{name}, #{description}, #{isPublic}, #{contentCount}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FavoriteFolder favoriteFolder);

    /**
     * 更新收藏夹
     */
    @Update("UPDATE favorite_folder SET name = #{name}, description = #{description}, " +
            "is_public = #{isPublic}, content_count = #{contentCount}, update_time = #{updateTime} " +
            "WHERE id = #{id}")
    void update(FavoriteFolder favoriteFolder);

    /**
     * 根据ID删除收藏夹
     */
    @Delete("DELETE FROM favorite_folder WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 根据ID查询收藏夹
     */
    @Select("SELECT * FROM favorite_folder WHERE id = #{id}")
    FavoriteFolder selectById(Long id);

    /**
     * 根据用户ID查询收藏夹列表
     */
    @Select("SELECT * FROM favorite_folder WHERE user_id = #{userId} ORDER BY is_default DESC, sort ASC, create_time DESC")
    List<FavoriteFolder> selectByUserId(Long userId);

    /**
     * 根据用户ID和名称查询收藏夹
     */
    @Select("SELECT * FROM favorite_folder WHERE user_id = #{userId} AND name = #{name}")
    FavoriteFolder selectByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    /**
     * 查询用户的默认收藏夹
     */
    @Select("SELECT * FROM favorite_folder WHERE user_id = #{userId} AND is_default = 1")
    FavoriteFolder selectDefaultByUserId(Long userId);

    /**
     * 更新收藏夹内容数量
     */
    @Update("UPDATE favorite_folder SET content_count = #{count} WHERE id = #{folderId}")
    void updateContentCount(@Param("folderId") Long folderId, @Param("count") Integer count);
}