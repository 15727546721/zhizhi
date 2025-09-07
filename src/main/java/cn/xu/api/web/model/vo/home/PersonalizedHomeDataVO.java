package cn.xu.api.web.model.vo.home;

import cn.xu.api.web.model.vo.article.ArticleListVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 个性化主页数据VO
 * 用于封装用户个性化的主页数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizedHomeDataVO {
    /**
     * 关注用户的文章列表
     */
    private List<ArticleListVO> followingArticles;
}