package cn.xu.model.vo.home;

import cn.xu.model.entity.Tag;
import cn.xu.model.entity.User;
import cn.xu.model.vo.post.PostListVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 主页数据VO
 * 用于封装主页展示的所有数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDataVO {
    /**
     * 热门帖子列表
     */
    private List<PostListVO> hotPosts;

    /**
     * 最新帖子列表
     */
    private List<PostListVO> latestPosts;

    /**
     * 推荐作者列表
     */
    private List<User> recommendedAuthors;

    /**
     * 热门标签列表
     */
    private List<Tag> hotTags;

}