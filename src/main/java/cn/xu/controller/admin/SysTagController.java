package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.request.PageRequest;
import cn.xu.common.response.ResponseEntity;
import cn.xu.controller.admin.model.dto.post.TagRequest;
import cn.xu.model.entity.Tag;
import cn.xu.service.post.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 标签管理控制器
 * 
 * @author xu
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/api/system/tag")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "标签管理相关接口")
public class SysTagController {

    @Resource(name = "tagService")
    private TagService tagService;

    @PostMapping("/add")
    @Operation(summary = "添加标签")
    @SaCheckLogin
    @SaCheckPermission("system:tag:add")
    @ApiOperationLog(description = "添加标签")
    public ResponseEntity addTag(@RequestBody TagRequest tagRequest) {
        if (ObjectUtils.isEmpty(tagRequest) || ObjectUtils.isEmpty(tagRequest.getName())) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info("标签名称不能为空")
                    .build();
        }
        // 创建标签（带描述和排序）
        Tag tag = tagService.createTag(tagRequest.getName());
        if (tag != null && (tagRequest.getDescription() != null || tagRequest.getSort() != null)) {
            // 如果有额外字段，再更新一次
            tag.setDescription(tagRequest.getDescription());
            tag.setSort(tagRequest.getSort() != null ? tagRequest.getSort() : 1);
            tag.setUpdateTime(java.time.LocalDateTime.now());
            tagService.updateTag(tag);
        }
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("保存标签成功")
                .build();
    }

    @GetMapping("/list")
    @Operation(summary = "获取标签列表")
    @SaCheckLogin
    // @SaCheckPermission("system:tag:list")  // 暂时移除权限检查进行调试
    @ApiOperationLog(description = "获取标签列表")
    public ResponseEntity getTagList(@ModelAttribute PageRequest pageRequest) {
        int page = pageRequest.getPageNo();
        int size = pageRequest.getPageSize();
        log.info("查询标签列表: page={}, size={}", page, size);
        List<Tag> tagList = tagService.getTagList();
        return ResponseEntity.<List<Tag>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询标签列表成功")
                .data(tagList)
                .build();
    }

    @GetMapping("/getTagSelectList")
    @Operation(summary = "获取标签下拉列表")
    @SaCheckLogin
    @ApiOperationLog(description = "获取标签下拉列表")
    public ResponseEntity getTagSelectList() {
        log.info("查询标签下拉列表");
        List<Tag> tagList = tagService.getTagList();
        return ResponseEntity.<List<Tag>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询标签列表成功")
                .data(tagList)
                .build();
    }

    @PostMapping("/update")
    @Operation(summary = "更新标签")
    @SaCheckLogin
    @SaCheckPermission("system:tag:update")
    @ApiOperationLog(description = "更新标签")
    public ResponseEntity updateTag(@RequestBody TagRequest tagRequest) {
        if (ObjectUtils.isEmpty(tagRequest) || tagRequest.getId() == null) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        // 构建Tag对象进行完整更新
        Tag tag = new Tag();
        tag.setId(tagRequest.getId());
        tag.setName(tagRequest.getName());
        tag.setDescription(tagRequest.getDescription());
        tag.setSort(tagRequest.getSort());
        tag.setUpdateTime(java.time.LocalDateTime.now());
        tagService.updateTag(tag);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新标签成功")
                .build();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除标签")
    @SaCheckLogin
    @SaCheckPermission("system:tag:delete")
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

    @GetMapping("/top")
    @Operation(summary = "置顶/取消置顶标签")
    @SaCheckLogin
    @SaCheckPermission("system:tag:update")
    @ApiOperationLog(description = "置顶/取消置顶标签")
    public ResponseEntity toggleTopTag(@Parameter(description = "标签ID") @RequestParam("id") Long tagId) {
        if (tagId == null) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info("标签ID不能为空")
                    .build();
        }
        tagService.toggleRecommended(tagId);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("操作成功")
                .build();
    }
}

