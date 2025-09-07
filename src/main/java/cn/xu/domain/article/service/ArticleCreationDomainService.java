package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.file.service.MinioService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文章创建领域服务
 * 负责文章创建相关的业务逻辑，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCreationDomainService {

    private final IArticleRepository articleRepository;
    private final MinioService minioService;

    /**
     * 创建文章
     * @param articleEntity 文章实体
     * @return 文章ID
     */
    public Long createArticle(ArticleEntity articleEntity) {
        if (articleEntity == null) {
            throw new IllegalArgumentException("文章信息不能为空");
        }

        validateArticleEntity(articleEntity);

        try {
            log.info("开始创建文章 - title: {}, userId: {}", articleEntity.getTitle(), articleEntity.getUserId());

            Long articleId = articleRepository.save(articleEntity);
            log.info("文章创建成功 - articleId: {}", articleId);

            return articleId;
        } catch (Exception e) {
            log.error("创建文章失败 - title: {}", articleEntity.getTitle(), e);
            throw new BusinessException("创建文章失败：" + e.getMessage());
        }
    }

    /**
     * 创建或更新文章草稿
     * @param articleEntity 文章实体
     * @return 文章ID
     */
    public Long createOrUpdateArticleDraft(ArticleEntity articleEntity) {
        if (articleEntity == null) {
            throw new IllegalArgumentException("文章信息不能为空");
        }

        validateArticleEntity(articleEntity);

        try {
            log.info("开始创建/更新文章草稿 - title: {}, userId: {}", 
                    articleEntity.getTitle(), articleEntity.getUserId());

            Long articleId;
            if (articleEntity.getId() == null) {
                // 创建新草稿
                articleId = articleRepository.save(articleEntity);
                log.info("文章草稿创建成功 - articleId: {}", articleId);
            } else {
                // 更新现有草稿
                articleRepository.update(articleEntity);
                articleId = articleEntity.getId();
                log.info("文章草稿更新成功 - articleId: {}", articleId);
            }

            return articleId;
        } catch (Exception e) {
            log.error("创建/更新文章草稿失败 - title: {}", articleEntity.getTitle(), e);
            throw new BusinessException("创建文章草稿失败：" + e.getMessage());
        }
    }

    /**
     * 上传文章封面
     * @param imageFile 图片文件
     * @return 上传后的图片URL
     */
    public String uploadCover(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("图片文件不能为空");
        }

        validateImageFile(imageFile);

        try {
            log.info("开始上传文章封面 - fileName: {}, size: {}", 
                    imageFile.getOriginalFilename(), imageFile.getSize());

            String uploadFileUrl = minioService.uploadFile(imageFile, null);
            if (uploadFileUrl == null) {
                log.error("上传文章封面失败 - 上传服务返回空URL");
                throw new BusinessException("上传封面失败");
            }

            log.info("文章封面上传成功 - imageUrl: {}", uploadFileUrl);
            return uploadFileUrl;
        } catch (Exception e) {
            log.error("上传文章封面失败", e);
            throw new BusinessException("上传封面失败：" + e.getMessage());
        }
    }

    /**
     * 验证文章实体
     * @param articleEntity 文章实体
     */
    private void validateArticleEntity(ArticleEntity articleEntity) {
        if (articleEntity.getTitle() == null || articleEntity.getTitle().getValue().trim().isEmpty()) {
            log.warn("文章创建验证失败：标题为空");
            throw new IllegalArgumentException("文章标题不能为空");
        }

        if (articleEntity.getUserId() == null) {
            log.warn("文章创建验证失败：作者ID为空");
            throw new IllegalArgumentException("文章作者ID不能为空");
        }

        if (articleEntity.getContent() == null || articleEntity.getContent().getValue().trim().isEmpty()) {
            log.warn("文章创建验证失败：内容为空");
            throw new IllegalArgumentException("文章内容不能为空");
        }

        // 验证标题长度
        if (articleEntity.getTitle().length() > 100) {
            log.warn("文章创建验证失败：标题过长 - length: {}", articleEntity.getTitle().length());
            throw new IllegalArgumentException("文章标题不能超过200个字符");
        }

        // 验证描述长度
        if (articleEntity.getDescription() != null && articleEntity.getDescription().length() > 500) {
            log.warn("文章创建验证失败：描述过长 - length: {}", articleEntity.getDescription().length());
            throw new IllegalArgumentException("文章描述不能超过500个字符");
        }

        // 验证内容长度
        if (articleEntity.getContent().getValue().length() > 100000) {
            log.warn("文章创建验证失败：内容过长 - length: {}", articleEntity.getContent().getValue().length());
            throw new IllegalArgumentException("文章内容不能超过100000个字符");
        }

        log.debug("文章实体验证通过 - title: {}, userId: {}", articleEntity.getTitle(), articleEntity.getUserId());
    }

    /**
     * 验证图片文件
     * @param imageFile 图片文件
     */
    private void validateImageFile(MultipartFile imageFile) {
        // 验证文件大小（5MB限制）
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (imageFile.getSize() > maxSize) {
            log.warn("图片文件验证失败：文件过大 - size: {} bytes", imageFile.getSize());
            throw new IllegalArgumentException("图片文件大小不能超过5MB");
        }

        // 验证文件类型
        String contentType = imageFile.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            log.warn("图片文件验证失败：文件类型不支持 - contentType: {}", contentType);
            throw new IllegalArgumentException("只支持JPG、PNG、GIF格式的图片");
        }

        // 验证文件扩展名
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null || !isValidImageExtension(originalFilename)) {
            log.warn("图片文件验证失败：文件扩展名不正确 - filename: {}", originalFilename);
            throw new IllegalArgumentException("图片文件扩展名不正确");
        }

        log.debug("图片文件验证通过 - filename: {}, size: {} bytes", originalFilename, imageFile.getSize());
    }

    /**
     * 检查是否为有效的图片类型
     * @param contentType 内容类型
     * @return 是否有效
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif");
    }

    /**
     * 检查是否为有效的图片扩展名
     * @param filename 文件名
     * @return 是否有效
     */
    private boolean isValidImageExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".jpg") ||
               lowerCaseFilename.endsWith(".jpeg") ||
               lowerCaseFilename.endsWith(".png") ||
               lowerCaseFilename.endsWith(".gif");
    }
}