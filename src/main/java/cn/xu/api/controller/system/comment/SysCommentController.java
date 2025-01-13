package cn.xu.api.controller.system.comment;

import cn.xu.api.controller.web.comment.request.CommentRequest;
import cn.xu.api.dto.comment.CommentDTO;
import cn.xu.api.dto.comment.CommentReplyDTO;
import cn.xu.api.dto.common.PageRequest;
import cn.xu.api.dto.common.ResponseEntity;
import cn.xu.api.dto.common.PageResponse;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.service.comment.CommentService;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/system/comment")
@Tag(name = "评论管理", description = "评论管理相关接口")
public class SysCommentController {

    @Resource
    private CommentService commentService;

    @Operation(summary = "删除评论")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable @NotNull(message = "评论ID不能为空") Long id) {
        try {
            log.info("删除评论, id: {}", id);
            // 获取评论信息
            CommentEntity comment = commentService.getCommentById(id);
            if (comment == null) {
                return ResponseEntity.<Void>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("评论不存在")
                        .build();
            }
            
            // 判断是否为一级评论
            if (comment.getParentId() == null) {
                // 删除一级评论及其所有子评论
                commentService.deleteCommentWithReplies(id);
            } else {
                // 删除单条二级评论
                commentService.deleteComment(id);
            }
            
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .build();
                    
        } catch (BusinessException e) {
            log.error("删除评论失败 - id: {}, error: {}", id, e.getMessage());
            return ResponseEntity.<Void>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("删除评论发生未知错误 - id: {}", id, e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("删除评论失败：" + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "分页获取一级评论列表")
    @Parameters({
        @Parameter(name = "type", description = "评论类型（可选）：1-文章评论，2-话题评论", required = false),
        @Parameter(name = "pageNo", description = "页码", required = true),
        @Parameter(name = "pageSize", description = "每页数量", required = true)
    })
    @GetMapping("/list")
    public ResponseEntity<PageResponse<List<CommentDTO>>> getComments(@Valid CommentRequest request) {
        log.info("分页获取一级评论列表, request: {}", request);
        PageResponse<List<CommentDTO>> pageResponse = commentService.getRootCommentsWithUserByPage(request);
        return ResponseEntity.<PageResponse<List<CommentDTO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(pageResponse)
                .build();
    }

    @Operation(summary = "分页获取二级评论列表")
    @Parameters({
        @Parameter(name = "pageNum", description = "页码", required = true),
        @Parameter(name = "pageSize", description = "每页数量", required = true)
    })
    @GetMapping("/replies")
    public ResponseEntity<List<CommentReplyDTO>> getReplies(
            @RequestParam @NotNull(message = "父评论ID不能为空") Long parentId,
            @Valid PageRequest pageRequest) {
        log.info("获取二级评论列表, parentId: {}, pageRequest: {}", parentId, pageRequest);
        List<CommentReplyDTO> replies = commentService.getPagedRepliesWithUser(parentId, pageRequest);
        return ResponseEntity.<List<CommentReplyDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(replies)
                .build();
    }
}
