package cn.xu.api.web.controller;

import cn.xu.api.web.model.vo.comment.HotCommentResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.service.HotCommentDomainService;
import cn.xu.infrastructure.persistent.converter.HotCommentConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 热点评论控制器
 * 提供热点评论相关的API接口
 * 
 * @author Lily
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/v1/hot-comment")
@Tag(name = "热点评论接口", description = "热点评论相关接口")
public class HotCommentController {

    private final HotCommentDomainService hotCommentDomainService;
    private final HotCommentConverter hotCommentConverter;

    /**
     * 获取指定目标的热门评论
     * 
     * @param targetType 评论目标类型 (1-文章, 2-随笔)
     * @param targetId 目标ID
     * @param pageNo 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @return 热门评论列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取指定目标的热门评论")
    @ApiOperationLog(description = "获取指定目标的热门评论")
    public ResponseEntity<PageResponse<List<HotCommentResponse>>> getHotComments(
            @Parameter(description = "评论目标类型 (1-文章, 2-随笔)") @RequestParam Integer targetType,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量，默认为10") @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            // 参数校验
            if (targetType == null || targetId == null) {
                return ResponseEntity.<PageResponse<List<HotCommentResponse>>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("参数不能为空")
                    .build();
            }
            
            if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
                return ResponseEntity.<PageResponse<List<HotCommentResponse>>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("分页参数不合法")
                    .build();
            }
            
            List<CommentEntity> hotComments = hotCommentDomainService.getHotComments(
                targetType, targetId, pageNo, pageSize);
            
            // 转换为VO对象
            List<HotCommentResponse> hotCommentVOs = hotComments.stream()
                    .map(hotCommentConverter::convertToVO)
                    .collect(Collectors.toList());
            
            // 构造分页响应
            PageResponse<List<HotCommentResponse>> pageResponse = PageResponse.ofList(pageNo, pageSize, 
                (long) hotCommentVOs.size(), hotCommentVOs);
            
            return ResponseEntity.<PageResponse<List<HotCommentResponse>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("success")
                .data(pageResponse)
                .build();
        } catch (Exception e) {
            log.error("获取热门评论失败: targetType={}, targetId={}, pageNo={}, pageSize={}", 
                     targetType, targetId, pageNo, pageSize, e);
            return ResponseEntity.<PageResponse<List<HotCommentResponse>>>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info("获取热门评论失败")
                .build();
        }
    }

    /**
     * 刷新指定目标的热门评论缓存
     * 
     * @param targetType 评论目标类型
     * @param targetId 目标ID
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新指定目标的热门评论缓存")
    @ApiOperationLog(description = "刷新指定目标的热门评论缓存")
    public ResponseEntity<Boolean> refreshHotCommentCache(
            @Parameter(description = "评论目标类型") @RequestParam Integer targetType,
            @Parameter(description = "目标ID") @RequestParam Long targetId) {
        
        try {
            // 参数校验
            if (targetType == null || targetId == null) {
                return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("参数不能为空")
                    .build();
            }
            
            hotCommentDomainService.refreshHotCommentCache(targetType, targetId);
            
            return ResponseEntity.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("success")
                .data(true)
                .build();
        } catch (Exception e) {
            log.error("刷新热门评论缓存失败: targetType={}, targetId={}", targetType, targetId, e);
            return ResponseEntity.<Boolean>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info("刷新热门评论缓存失败")
                .build();
        }
    }
}