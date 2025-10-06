package cn.xu.api.web.model.vo.home;

import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.domain.post.model.entity.CategoryEntity;
import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 主页数据Response
 * 用于封装主页展示的所有数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDataResponse {
    /**
     * 热门帖子列表
     */
    private List<PostListResponse> hotPosts;

    /**
     * 最新帖子列表
     */
    private List<PostListResponse> latestPosts;

    /**
     * 推荐作者列表
     */
    private List<UserEntity> recommendedAuthors;

    /**
     * 热门标签列表
     */
    private List<TagEntity> hotTags;

    /**
     * 分类列表
     */
    private List<CategoryEntity> categories;
}