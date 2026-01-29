package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.message.UpdateUserMessageSettingsRequest;
import cn.xu.model.entity.UserSettings;
import cn.xu.model.vo.message.UserMessageSettingsVO;
import cn.xu.service.message.UserMessageSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户消息设置控制器
 */
@Slf4j
@Tag(name = "用户消息设置", description = "用户私信设置相关API")
@RestController
@RequestMapping("/api/user/settings")
@RequiredArgsConstructor
public class UserMessageSettingsController {

    private final UserMessageSettingsService userMessageSettingsService;

    /**
     * 获取当前用户的消息设置
     */
    @Operation(summary = "获取消息设置")
    @GetMapping("/message")
    @ApiOperationLog(description = "获取消息设置")
    public ResponseEntity<UserMessageSettingsVO> getMessageSettings() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            UserSettings settings = userMessageSettingsService.getSettings(userId);
            
            UserMessageSettingsVO vo = convertToVO(settings);
            
            return ResponseEntity.<UserMessageSettingsVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(vo)
                    .build();
        } catch (Exception e) {
            log.error("获取消息设置失败", e);
            return ResponseEntity.<UserMessageSettingsVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取消息设置失败")
                    .build();
        }
    }

    /**
     * 更新当前用户的消息设置
     */
    @Operation(summary = "更新消息设置")
    @PostMapping("/message")
    @ApiOperationLog(description = "更新消息设置")
    public ResponseEntity<Void> updateMessageSettings(@RequestBody UpdateUserMessageSettingsRequest dto) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            userMessageSettingsService.updateSettings(userId, dto.getAllowStrangerMessage());
            
            log.info("用户{}更新消息设置成功", userId);
            
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("更新成功")
                    .build();
        } catch (Exception e) {
            log.error("更新消息设置失败", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("更新消息设置失败")
                    .build();
        }
    }

    /**
     * 重置消息设置为默认值
     */
    @Operation(summary = "重置消息设置")
    @PostMapping("/message/reset")
    @ApiOperationLog(description = "重置消息设置")
    public ResponseEntity<UserMessageSettingsVO> resetMessageSettings() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 重置为默认值：允许陌生人私信
            userMessageSettingsService.updateSettings(userId, true);
            UserSettings settings = userMessageSettingsService.getSettings(userId);
            
            log.info("用户{}重置消息设置成功", userId);
            
            return ResponseEntity.<UserMessageSettingsVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("重置成功")
                    .data(convertToVO(settings))
                    .build();
        } catch (Exception e) {
            log.error("重置消息设置失败", e);
            return ResponseEntity.<UserMessageSettingsVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("重置消息设置失败")
                    .build();
        }
    }

    private UserMessageSettingsVO convertToVO(UserSettings settings) {
        UserMessageSettingsVO vo = new UserMessageSettingsVO();
        vo.setUserId(settings.getUserId());
        vo.setAllowStrangerMessage(settings.getAllowStrangerMessageBool());
        return vo;
    }
}
