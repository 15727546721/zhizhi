package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Announcement;
import cn.xu.repository.mapper.AnnouncementMapper;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告管理控制器（管理端）
 */
@Slf4j
@RestController
@RequestMapping("/api/system/announcements")
@RequiredArgsConstructor
@Tag(name = "公告管理", description = "公告管理接口")
public class SysAnnouncementController {

    private final AnnouncementMapper announcementMapper;

    /**
     * 获取公告列表
     */
    @GetMapping
    @SaCheckLogin
    @SaCheckPermission("system:announcement:list")
    @Operation(summary = "获取公告列表")
    @ApiOperationLog(description = "获取公告列表")
    public ResponseEntity<PageResponse<List<Announcement>>> getList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        int offset = (pageNo - 1) * pageSize;
        List<Announcement> list = announcementMapper.selectListWithFilters(type, status, keyword, offset, pageSize);
        long total = announcementMapper.countWithFilters(type, status, keyword);
        
        return ResponseEntity.<PageResponse<List<Announcement>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(PageResponse.of(pageNo, pageSize, total, list))
                .build();
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "获取公告详情")
    @ApiOperationLog(description = "获取公告详情")
    public ResponseEntity<Announcement> getDetail(@PathVariable Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return ResponseEntity.<Announcement>builder()
                    .code(ResponseCode.NOT_FOUND.getCode())
                    .info("公告不存在")
                    .build();
        }
        return ResponseEntity.<Announcement>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(announcement)
                .build();
    }

    /**
     * 创建公告
     */
    @PostMapping
    @SaCheckLogin
    @SaCheckPermission("system:announcement:add")
    @Operation(summary = "创建公告")
    @ApiOperationLog(description = "创建公告")
    public ResponseEntity<Long> create(@RequestBody AnnouncementRequest request) {
        Long userId = LoginUserUtil.getLoginUserId();
        
        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : Announcement.TYPE_NORMAL)
                .status(Announcement.STATUS_DRAFT)
                .isTop(request.getIsTop() != null ? request.getIsTop() : 0)
                .publisherId(userId)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        announcementMapper.insert(announcement);
        log.info("[公告] 创建公告: id={}, title={}", announcement.getId(), announcement.getTitle());
        
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(announcement.getId())
                .info("创建成功")
                .build();
    }

    /**
     * 更新公告
     */
    @PostMapping("/{id}/update")
    @SaCheckLogin
    @SaCheckPermission("system:announcement:edit")
    @Operation(summary = "更新公告")
    @ApiOperationLog(description = "更新公告")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody AnnouncementRequest request) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.NOT_FOUND.getCode())
                    .info("公告不存在")
                    .build();
        }
        
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        if (request.getType() != null) {
            announcement.setType(request.getType());
        }
        if (request.getIsTop() != null) {
            announcement.setIsTop(request.getIsTop());
        }
        announcement.setUpdateTime(LocalDateTime.now());
        
        announcementMapper.updateById(announcement);
        log.info("[公告] 更新公告: id={}", id);
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新成功")
                .build();
    }

    /**
     * 发布公告
     */
    @PostMapping("/{id}/publish")
    @SaCheckLogin
    @SaCheckPermission("system:announcement:edit")
    @Operation(summary = "发布公告")
    @ApiOperationLog(description = "发布公告")
    public ResponseEntity<Void> publish(@PathVariable Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.NOT_FOUND.getCode())
                    .info("公告不存在")
                    .build();
        }
        
        announcement.setStatus(Announcement.STATUS_PUBLISHED);
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        
        log.info("[公告] 发布公告: id={}", id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("发布成功")
                .build();
    }

    /**
     * 下架公告
     */
    @PostMapping("/{id}/offline")
    @SaCheckLogin
    @SaCheckPermission("system:announcement:edit")
    @Operation(summary = "下架公告")
    @ApiOperationLog(description = "下架公告")
    public ResponseEntity<Void> offline(@PathVariable Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.NOT_FOUND.getCode())
                    .info("公告不存在")
                    .build();
        }
        
        announcement.setStatus(Announcement.STATUS_OFFLINE);
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        
        log.info("[公告] 下架公告: id={}", id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("下架成功")
                .build();
    }

    /**
     * 删除公告
     */
    @PostMapping("/{id}/delete")
    @SaCheckLogin
    @SaCheckPermission("system:announcement:delete")
    @Operation(summary = "删除公告")
    @ApiOperationLog(description = "删除公告")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        log.info("[公告] 删除公告: id={}", id);
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    /**
     * 批量删除公告
     */
    @PostMapping("/batch-delete")
    @SaCheckLogin
    @SaCheckPermission("system:announcement:delete")
    @Operation(summary = "批量删除公告")
    @ApiOperationLog(description = "批量删除公告")
    public ResponseEntity<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.PARAM_ERROR.getCode())
                    .info("请选择要删除的公告")
                    .build();
        }
        
        for (Long id : ids) {
            announcementMapper.deleteById(id);
        }
        log.info("[公告] 批量删除公告: ids={}", ids);
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    /**
     * 置顶/取消置顶
     */
    @PostMapping("/{id}/top")
    @SaCheckLogin
    @SaCheckPermission("system:announcement:edit")
    @Operation(summary = "置顶公告")
    @ApiOperationLog(description = "置顶公告")
    public ResponseEntity<Void> toggleTop(@PathVariable Long id, @RequestParam Integer isTop) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.NOT_FOUND.getCode())
                    .info("公告不存在")
                    .build();
        }
        
        announcement.setIsTop(isTop);
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        
        log.info("[公告] {}公告: id={}", isTop == 1 ? "置顶" : "取消置顶", id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(isTop == 1 ? "置顶成功" : "取消置顶成功")
                .build();
    }

    @Data
    public static class AnnouncementRequest {
        private String title;
        private String content;
        private Integer type;
        private Integer isTop;
    }
}
