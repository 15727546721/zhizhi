package cn.xu.model.dto.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 获取我的收藏列表请求DTO
 */
@Data
@Schema(description = "获取我的收藏列表请求")
public class GetMyFavoritesRequest {
    
    @Schema(description = "页码，从1开始", example = "1")
    private Integer pageNo = 1;
    
    @Schema(description = "每页数量，最大100", example = "10")
    private Integer pageSize = 10;
    
    @Schema(description = "目标类型：POST", example = "POST")
    private String type = "POST";
    
    @Schema(description = "收藏夹ID（可选，不传则查询全部收藏）")
    private Long folderId;
    
    /**
     * 获取安全的页码
     */
    public int getSafePageNo() {
        return pageNo != null && pageNo >= 1 ? pageNo : 1;
    }
    
    /**
     * 获取安全的每页数量
     */
    public int getSafePageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
    
    /**
     * 获取偏移量
     */
    public int getOffset() {
        return (getSafePageNo() - 1) * getSafePageSize();
    }
}
