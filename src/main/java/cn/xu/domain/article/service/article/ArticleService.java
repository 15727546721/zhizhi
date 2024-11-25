package cn.xu.domain.article.service.article;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.Constants;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.TagVO;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.persistent.po.Article;
import cn.xu.infrastructure.persistent.po.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
    public Long createArticle(ArticleEntity articleEntity) {
        long authorId = StpUtil.getLoginIdAsLong();// 获取当前登录用户ID

        // 保存逻辑
        return articleRepository.save(articleEntity);
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

    @Override
    public void deleteArticles(List<Long> articleIds) {
        articleRepository.deleteByIds(articleIds);
    }

    @Override
    public void updateArticle(ArticleEntity articleEntity) {

    }

    @Override
    public ArticleEntity getArticleById(Long id) {

        Article article = articleRepository.findById(id);
        if (article == null) {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "未查询到文章");
        }
        // 转换为实体
        ArticleEntity articleEntity = new ArticleEntity();
        articleEntity.setId(article.getId());
        articleEntity.setTitle(article.getTitle());
        articleEntity.setContent(article.getContent());
        articleEntity.setCoverUrl(article.getCoverUrl());
        articleEntity.setDescription(article.getDescription());

        return articleEntity;
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

