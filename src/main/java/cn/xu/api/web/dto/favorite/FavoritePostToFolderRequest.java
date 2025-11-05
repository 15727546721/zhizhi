package cn.xu.api.web.dto.favorite;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 收藏帖子到收藏夹请求数据
 */
@Data
public class FavoritePostToFolderRequest {
    
    /**
     * 收藏夹ID
     */
    @NotNull(message = "收藏夹ID不能为空")
    private Long folderId;
    
    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
}