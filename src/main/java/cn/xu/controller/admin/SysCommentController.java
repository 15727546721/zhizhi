package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.controller.admin.model.vo.comment.SysCommentVO;
import cn.xu.model.dto.comment.FindReplyRequest;
import cn.xu.model.entity.Comment;
import cn.xu.service.comment.CommentService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论管理控制器
 * 
 * @author xu
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/api/system/comment")
@Tag(name = "评论管理", description = "评论管理相关接口")
public class SysCommentController {

    @Resource
    private CommentService commentService;

    @Operation(summary = "管理员删除评论")
    @DeleteMapping("/delete/{id}")
    @SaCheckLogin
    @SaCheckPermission("system:comment:delete")
    @ApiOperationLog(description = "管理员删除评论")
    public ResponseEntity<Void> deleteComment(@Parameter(description = "评论ID") @PathVariable @NotNull(message = "评论ID不能为空") Long id) {
        commentService.deleteCommentByAdmin(id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    @Operation(summary = "分页获取一级评论列表")
    @Parameters({
        @Parameter(name = "type", description = "评论类型（可选）：1-帖子评论", required = false),
        @Parameter(name = "userId", description = "用户ID（可选）", required = false),
        @Parameter(name = "pageNo", description = "页码", required = true),
        @Parameter(name = "pageSize", description = "每页数量", required = true)
    })
    @GetMapping("/list")
    @SaCheckLogin
    @SaCheckPermission("system:comment:list")
    @ApiOperationLog(description = "分页获取一级评论列表")
    public ResponseEntity<PageResponse<List<SysCommentVO>>> getComments(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("分页获取一级评论列表, type: {}, userId: {}, pageNo: {}, pageSize: {}", type, userId, pageNo, pageSize);
        
        List<Comment> comments = commentService.findAllRootComments(type, userId, pageNo, pageSize);
        long total = commentService.countAllRootComments(type, userId);
        
        // 转换为 VO
        List<SysCommentVO> voList = comments.stream()
                .map(SysCommentVO::fromComment)
                .collect(Collectors.toList());
        
        PageResponse<List<SysCommentVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, total, voList);
        return ResponseEntity.<PageResponse<List<SysCommentVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(pageResponse)
                .build();
    }

    @Operation(summary = "分页获取二级评论列表")
    @Parameters({
        @Parameter(name = "parentId", description = "父评论ID", required = true),
        @Parameter(name = "pageNum", description = "页码", required = true),
        @Parameter(name = "pageSize", description = "每页数量", required = true)
    })
    @GetMapping("/replies/{parentId}")
    @SaCheckLogin
    @SaCheckPermission("system:comment:list")
    @ApiOperationLog(description = "分页获取二级评论列表")
    public ResponseEntity<List<SysCommentVO>> getReplies(
            @Parameter(description = "父评论ID") @PathVariable @NotNull(message = "父评论ID不能为空") Long parentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        log.info("获取二级评论列表 - 父评论ID: {}, 页码: {}, 每页数量: {}", parentId, pageNum, pageSize);

        // 校验父评论是否存在
        Comment parentComment = commentService.getCommentById(parentId);
        if (parentComment == null) {
            log.warn("父评论不存在 - ID: {}", parentId);
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "父评论不存在");
        }

        // 确保是一级评论
        if (parentComment.getParentId() != null && parentComment.getParentId() > 0) {
            log.warn("非一级评论 - ID: {}", parentId);
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "该评论不是一级评论");
        }

        // 查询子评论
        FindReplyRequest request = new FindReplyRequest();
        request.setParentId(parentId);
        request.setPageNo(pageNum);
        request.setPageSize(pageSize);
        request.setSortType("NEW");
        
        List<Comment> replies = commentService.findChildCommentList(request);
        
        // 转换为 VO
        List<SysCommentVO> voList = replies.stream()
                .map(SysCommentVO::fromComment)
                .collect(Collectors.toList());
        
        log.info("获取到 {} 条回复", voList.size());

        return ResponseEntity.<List<SysCommentVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(voList)
                .build();
    }
}
