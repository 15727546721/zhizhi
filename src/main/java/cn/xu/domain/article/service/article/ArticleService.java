package cn.xu.domain.article.service.article;

import cn.xu.api.dto.article.CreateArticleRequest;
import cn.xu.api.dto.article.ArticleListResponse;
import cn.xu.common.Constants;
import cn.xu.domain.article.model.aggregate.ArticleAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.TagVO;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.persistent.po.ArticleTag;
import cn.xu.infrastructure.persistent.po.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Resource
    private MinioService minioService; // minio客户端

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createArticle(CreateArticleRequest createArticleRequest) {
        ArticleAggregate aggregate = new ArticleAggregate();

        // 创建文章实体
        ArticleEntity articleEntity = new ArticleEntity();
        articleEntity.setTitle(createArticleRequest.getTitle());
        articleEntity.setContent(createArticleRequest.getContent());
        articleEntity.setCoverUrl(createArticleRequest.getCoverUrl());
        articleEntity.setAuthorId(createArticleRequest.getAuthorId());
        articleEntity.setCategoryId(createArticleRequest.getCategoryId());
        articleEntity.setCreateTime(LocalDateTime.now());
        articleEntity.setUpdateTime(LocalDateTime.now());

        // 调用验证方法
        articleEntity.validate();

        aggregate.setArticleEntity(articleEntity);

        // 添加标签
        for (TagVO tagVO : createArticleRequest.getTags()) {
            TagVO tag = fetchTagById(tagVO.getId()); // 根据ID查询Tag信息
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

    @Override
    public String uploadCover(MultipartFile imageFile) {
        String uploadFileUrl = null;
        try {
            uploadFileUrl = minioService.uploadFile(imageFile, null);
        } catch (Exception e) {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "上传封面失败");
        }
        if (uploadFileUrl == null) {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "上传封面失败");
        }
        return uploadFileUrl;
    }

    @Override
    public ArticleListResponse listArticle(int page, int size) {
        return null;
    }

    public TagVO fetchTagById(Long tagId) {
        Tag tagPO = tagRepository.findById(tagId); // 从仓储中查询PO
        if (tagPO == null) {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "未查询到标签");
        }
        // 转换为值对象
        TagVO tag = new TagVO();
        tag.setName(tagPO.getName());
        return tag;
    }

}

