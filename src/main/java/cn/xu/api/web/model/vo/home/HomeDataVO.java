package cn.xu.api.web.model.vo.home;

import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.user.model.entity.UserEntity;
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
     * 热门文章列表
     */
    private List<ArticleListVO> hotArticles;

    /**
     * 最新文章列表
     */
    private List<ArticleListVO> latestArticles;

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