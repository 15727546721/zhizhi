package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.vo.comment.SysCommentVO;
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
 * <p>提供后台评论管理功能，包括查看、删除等</p>
 * <p>需要登录并拥有相应权限</p>

 */
@Slf4j
@RestController
@RequestMapping("/api/system/comment")
@Tag(name = "评论管理", description = "评论管理相关接口")
public class SysCommentController {

    @Resource
    private CommentService commentService;

    /**
     * 管理员删除评论
     *
     * <p>后台强制删除评论，无需校验所有权
     * <p>需要system:comment:delete权限
     *
     * @param id 评论ID
     * @return 删除结果
     */
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

    /**
     * 分页获取一级评论列表
     *
     * <p>获取所有顶级评论，支持按类型和用户筛选
     * <p>需要system:comment:list权限
     *
     * @param type 评论类型（可选）- 帖子评论
     * @param userId 用户ID（可选）
     * @param pageNo 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @return 分页的评论列表
     */
    @Operation(summary = "分页获取一级评论列表")
    @Parameters({
            @Parameter(name = "type", description = "评论类型（可选）- 帖子评论", required = false),
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
        log.info("分页获取一级评论列表: type: {}, userId: {}, pageNo: {}, pageSize: {}", type, userId, pageNo, pageSize);

        List<Comment> comments = commentService.findAllRootComments(type, userId, pageNo, pageSize);
        long total = commentService.countAllRootComments(type, userId);

        // 转换成VO
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

    /**
     * 分页获取二级评论列表
     *
     * <p>获取指定一级评论的所有回复
     * <p>需要system:comment:list权限
     *
     * @param parentId 父评论ID（必须是一级评论）
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为50
     * @return 回复列表
     * @throws BusinessException 当父评论不存在或不是一级评论时抛出
     */
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

        // 转换成VO
        List<SysCommentVO> voList = replies.stream()
                .map(SysCommentVO::fromComment)
                .collect(Collectors.toList());

        log.info("获取了 {} 条回复", voList.size());

        return ResponseEntity.<List<SysCommentVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(voList)
                .build();
    }
}