package cn.xu.api.controller.article;

import cn.xu.api.dto.article.CreateArticleRequest;
import cn.xu.api.dto.common.PageRequest;
import cn.xu.api.dto.article.ArticleListResponse;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.service.IArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/article")
@RestController
public class ArticleController {

    @Resource
    private IArticleService articleService;

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
    public ResponseEntity saveArticle(CreateArticleRequest createArticleRequest) {
        articleService.createArticle(createArticleRequest);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章创建成功")
                .build();
    }

//    @PostMapping("/update")
//    public ResponseEntity updateArticle(ArticleCreateDTO articleCreateDTO) {
//        articleService.updateArticle(articleCreateDTO);
//        return ResponseEntity.builder()
//                .code(Constants.ResponseCode.SUCCESS.getCode())
//                .info("文章更新成功")
//                .build();
//    }
//
//    @PostMapping("/delete")
//    public ResponseEntity deleteArticle(Long articleId) {
//        articleService.deleteArticle(articleId);
//        return ResponseEntity.builder()
//                .code(Constants.ResponseCode.SUCCESS.getCode())
//                .info("文章删除成功")
//                .build();
//    }

    @PostMapping("/list")
    public ResponseEntity listArticle(@ModelAttribute PageRequest pageRequest) {
        log.info("文章列表获取参数: page={}, size={}", pageRequest.getPage(), pageRequest.getSize());
        articleService.listArticle(pageRequest.getPage(), pageRequest.getSize());
//        ArticleListResponseDTO data = new ArticleListResponseDTO();
        return ResponseEntity.<ArticleListResponse>builder()
                .data(null)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章列表获取成功")
                .build();
    }
}
