package cn.xu.api.controller.admin.article;

import cn.xu.api.dto.article.ArticleListResponse;
import cn.xu.api.dto.article.ArticleResponse;
import cn.xu.api.dto.article.CreateArticleRequest;
import cn.xu.api.dto.common.PageRequest;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.article.service.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("admin/article")
@RestController
public class ArticleController {

    @Resource
    private IArticleService articleService;

    @Resource
    private ICategoryService categoryService;

    @PostMapping("/uploadCover")
    public ResponseEntity<String> uploadCover(@RequestPart("file") MultipartFile file) {
        String coverUrl = articleService.uploadCover(file);
        return ResponseEntity.<String>builder()
                .data(coverUrl)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("上传封面成功")
                .build();
    }

    @PostMapping("/add")
    public ResponseEntity saveArticle(@RequestBody CreateArticleRequest createArticleRequest) {
        articleService.createArticle(createArticleRequest);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章创建成功")
                .build();
    }

    @PostMapping("/update")
    public ResponseEntity updateArticle(@RequestBody CreateArticleRequest createArticleRequest) {
        articleService.updateArticle(createArticleRequest);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章更新成功")
                .build();
    }

    @PostMapping("/delete")
    public ResponseEntity deleteArticles(@RequestBody List<Long> articleIds) {
        articleService.deleteArticles(articleIds);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章批量删除成功")
                .build();
    }

    @PostMapping("/list")
    public ResponseEntity<List<ArticleListResponse>> listArticle(@ModelAttribute PageRequest pageRequest) {
        log.info("文章列表获取参数: page={}, size={}", pageRequest.getPage(), pageRequest.getSize());
        List<ArticleEntity> articles = articleService.listArticle(pageRequest.getPage(), pageRequest.getSize());
        log.info("文章列表获取结果: {}", articles);


        // 将 ArticleEntity 转换为 ArticleResponse
        List<ArticleListResponse> response = articles.stream()
                .map(article -> ArticleListResponse.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .coverUrl(article.getCoverUrl())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.<List<ArticleListResponse>>builder()
                .data(response)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章列表获取成功")
                .build();
    }

    /**
     * 获取文章详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable("id") Long id) {
        ArticleEntity article = articleService.getArticleById(id);
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setContent(article.getContent());
        response.setCoverUrl(article.getCoverUrl());
        response.setDescription(article.getDescription());
        response.setStatus(article.getStatus());
        response.setCommentEnabled(article.getCommentEnabled());
        return ResponseEntity.<ArticleResponse>builder()
                .data(response)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章获取成功")
                .build();
    }
}
