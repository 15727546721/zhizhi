package cn.xu.model.vo.favorite;

import cn.xu.model.entity.FavoriteFolder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏夹VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteFolderVO {
    
    /** 收藏夹ID */
    private Long id;
    
    /** 收藏夹名称 */
    private String name;
    
    /** 描述 */
    private String description;
    
    /** 是否公开 */
    private Boolean isPublic;
    
    /** 是否默认收藏夹 */
    private Boolean isDefault;
    
    /** 收藏数量 */
    private Integer itemCount;
    
    /** 封面图URL */
    private String coverUrl;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /**
     * 从实体转换
     */
    public static FavoriteFolderVO fromEntity(FavoriteFolder folder) {
        if (folder == null) {
            return null;
        }
        return FavoriteFolderVO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .description(folder.getDescription())
                .isPublic(folder.isPublicFolder())
                .isDefault(folder.isDefaultFolder())
                .itemCount(folder.getItemCount())
                .coverUrl(folder.getCoverUrl())
                .createTime(folder.getCreateTime())
                .build();
    }
}
