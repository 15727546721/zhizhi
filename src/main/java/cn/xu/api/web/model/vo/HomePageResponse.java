package cn.xu.api.web.model.vo;

import cn.xu.api.web.model.vo.post.PostListResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 首页页面Response
 * 用于封装首页展示的所有数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageResponse {
    /**
     * 热门帖子列表
     */
    private List<PostListResponse> hotPosts;

    /**
     * 最新帖子列表
     */
    private List<PostListResponse> latestPosts;

    /**
     * 关注用户的帖子列表
     */
    private List<PostListResponse> followingPosts;
}