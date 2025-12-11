package cn.xu.model.vo.home;

import cn.xu.model.vo.post.PostListVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 个性化主页数据Response
 * 用于封装用户个性化的主页数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizedHomeDataVO {
    /**
     * 关注用户的帖子列表
     */
    private List<PostListVO> followingPosts;
}
