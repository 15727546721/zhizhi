package cn.xu.domain.article.service.article;

import cn.xu.api.dto.request.article.ArticleCreateDTO;
import cn.xu.common.Constants;
import cn.xu.domain.article.model.aggregate.ArticleAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.TagVO;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.persistent.po.ArticleTag;
import cn.xu.infrastructure.persistent.po.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class ArticleService implements IArticleService {

    @Resource
    private IArticleRepository articleRepository; // 文章仓储
    @Resource
    private IArticleTagRepository articleTagRepository; // 文章标签仓储

    @Resource
    private ITagRepository tagRepository; // 标签仓储

    @Override
    @Transactional
    public void createArticle(ArticleCreateDTO articleCreateDTO) {
        ArticleAggregate aggregate = new ArticleAggregate();

        // 创建文章实体
        ArticleEntity articleEntity = new ArticleEntity();
        articleEntity.setTitle(articleCreateDTO.getTitle());
        articleEntity.setContent(articleCreateDTO.getContent());
        articleEntity.setAuthorId(articleCreateDTO.getAuthorId());
        articleEntity.setCategoryId(articleCreateDTO.getCategoryId());
        articleEntity.setCreateTime(LocalDateTime.now());
        articleEntity.setUpdateTime(LocalDateTime.now());

        // 调用验证方法
        articleEntity.validate();

        aggregate.setArticleEntity(articleEntity);

        // 添加标签
        for (Long tagId : articleCreateDTO.getTagIds()) {
            TagVO tag = fetchTagById(tagId); // 根据ID查询Tag信息
            aggregate.addTag(tag); // 使用聚合的方法添加标签
        }

        // 保存逻辑
        articleRepository.save(articleEntity);
        for (TagVO tag : aggregate.getTags()) {
            // 保存标签关系
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleEntity.getId());
            articleTagRepository.save(articleTag);
        }
    }

    public TagVO fetchTagById(Long tagId) {
        Tag tagPO = tagRepository.findById(tagId); // 从仓储中查询PO
        if (tagPO == null) {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "未查询到标签");
        }
        // 转换为值对象
        TagVO tag = new TagVO();
        tag.setName(tagPO.getName());
        tag.setDescription(tagPO.getDescription());
        return tag;
    }

}

