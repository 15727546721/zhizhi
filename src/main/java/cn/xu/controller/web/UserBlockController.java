package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.UserBlock;
import cn.xu.service.message.UserBlockService;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户拉黑控制器
 *
 * <p>提供用户拉黑/取消拉黑、查询拉黑状态、获取拉黑列表等功能
 *
 * 
 */
@Slf4j
@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@Tag(name = "用户拉黑", description = "用户拉黑管理接口")
public class UserBlockController {

    private final UserBlockService userBlockService;

    /**
     * 拉黑用户
     */
    @PostMapping("/{userId}")
    @SaCheckLogin
    @Operation(summary = "拉黑用户")
    @ApiOperationLog(description = "拉黑用户")
    public ResponseEntity<Void> blockUser(
            @Parameter(description = "被拉黑用户ID") @PathVariable Long userId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            userBlockService.blockUser(currentUserId, userId);

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("拉黑成功")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("拉黑用户失败: userId={}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "拉黑失败");
        }
    }

    /**
     * 取消拉黑
     */
    @PostMapping("/unblock/{userId}")
    @SaCheckLogin
    @Operation(summary = "取消拉黑")
    @ApiOperationLog(description = "取消拉黑")
    public ResponseEntity<Void> unblockUser(
            @Parameter(description = "被拉黑用户ID") @PathVariable Long userId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            userBlockService.unblockUser(currentUserId, userId);

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("取消拉黑成功")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消拉黑失败: userId={}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "取消拉黑失败");
        }
    }

    /**
     * 检查是否已拉黑某用户
     */
    @GetMapping("/status/{userId}")
    @SaCheckLogin
    @Operation(summary = "检查拉黑状态")
    @ApiOperationLog(description = "检查拉黑状态")
    public ResponseEntity<Boolean> isBlocked(
            @Parameter(description = "目标用户ID") @PathVariable Long userId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            boolean blocked = userBlockService.isBlocked(currentUserId, userId);

            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(blocked)
                    .build();
        } catch (Exception e) {
            log.error("检查拉黑状态失败: userId={}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "检查失败");
        }
    }

    /**
     * 获取拉黑列表
     */
    @GetMapping
    @SaCheckLogin
    @Operation(summary = "获取拉黑列表")
    @ApiOperationLog(description = "获取拉黑列表")
    public ResponseEntity<List<UserBlock>> getBlockList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            List<UserBlock> list = userBlockService.getBlockList(currentUserId, page, size);

            return ResponseEntity.<List<UserBlock>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(list)
                    .build();
        } catch (Exception e) {
            log.error("获取拉黑列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取列表失败");
        }
    }

    /**
     * 获取拉黑数量
     */
    @GetMapping("/count")
    @SaCheckLogin
    @Operation(summary = "获取拉黑数量")
    @ApiOperationLog(description = "获取拉黑数量")
    public ResponseEntity<Integer> getBlockCount() {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            int count = userBlockService.getBlockCount(currentUserId);

            return ResponseEntity.<Integer>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(count)
                    .build();
        } catch (Exception e) {
            log.error("获取拉黑数量失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取数量失败");
        }
    }
}