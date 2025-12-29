package cn.xu.controller.web;

import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Announcement;
import cn.xu.repository.mapper.AnnouncementMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器（用户端）
 */
@Slf4j
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Tag(name = "公告", description = "公告相关接口")
public class AnnouncementController {

    private final AnnouncementMapper announcementMapper;

    /**
     * 获取公告列表（已发布的）
     */
    @GetMapping
    @Operation(summary = "获取公告列表")
    @ApiOperationLog(description = "获取公告列表")
    public ResponseEntity<PageResponse<List<Announcement>>> getAnnouncementList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Announcement> list = announcementMapper.selectPublishedList(offset, pageSize);
        long total = announcementMapper.countPublished();
        
        return ResponseEntity.<PageResponse<List<Announcement>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(PageResponse.of(pageNo, pageSize, total, list))
                .build();
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情")
    @ApiOperationLog(description = "获取公告详情")
    public ResponseEntity<Announcement> getAnnouncementDetail(@PathVariable Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getStatus() != Announcement.STATUS_PUBLISHED) {
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
}
