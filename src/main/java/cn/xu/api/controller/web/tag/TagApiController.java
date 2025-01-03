package cn.xu.api.controller.web.tag;


import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.service.article.TagService;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/tag")
@Tag(name = "文章标签接口", description = "文章标签相关接口")
public class TagApiController {

    @Resource
    private TagService tagService;

    @GetMapping("/list")
    public ResponseEntity getTagList() {
        List<TagEntity> tagEntityList = tagService.getTagList();
        return ResponseEntity.<List<TagEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询标签列表成功")
                .data(tagEntityList)
                .build();
    }
}
