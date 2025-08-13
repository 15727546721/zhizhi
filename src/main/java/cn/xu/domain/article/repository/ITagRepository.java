package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.aggregate.ArticleAndTagAgg;
import cn.xu.domain.article.model.entity.TagEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ITagRepository {
    void save(TagEntity tag);

    List<TagEntity> queryTagList(int page, int size);

    void update(TagEntity tag);

    void delete(List<Long> idList);

    TagEntity findById(Long tagId);

    List<TagEntity> getTagSelectList();

    List<TagEntity> getTagsByArticleId(Long articleId);

    /**
     * 获取所有标签
     *
     * @return
     */
    List<TagEntity> getTagList();

    /**
     * 根据文章ID获取标签列表
     * @param articleIds
     * @return
     */
    List<ArticleAndTagAgg> selectByArticleIds(@Param("articleIds") List<Long> articleIds);
}
