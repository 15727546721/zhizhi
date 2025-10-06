package cn.xu.api.system.controller;

import cn.xu.api.system.model.dto.post.TagRequest;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.request.PageRequest;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.post.service.ITagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "帖子标签管理", description = "帖子标签管理相关接口")
@Slf4j
@RequestMapping("system/tag")
@RestController
public class SysTagController {

    @Resource
    private ITagService tagService;

    @PostMapping("/add")
    @Operation(summary = "添加标签", description = "添加新的帖子标签")
    @ApiOperationLog(description = "添加标签")
    public ResponseEntity addTag(@RequestBody TagRequest tagRequest) {
        if (ObjectUtils.isEmpty(tagRequest)) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        TagEntity tagEntity = TagEntity.builder()
                .name(tagRequest.getName())
                .build();
        tagService.createTag(tagEntity.getName());
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("保存标签成功")
                .build();
    }

    @GetMapping("/list")
    @Operation(summary = "获取标签列表", description = "分页获取帖子标签列表")
    @ApiOperationLog(description = "获取标签列表")
    public ResponseEntity getTagList(@ModelAttribute PageRequest pageRequest) {
        int page = pageRequest.getPageNo();
        int size = pageRequest.getPageSize();
        log.info("查询标签列表: page={}, size={}", page, size);
        List<TagEntity> tagEntityList = tagService.getTagList();
        return ResponseEntity.<List<TagEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询标签列表成功")
                .data(tagEntityList)
                .build();
    }

    @GetMapping("/getTagSelectList")
    @Operation(summary = "获取标签下拉列表", description = "获取所有标签的下拉列表选项")
    @ApiOperationLog(description = "获取标签下拉列表")
    public ResponseEntity getTagSelectList() {
        log.info("查询标签下拉列表");
        List<TagEntity> tagEntityList = tagService.getTagList();
        return ResponseEntity.<List<TagEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询标签列表成功")
                .data(tagEntityList)
                .build();
    }

    @PostMapping("/update")
    @Operation(summary = "更新标签", description = "更新帖子标签信息")
    @ApiOperationLog(description = "更新标签")
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
                .build();
        tagService.updateTag(tagEntity.getId(), tagEntity.getName());
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新标签成功")
                .build();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除标签", description = "删除指定的帖子标签")
    @ApiOperationLog(description = "删除标签")
    public ResponseEntity deleteTag(@Parameter(description = "标签ID列表") @RequestBody List<Long> idList) {
        if (idList.isEmpty()) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        tagService.deleteTag(idList.get(0)); // 假设只删除一个标签
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除标签成功")
                .build();
    }
}
