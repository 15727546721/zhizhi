package cn.xu.api.web.controller;

import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.model.entity.MessageType;
import cn.xu.domain.message.service.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final IMessageService messageService;

    @GetMapping
    public List<MessageEntity> getUserMessages(
            @RequestParam(required = false) MessageType type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // TODO: 从SecurityContext获取当前用户ID
        Long currentUserId = 1L;
        return messageService.getUserMessages(currentUserId, type, page, size);
    }

    @GetMapping("/unread/count")
    public long getUnreadCount() {
        // TODO: 从SecurityContext获取当前用户ID
        Long currentUserId = 1L;
        return messageService.getUnreadCount(currentUserId);
    }

    @PutMapping("/{messageId}/read")
    public void markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
    }

    @PutMapping("/read/all")
    public void markAllAsRead() {
        // TODO: 从SecurityContext获取当前用户ID
        Long currentUserId = 1L;
        messageService.markAllAsRead(currentUserId);
    }

    @DeleteMapping("/{messageId}")
    public void deleteMessage(@PathVariable Long messageId) {
        // TODO: 从SecurityContext获取当前用户ID
        Long currentUserId = 1L;
        messageService.deleteMessage(messageId, currentUserId);
    }
} 