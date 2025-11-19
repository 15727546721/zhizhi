package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.SendPrivateMessageDTO;
import cn.xu.api.web.model.dto.UpdateSystemConfigDTO;
import cn.xu.api.web.model.dto.UpdateUserMessageSettingsDTO;
import cn.xu.api.web.model.vo.*;
import cn.xu.application.service.PrivateMessageApplicationService;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.message.model.aggregate.PrivateMessageAggregate;
import cn.xu.domain.message.model.entity.ConversationEntity;
import cn.xu.domain.message.model.entity.SystemConfigEntity;
import cn.xu.domain.message.model.entity.UserBlockEntity;
import cn.xu.domain.message.model.entity.UserMessageSettingsEntity;
import cn.xu.domain.message.service.PrivateMessageDomainService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 私信控制器
 */
@Tag(name = "私信接口")
@RestController
@RequestMapping("/api/private-messages")
@Validated
@Slf4j
@RequiredArgsConstructor
public class PrivateMessageController {
    
    private final PrivateMessageApplicationService privateMessageApplicationService;
    private final IUserService userService;
    
    @Operation(summary = "发送私信")
    @PostMapping
    @SaCheckLogin
    @ApiOperationLog(description = "发送私信")
    public ResponseEntity<PrivateMessageDomainService.SendMessageResult> sendPrivateMessage(
            @Valid @RequestBody SendPrivateMessageDTO dto) {
        log.info("发送私信，请求参数：{}", dto);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        PrivateMessageDomainService.SendMessageResult result = privateMessageApplicationService
                .sendPrivateMessage(currentUserId, dto.getReceiverId(), dto.getContent());
        
        return ResponseEntity.<PrivateMessageDomainService.SendMessageResult>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .info(result.getMessage())
                .build();
    }
    
    @Operation(summary = "获取两个用户之间的消息列表")
    @GetMapping("/conversations/{userId}")
    @SaCheckLogin
    @ApiOperationLog(description = "获取两个用户之间的消息列表")
    public ResponseEntity<List<PrivateMessageVO>> getMessagesBetweenUsers(
            @Parameter(description = "对方用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取消息列表，对方用户ID：{}，页码：{}，每页数量：{}", userId, pageNo, pageSize);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        List<PrivateMessageAggregate> messages = privateMessageApplicationService
                .getMessagesBetweenUsers(currentUserId, userId, pageNo, pageSize);
        
        // 转换为VO
        List<PrivateMessageVO> voList = messages.stream()
                .map(msg -> convertToVO(msg, currentUserId))
                .collect(Collectors.toList());
        
        return ResponseEntity.<List<PrivateMessageVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(voList)
                .build();
    }
    
    @Operation(summary = "获取对话列表")
    @GetMapping("/conversations")
    @SaCheckLogin
    @ApiOperationLog(description = "获取对话列表")
    public ResponseEntity<List<ConversationVO>> getConversationList(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取对话列表，页码：{}，每页数量：{}", pageNo, pageSize);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        List<ConversationEntity> conversations = privateMessageApplicationService
                .getConversationList(currentUserId, pageNo, pageSize);
        
        // 批量转换为VO（优化N+1问题）
        List<ConversationVO> voList = convertToConversationVOList(conversations, currentUserId);
        
        return ResponseEntity.<List<ConversationVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(voList)
                .build();
    }
    
    @Operation(summary = "标记消息为已读")
    @PostMapping("/conversations/{userId}/read")
    @SaCheckLogin
    @ApiOperationLog(description = "标记消息为已读")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "发送者用户ID", required = true)
            @PathVariable Long userId) {
        log.info("标记消息为已读，发送者用户ID：{}", userId);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        privateMessageApplicationService.markAsRead(currentUserId, userId);
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("标记成功")
                .build();
    }
    
    @Operation(summary = "获取未读消息数")
    @GetMapping("/conversations/{userId}/unread-count")
    @SaCheckLogin
    @ApiOperationLog(description = "获取未读消息数")
    public ResponseEntity<Long> getUnreadCount(
            @Parameter(description = "发送者用户ID", required = true)
            @PathVariable Long userId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        long count = privateMessageApplicationService.getUnreadCount(currentUserId, userId);
        
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }
    
    @Operation(summary = "屏蔽用户")
    @PostMapping("/users/{userId}/block")
    @SaCheckLogin
    @ApiOperationLog(description = "屏蔽用户")
    public ResponseEntity<Void> blockUser(
            @Parameter(description = "被屏蔽用户ID", required = true)
            @PathVariable Long userId) {
        log.info("屏蔽用户，被屏蔽用户ID：{}", userId);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        privateMessageApplicationService.blockUser(currentUserId, userId);
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("屏蔽成功")
                .build();
    }
    
    @Operation(summary = "取消屏蔽用户")
    @DeleteMapping("/users/{userId}/block")
    @SaCheckLogin
    @ApiOperationLog(description = "取消屏蔽用户")
    public ResponseEntity<Void> unblockUser(
            @Parameter(description = "被屏蔽用户ID", required = true)
            @PathVariable Long userId) {
        log.info("取消屏蔽用户，被屏蔽用户ID：{}", userId);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        privateMessageApplicationService.unblockUser(currentUserId, userId);
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("取消屏蔽成功")
                .build();
    }

    @Operation(summary = "查询是否被对方屏蔽")
    @GetMapping("/users/{userId}/block-status")
    @SaCheckLogin
    @ApiOperationLog(description = "查询是否被对方屏蔽")
    public ResponseEntity<Map<String, Boolean>> getBlockStatus(
            @Parameter(description = "对方用户ID", required = true)
            @PathVariable Long userId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 对方是否屏蔽了当前用户：existsBlock(blocker=other, blocked=current)
        boolean blockedByOther = privateMessageApplicationService.existsBlock(userId, currentUserId);
        return ResponseEntity.<Map<String, Boolean>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(java.util.Collections.singletonMap("blockedByOther", blockedByOther))
                .build();
    }
    
    @Operation(summary = "查询当前用户是否屏蔽了对方")
    @GetMapping("/users/{userId}/is-blocked")
    @SaCheckLogin
    @ApiOperationLog(description = "查询当前用户是否屏蔽了对方")
    public ResponseEntity<Boolean> checkIfBlocked(
            @Parameter(description = "对方用户ID", required = true)
            @PathVariable Long userId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 当前用户是否屏蔽了对方：existsBlock(blocker=current, blocked=other)
        boolean hasBlocked = privateMessageApplicationService.existsBlock(currentUserId, userId);
        return ResponseEntity.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(hasBlocked)
                .build();
    }
    
    @Operation(summary = "获取屏蔽列表")
    @GetMapping("/users/blocks")
    @SaCheckLogin
    @ApiOperationLog(description = "获取屏蔽列表")
    public ResponseEntity<List<UserBlockVO>> getBlockList(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取屏蔽列表，页码：{}，每页数量：{}", pageNo, pageSize);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        List<UserBlockEntity> blockList = privateMessageApplicationService
                .getBlockList(currentUserId, pageNo, pageSize);
        
        // 转换为VO并补充用户信息
        List<UserBlockVO> voList = blockList.stream()
                .map(this::convertToUserBlockVO)
                .collect(Collectors.toList());
        
        return ResponseEntity.<List<UserBlockVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(voList)
                .build();
    }
    
    /**
     * 转换为私信VO
     */
    private PrivateMessageVO convertToVO(PrivateMessageAggregate aggregate, Long currentUserId) {
        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setMessageId(aggregate.getId());
        vo.setSenderId(aggregate.getPrivateMessage().getSenderId());
        vo.setReceiverId(aggregate.getPrivateMessage().getReceiverId());
        vo.setContent(aggregate.getPrivateMessage().getContent());
        vo.setStatus(aggregate.getPrivateMessage().getStatus().getCode());
        vo.setIsRead(aggregate.getPrivateMessage().getIsRead());
        vo.setCreateTime(aggregate.getPrivateMessage().getCreateTime());
        
        // 补充用户信息
        try {
            Long senderId = aggregate.getPrivateMessage().getSenderId();
            Long receiverId = aggregate.getPrivateMessage().getReceiverId();
            Set<Long> userIds = new HashSet<>();
            userIds.add(senderId);
            userIds.add(receiverId);
            Map<Long, UserEntity> userMap = userService.getUserMapByIds(userIds);
            
            UserEntity sender = userMap.get(senderId);
            if (sender != null) {
                vo.setSenderName(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatar());
            }
            
            UserEntity receiver = userMap.get(receiverId);
            if (receiver != null) {
                vo.setReceiverName(receiver.getNickname());
                vo.setReceiverAvatar(receiver.getAvatar());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败", e);
        }
        
        return vo;
    }
    
    /**
     * 批量转换为对话VO（优化N+1问题）
     */
    private List<ConversationVO> convertToConversationVOList(List<ConversationEntity> conversations, Long currentUserId) {
        if (conversations.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 1. 批量获取所有对方用户ID
        Set<Long> otherUserIds = conversations.stream()
                .map(conv -> conv.getOtherParticipant(currentUserId))
                .collect(Collectors.toSet());
        
        // 2. 批量获取用户信息
        Map<Long, UserEntity> userMap;
        try {
            userMap = userService.getUserMapByIds(otherUserIds);
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
            userMap = Collections.emptyMap();
        }
        // 创建final副本供lambda使用
        final Map<Long, UserEntity> finalUserMap = userMap;
        
        // 3. 批量获取最后一条消息和未读数
        final Map<Long, PrivateMessageAggregate> lastMessageMap = new java.util.HashMap<>();
        final Map<Long, Long> unreadCountMap = new java.util.HashMap<>();
        
        for (Long otherUserId : otherUserIds) {
            try {
                // 获取最后一条消息
                List<PrivateMessageAggregate> lastMessages = privateMessageApplicationService
                        .getMessagesBetweenUsers(currentUserId, otherUserId, 1, 1);
                if (!lastMessages.isEmpty()) {
                    lastMessageMap.put(otherUserId, lastMessages.get(0));
                }
                
                // 获取未读消息数
                long unreadCount = privateMessageApplicationService.getUnreadCount(currentUserId, otherUserId);
                unreadCountMap.put(otherUserId, unreadCount);
            } catch (Exception e) {
                log.warn("获取最后一条消息或未读消息数失败，对方用户ID：{}", otherUserId, e);
            }
        }
        
        // 4. 转换为VO
        return conversations.stream()
                .map(conv -> {
                    ConversationVO vo = new ConversationVO();
                    Long otherUserId = conv.getOtherParticipant(currentUserId);
                    vo.setUserId(otherUserId);
                    vo.setLastMessageTime(conv.getLastMessageTime());
                    
                    // 设置用户信息
                    UserEntity user = finalUserMap.get(otherUserId);
                    if (user != null) {
                        vo.setUserName(user.getNickname());
                        vo.setUserAvatar(user.getAvatar());
                    }
                    
                    // 设置最后一条消息
                    PrivateMessageAggregate lastMessage = lastMessageMap.get(otherUserId);
                    if (lastMessage != null) {
                        String content = lastMessage.getPrivateMessage().getContent();
                        String preview = content;
                        try {
                            if (content != null && content.trim().startsWith("{")) {
                                com.fasterxml.jackson.databind.JsonNode node =
                                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(content);
                                if (node.has("type") && "image".equalsIgnoreCase(node.get("type").asText())) {
                                    preview = "[图片]";
                                }
                            }
                        } catch (Exception ignore) {
                            // 忽略解析异常，按文本展示
                        }
                        vo.setLastMessage(preview);
                        vo.setLastMessageStatus(lastMessage.getPrivateMessage().getStatus().getCode());
                    }
                    
                    // 设置未读消息数
                    Long unreadCount = unreadCountMap.get(otherUserId);
                    vo.setUnreadCount(unreadCount != null ? unreadCount : 0L);
                    
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为对话VO（单个，保留用于兼容）
     */
    private ConversationVO convertToConversationVO(ConversationEntity conversation, Long currentUserId) {
        List<ConversationVO> voList = convertToConversationVOList(Collections.singletonList(conversation), currentUserId);
        return voList.isEmpty() ? new ConversationVO() : voList.get(0);
    }
    
    @Operation(summary = "获取用户私信设置")
    @GetMapping("/settings")
    @SaCheckLogin
    @ApiOperationLog(description = "获取用户私信设置")
    public ResponseEntity<UserMessageSettingsVO> getUserMessageSettings() {
        log.info("获取用户私信设置");
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        UserMessageSettingsEntity settings = privateMessageApplicationService.getUserMessageSettings(currentUserId);
        UserMessageSettingsVO vo = convertToSettingsVO(settings);
        
        return ResponseEntity.<UserMessageSettingsVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(vo)
                .build();
    }
    
    @Operation(summary = "更新用户私信设置")
    @PutMapping("/settings")
    @SaCheckLogin
    @ApiOperationLog(description = "更新用户私信设置")
    public ResponseEntity<Void> updateUserMessageSettings(@Valid @RequestBody UpdateUserMessageSettingsDTO dto) {
        log.info("更新用户私信设置，请求参数：{}", dto);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        privateMessageApplicationService.updateUserMessageSettings(
                currentUserId,
                dto.getAllowStrangerMessage(),
                dto.getAllowNonMutualFollowMessage(),
                dto.getMessageNotificationEnabled()
        );
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新成功")
                .build();
    }
    
    @Operation(summary = "获取私信相关系统配置")
    @GetMapping("/system-configs")
    @SaCheckLogin
    @ApiOperationLog(description = "获取私信相关系统配置")
    public ResponseEntity<List<SystemConfigVO>> getPrivateMessageSystemConfigs() {
        log.info("获取私信相关系统配置");
        
        List<SystemConfigEntity> configs = privateMessageApplicationService.getPrivateMessageSystemConfigs();
        List<SystemConfigVO> voList = configs.stream()
                .map(this::convertToSystemConfigVO)
                .collect(Collectors.toList());
        
        return ResponseEntity.<List<SystemConfigVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(voList)
                .build();
    }
    
    @Operation(summary = "更新系统配置（管理员功能）")
    @PutMapping("/system-configs")
    @SaCheckLogin
    @SaCheckRole("admin")
    @ApiOperationLog(description = "更新系统配置")
    public ResponseEntity<Void> updateSystemConfig(@Valid @RequestBody UpdateSystemConfigDTO dto) {
        log.info("更新系统配置，请求参数：{}", dto);
        
        privateMessageApplicationService.updateSystemConfig(dto.getConfigKey(), dto.getConfigValue());
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新成功")
                .build();
    }
    
    /**
     * 转换为用户私信设置VO
     */
    private UserMessageSettingsVO convertToSettingsVO(UserMessageSettingsEntity settings) {
        UserMessageSettingsVO vo = new UserMessageSettingsVO();
        vo.setUserId(settings.getUserId());
        vo.setAllowStrangerMessage(settings.getAllowStrangerMessage());
        vo.setAllowNonMutualFollowMessage(settings.getAllowNonMutualFollowMessage());
        vo.setMessageNotificationEnabled(settings.getMessageNotificationEnabled());
        return vo;
    }
    
    /**
     * 转换为系统配置VO
     */
    private SystemConfigVO convertToSystemConfigVO(SystemConfigEntity config) {
        SystemConfigVO vo = new SystemConfigVO();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigDesc(config.getConfigDesc());
        return vo;
    }
    
    /**
     * 转换为用户屏蔽VO
     */
    private UserBlockVO convertToUserBlockVO(UserBlockEntity block) {
        UserBlockVO vo = new UserBlockVO();
        vo.setId(block.getId());
        vo.setBlockedUserId(block.getBlockedUserId());
        vo.setCreateTime(block.getCreateTime());
        
        // 补充被屏蔽用户信息
        try {
            UserEntity blockedUser = userService.getUserById(block.getBlockedUserId());
            if (blockedUser != null) {
                vo.setBlockedUserName(blockedUser.getNickname());
                vo.setBlockedUserAvatar(blockedUser.getAvatar());
            }
        } catch (Exception e) {
            log.warn("获取被屏蔽用户信息失败，用户ID：{}", block.getBlockedUserId(), e);
        }
        
        return vo;
    }
}

