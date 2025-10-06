package cn.xu.api.system.controller;

import cn.xu.api.system.model.vo.comment.CommentReplyResponse;
import cn.xu.api.web.model.dto.comment.CommentCreateRequest;
import cn.xu.api.web.model.vo.comment.CommentResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.request.PageRequest;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.service.impl.CommentServiceImpl;
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
    private CommentServiceImpl commentServiceImpl;

    @Operation(summary = "管理员删除评论")
    @DeleteMapping("/delete/{id}")
    @ApiOperationLog(description = "管理员删除评论")
    public ResponseEntity<Void> deleteComment(@Parameter(description = "评论ID") @PathVariable @NotNull(message = "评论ID不能为空") Long id) {
        commentServiceImpl.deleteCommentByAdmin(id);
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
    @ApiOperationLog(description = "分页获取一级评论列表")
    public ResponseEntity<PageResponse<List<CommentResponse>>> getComments(@Valid CommentCreateRequest request) {
        log.info("分页获取一级评论列表, request: {}", request);
//        PageResponse<List<CommentResponse>> pageResponse = commentServiceImpl.getRootCommentsWithUserByPage(request);
        return ResponseEntity.<PageResponse<List<CommentResponse>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(null)
                .build();
    }

    @Operation(summary = "分页获取二级评论列表")
    @Parameters({
        @Parameter(name = "parentId", description = "父评论ID", required = true),
        @Parameter(name = "pageNum", description = "页码", required = true),
        @Parameter(name = "pageSize", description = "每页数量", required = true)
    })
    @GetMapping("/replies/{parentId}")
    @ApiOperationLog(description = "分页获取二级评论列表")
    public ResponseEntity<?> getReplies(
            @Parameter(description = "父评论ID") @PathVariable @NotNull(message = "父评论ID不能为空") Long parentId,
            @Valid PageRequest pageRequest) {
        try {
            // 1. 参数校验日志
            log.info("[评论服务] 开始获取二级评论列表 - 父评论ID: {}, 页码: {}, 每页数量: {}", 
                    parentId, pageRequest.getPageNo(), pageRequest.getPageSize());

            // 2. 业务处理前校验父评论是否存在
            CommentEntity parentComment = commentServiceImpl.getCommentById(parentId);
            if (parentComment == null) {
                log.error("[评论服务] 父评论不存在 - ID: {}", parentId);
                return ResponseEntity.<List<CommentReplyResponse>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(String.format("父评论[%d]不存在", parentId))
                        .build();
            }

            // 3. 确保是一级评论
            if (parentComment.getParentId() != null) {
                log.error("[评论服务] 非法的父评论ID - ID: {}, 该评论不是一级评论", parentId);
                return ResponseEntity.<List<CommentReplyResponse>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(String.format("评论[%d]不是一级评论", parentId))
                        .build();
            }

            // 4. 调用服务获取数据
//            List<CommentEntity> replies = commentServiceImpl.getPagedRepliesWithUser(parentId, pageRequest);
            
            // 5. 记录处理结果
//            log.info("[评论服务] 成功获取二级评论列表 - 父评论ID: {}, 获取到 {} 条回复",
//                    parentId, replies.size());

            // 6. 返回结果
            return ResponseEntity.<List<CommentEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(null)
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