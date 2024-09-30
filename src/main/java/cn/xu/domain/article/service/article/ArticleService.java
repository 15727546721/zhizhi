package cn.xu.domain.article.service.article;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.dto.article.CreateArticleRequest;
import cn.xu.common.Constants;
import cn.xu.domain.article.model.aggregate.ArticleAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.ArticleTagEntity;
import cn.xu.domain.article.model.valobj.TagVO;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.persistent.po.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

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
        long authorId = StpUtil.getLoginIdAsLong();// 获取当前登录用户ID

        // 创建文章实体
        ArticleEntity articleEntity = new ArticleEntity();
        articleEntity.setTitle(createArticleRequest.getTitle());
        articleEntity.setContent(createArticleRequest.getContent());
        articleEntity.setCoverUrl(createArticleRequest.getCoverUrl());
        articleEntity.setAuthorId(authorId);
        articleEntity.setCategoryId(createArticleRequest.getCategoryId());
        articleEntity.setCreateTime(LocalDateTime.now());
        articleEntity.setUpdateTime(LocalDateTime.now());

        // 调用验证方法
        articleEntity.validate();

        // 添加标签
        List<ArticleTagEntity> tags = new LinkedList<>();
        for (Long tagId : createArticleRequest.getTagIds()) {
            tags.add(ArticleTagEntity.builder().articleId(articleEntity.getId()).tagId(tagId).build());
        }

        ArticleAggregate articleAggregate = ArticleAggregate.builder()
                .articleEntity(articleEntity)
                .tags(tags)
                .build();
        // 保存逻辑
        articleRepository.save(articleAggregate);
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
    public List<ArticleEntity> listArticle(int page, int size) {
        List<ArticleEntity> articles = articleRepository.queryArticle(page, size);

        return articles;
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

