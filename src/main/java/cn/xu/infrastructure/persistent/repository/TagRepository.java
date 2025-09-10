package cn.xu.infrastructure.persistent.repository;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.aggregate.ArticleAndTagAgg;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.converter.TagConverter;
import cn.xu.infrastructure.persistent.dao.TagMapper;
import cn.xu.infrastructure.persistent.po.ArticleTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签仓储实现类
 * 通过TagConverter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TagRepository implements ITagRepository {

    private final TagMapper tagDao;
    private final TagConverter tagConverter;

    @Override
    public void save(TagEntity tag) {
        log.info("保存标签: {}", tag);
        if (tag == null) {
            throw new IllegalArgumentException("标签实体不能为空");
        }
        
        try {
            ArticleTag articleTagPO = tagConverter.toDataObject(tag);
            tagDao.insert(articleTagPO);
        } catch (Exception e) {
            log.error("保存标签失败: {}", e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存标签失败");
        }
    }

    @Override
    public List<TagEntity> queryTagList(int page, int size) {
        // 查询标签列表，使用分页
        List<ArticleTag> articleTagList = tagDao.selectListByPage((page - 1) * size, size);
        log.info("查询标签列表，返回结果：{}", articleTagList);
        
        return tagConverter.toDomainEntities(articleTagList);
    }

    @Override
    public void update(TagEntity tagEntity) {
        log.info("更新标签: {}", tagEntity);
        if (tagEntity == null) {
            throw new IllegalArgumentException("标签实体不能为空");
        }
        
        try {
            ArticleTag articleTagPO = tagConverter.toDataObject(tagEntity);
            tagDao.update(articleTagPO);
        } catch (Exception e) {
            log.error("更新标签失败: {}", e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新标签失败");
        }
    }

    @Override
    public void delete(List<Long> idList) {
        log.info("删除标签ID列表: {}", idList);
        try {
            tagDao.delete(idList);
        } catch (Exception e) {
            log.error("删除标签失败: {}", e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除标签失败");
        }
    }

    @Override
    public TagEntity findById(Long tagId) {
        if (tagId == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "标签ID不能为空");
        }
        log.info("查询标签ID: {}", tagId);
        try {
            ArticleTag articleTag = tagDao.selectById(tagId);
            log.info("查询标签结果: {}", articleTag);
            return tagConverter.toDomainEntity(articleTag);
        } catch (Exception e) {
            log.error("查询标签失败: {}", e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询标签失败");
        }
    }

    @Override
    public List<TagEntity> getTagSelectList() {
        // 查询标签列表
        List<ArticleTag> articleTagList = tagDao.selectList();
        log.info("查询标签列表，返回结果：{}", articleTagList);
        
        return tagConverter.toDomainEntities(articleTagList);
    }

    @Override
    public List<TagEntity> getTagsByArticleId(Long articleId) {
        List<ArticleTag> articleTagList = tagDao.selectByArticleId(articleId);
        log.info("查询文章ID: {} 对应的标签: {}", articleId, articleTagList);
        return tagConverter.toDomainEntities(articleTagList);
    }

    @Override
    public List<TagEntity> getTagList() {
        List<ArticleTag> articleTagList = tagDao.selectList();
        log.info("查询标签列表，返回结果：{}", articleTagList);
        return tagConverter.toDomainEntities(articleTagList);
    }

    @Override
    public List<ArticleAndTagAgg> selectByArticleIds(List<Long> articleIds) {
        return tagDao.selectByArticleIds(articleIds);
    }
}
