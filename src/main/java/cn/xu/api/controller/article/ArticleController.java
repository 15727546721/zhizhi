package cn.xu.api.controller.article;

import cn.xu.api.dto.request.article.ArticleCreateDTO;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.service.IArticleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

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
    public ResponseEntity saveArticle(ArticleCreateDTO articleCreateDTO) {
        articleService.createArticle(articleCreateDTO);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章创建成功")
                .build();
    }
}
