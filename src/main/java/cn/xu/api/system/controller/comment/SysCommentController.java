package cn.xu.api.system.controller.comment;

import cn.xu.api.web.controller.comment.request.CommentRequest;
import cn.xu.api.web.model.dto.comment.CommentDTO;
import cn.xu.api.web.model.dto.comment.CommentReplyDTO;
import cn.xu.api.web.model.dto.common.PageRequest;
import cn.xu.api.web.model.dto.common.PageResponse;
import cn.xu.api.web.model.dto.common.ResponseEntity;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.service.comment.CommentService;
import cn.xu.exception.BusinessException;
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

    @Operation(summary = "管理员删除评论")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable @NotNull(message = "评论ID不能为空") Long id) {
        commentService.deleteCommentByAdmin(id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
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
        @Parameter(name = "parentId", description = "父评论ID", required = true),
        @Parameter(name = "pageNum", description = "页码", required = true),
        @Parameter(name = "pageSize", description = "每页数量", required = true)
    })
    @GetMapping("/replies/{parentId}")
    public ResponseEntity<List<CommentReplyDTO>> getReplies(
            @PathVariable @NotNull(message = "父评论ID不能为空") Long parentId,
            @Valid PageRequest pageRequest) {
        try {
            // 1. 参数校验日志
            log.info("[评论服务] 开始获取二级评论列表 - 父评论ID: {}, 页码: {}, 每页数量: {}", 
                    parentId, pageRequest.getPageNo(), pageRequest.getPageSize());

            // 2. 业务处理前校验父评论是否存在
            CommentEntity parentComment = commentService.getCommentById(parentId);
            if (parentComment == null) {
                log.error("[评论服务] 父评论不存在 - ID: {}", parentId);
                return ResponseEntity.<List<CommentReplyDTO>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(String.format("父评论[%d]不存在", parentId))
                        .build();
            }

            // 3. 确保是一级评论
            if (parentComment.getParentId() != null) {
                log.error("[评论服务] 非法的父评论ID - ID: {}, 该评论不是一级评论", parentId);
                return ResponseEntity.<List<CommentReplyDTO>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(String.format("评论[%d]不是一级评论", parentId))
                        .build();
            }

            // 4. 调用服务获取数据
            List<CommentReplyDTO> replies = commentService.getPagedRepliesWithUser(parentId, pageRequest);
            
            // 5. 记录处理结果
            log.info("[评论服务] 成功获取二级评论列表 - 父评论ID: {}, 获取到 {} 条回复", 
                    parentId, replies.size());

            // 6. 返回结果
            return ResponseEntity.<List<CommentReplyDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(replies)
                    .build();

        } catch (BusinessException be) {
            // 业务异常日志记录
            log.error("[评论服务] 获取二级评论列表失败 - 业务异常 - 父评论ID: {}, 错误信息: {}", 
                    parentId, be.getMessage());
            throw be;
        } catch (Exception e) {
            // 系统异常日志记录
            log.error("[评论服务] 获取二级评论列表失败 - 系统异常 - 父评论ID: {}", parentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取评论回复列表失败");
        }
    }
}
