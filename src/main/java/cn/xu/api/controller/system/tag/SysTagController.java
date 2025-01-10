package cn.xu.api.controller.system.tag;

import cn.xu.api.dto.article.TagRequest;
import cn.xu.api.dto.common.PageRequest;
import cn.xu.api.dto.common.ResponseEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.service.ITagService;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "文章标签管理", description = "文章标签管理相关接口")
@Slf4j
@RequestMapping("system/tag")
@RestController
public class SysTagController {

    @Resource
    private ITagService tagService;

    @PostMapping("/add")
    public ResponseEntity addTag(@RequestBody TagRequest tagRequest) {
        if (ObjectUtils.isEmpty(tagRequest)) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        TagEntity tagEntity = TagEntity.builder()
                .name(tagRequest.getName())
                .description(tagRequest.getDescription())
                .build();
        tagService.save(tagEntity);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("保存标签成功")
                .build();
    }

    @GetMapping("/list")
    public ResponseEntity getTagList(@ModelAttribute PageRequest pageRequest) {
        int page = pageRequest.getPageNo();
        int size = pageRequest.getPageSize();
        log.info("查询标签列表: page={}, size={}", page, size);
        List<TagEntity> tagEntityList = tagService.queryTagList(page, size);
        return ResponseEntity.<List<TagEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询标签列表成功")
                .data(tagEntityList)
                .build();
    }

    @GetMapping("/getTagSelectList")
    public ResponseEntity getTagSelectList() {
        log.info("查询标签下拉列表");
        List<TagEntity> tagEntityList = tagService.getTagSelectList();
        return ResponseEntity.<List<TagEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询标签列表成功")
                .data(tagEntityList)
                .build();
    }

    @PostMapping("/update")
    public ResponseEntity updateTag(@RequestBody TagRequest tagRequest) {
        if (ObjectUtils.isEmpty(tagRequest)) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        TagEntity tagEntity = TagEntity.builder()
                .id(tagRequest.getId())
                .name(tagRequest.getName())
                .description(tagRequest.getDescription())
                .build();
        tagService.update(tagEntity);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新标签成功")
                .build();
    }

    @PostMapping("/delete")
    public ResponseEntity deleteTag(@RequestBody List<Long> idList) {
        if (idList.isEmpty()) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        tagService.delete(idList);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除标签成功")
                .build();
    }
}

