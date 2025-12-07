package cn.xu.model.vo;

import cn.xu.model.vo.post.PostListVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 首页页面VO
 * 用于封装首页展示的所有数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageVO {
    /**
     * 热门帖子列表
     */
    private List<PostListVO> hotPosts;

    /**
     * 最新帖子列表
     */
    private List<PostListVO> latestPosts;

    /**
     * 关注用户的帖子列表
     */
    private List<PostListVO> followingPosts;
}
