package cn.xu.api.web.controller;

import cn.xu.api.web.model.vo.comment.HotCommentVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.service.HotCommentDomainService;
import cn.xu.infrastructure.common.response.PageResponse;
import cn.xu.infrastructure.common.response.ResponseEntity;
import cn.xu.infrastructure.persistent.converter.HotCommentConverter;
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
    public ResponseEntity<PageResponse<List<HotCommentVO>>> getHotComments(
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            // 参数校验
            if (targetType == null || targetId == null) {
                return ResponseEntity.<PageResponse<List<HotCommentVO>>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("参数不能为空")
                    .build();
            }
            
            if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
                return ResponseEntity.<PageResponse<List<HotCommentVO>>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("分页参数不合法")
                    .build();
            }
            
            List<CommentEntity> hotComments = hotCommentDomainService.getHotComments(
                targetType, targetId, pageNo, pageSize);
            
            // 转换为VO对象
            List<HotCommentVO> hotCommentVOs = hotComments.stream()
                    .map(hotCommentConverter::convertToVO)
                    .collect(Collectors.toList());
            
            // 构造分页响应
            PageResponse<List<HotCommentVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, 
                (long) hotCommentVOs.size(), hotCommentVOs);
            
            return ResponseEntity.<PageResponse<List<HotCommentVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("success")
                .data(pageResponse)
                .build();
        } catch (Exception e) {
            log.error("获取热门评论失败: targetType={}, targetId={}, pageNo={}, pageSize={}", 
                     targetType, targetId, pageNo, pageSize, e);
            return ResponseEntity.<PageResponse<List<HotCommentVO>>>builder()
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
    public ResponseEntity<Boolean> refreshHotCommentCache(
            @RequestParam Integer targetType,
            @RequestParam Long targetId) {
        
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