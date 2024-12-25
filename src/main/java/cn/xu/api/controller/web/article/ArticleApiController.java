package cn.xu.api.controller.web.article;

import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.IArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("api/article")
@RestController
@Tag(name = "文章接口", description = "文章相关接口")
@Slf4j
public class ArticleApiController {

    @Resource
    private IArticleService articleService;

    @GetMapping("/category")
    @Operation(summary = "通过分类获取文章列表")
    public ResponseEntity<List<ArticleListDTO>> getArticleByCategory(Long categoryId) {
        log.info("获取文章列表，分类ID：{}", categoryId);
        if (categoryId == null || categoryId == 0) {
            categoryId = null;
        }
        List<ArticleListDTO> articleEntityList = articleService.getArticleByCategory(categoryId);
        return ResponseEntity.<List<ArticleListDTO>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(articleEntityList)
                .build();
    }


    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public ResponseEntity<ArticleEntity> getArticleDetail(@PathVariable("id") Long id) {
        ArticleEntity articleById = articleService.getArticleById(id);
        return ResponseEntity.<ArticleEntity>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(articleById)
                .build();
    }
}
