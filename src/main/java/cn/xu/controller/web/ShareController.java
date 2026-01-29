package cn.xu.controller.web;

import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.share.ShareRequest;
import cn.xu.model.vo.share.ShareResultVO;
import cn.xu.model.vo.share.ShareStatsVO;
import cn.xu.service.share.ShareService;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 分享控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
@Tag(name = "分享管理", description = "帖子分享相关接口")
public class ShareController {

    private final ShareService shareService;

    /**
     * 分享帖子
     */
    @PostMapping
    @Operation(summary = "分享帖子", description = "记录分享行为，24小时内重复分享不增加计数")
    @ApiOperationLog(description = "分享帖子")
    public ResponseEntity<ShareResultVO> share(
            @Valid @RequestBody ShareRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ip = IpUtils.getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            ShareService.ShareResult result = shareService.share(request, ip, userAgent);

            ShareResultVO vo = ShareResultVO.builder()
                    .shareId(result.shareId())
                    .countIncreased(result.countIncreased())
                    .totalShareCount(result.totalShareCount())
                    .build();

            return ResponseEntity.<ShareResultVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(result.countIncreased() ? "分享成功" : "分享成功（24小时内重复分享）")
                    .data(vo)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<ShareResultVO>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("分享失败: postId={}", request.getPostId(), e);
            return ResponseEntity.<ShareResultVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("分享失败")
                    .build();
        }
    }

    /**
     * 获取帖子分享统计
     */
    @GetMapping("/stats/{postId}")
    @Operation(summary = "获取分享统计", description = "获取帖子的分享统计数据")
    public ResponseEntity<ShareStatsVO> getShareStats(
            @Parameter(description = "帖子ID") @PathVariable Long postId) {
        try {
            ShareStatsVO stats = shareService.getShareStats(postId);
            return ResponseEntity.<ShareStatsVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取成功")
                    .data(stats)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<ShareStatsVO>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("获取分享统计失败: postId={}", postId, e);
            return ResponseEntity.<ShareStatsVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取失败")
                    .build();
        }
    }

    /**
     * 检查用户是否分享过
     */
    @GetMapping("/check/{postId}")
    @Operation(summary = "检查分享状态", description = "检查当前用户是否分享过该帖子")
    public ResponseEntity<Boolean> checkShared(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        try {
            boolean hasShared = shareService.hasUserShared(postId, userId);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(hasShared)
                    .build();
        } catch (Exception e) {
            log.error("检查分享状态失败: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }
}
