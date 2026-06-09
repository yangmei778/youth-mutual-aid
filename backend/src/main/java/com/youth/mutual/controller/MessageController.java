package com.youth.mutual.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.Message;
import com.youth.mutual.entity.Notification;
import com.youth.mutual.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知控制器
 */
@Tag(name = "消息通知")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserContext userContext;

    @Operation(summary = "获取私信列表")
    @GetMapping("/messages")
    public R<PageResult<Message>> getMessages(
            @RequestParam(required = false) Long targetUserId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = userContext.getRequiredUserId();
        return R.ok(messageService.getMessages(userId, targetUserId, pageNum, pageSize));
    }

    @Operation(summary = "发送私信")
    @PostMapping("/messages")
    public R<Void> sendMessage(@RequestBody java.util.Map<String, Object> body) {
        Long userId = userContext.getRequiredUserId();
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String content = body.get("content").toString();
        messageService.sendMessage(userId, receiverId, content, "text");
        return R.ok();
    }

    @Operation(summary = "获取系统通知")
    @GetMapping("/notifications")
    public R<PageResult<Notification>> getNotifications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = userContext.getRequiredUserId();
        return R.ok(messageService.getNotifications(userId, pageNum, pageSize));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/notifications/{id}/read")
    public R<Void> markAsRead(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        messageService.markNotificationRead(userId, id);
        return R.ok();
    }

    @Operation(summary = "标记所有通知已读")
    @PutMapping("/notifications/read-all")
    public R<Void> markAllRead() {
        Long userId = userContext.getRequiredUserId();
        messageService.markAllNotificationsRead(userId);
        return R.ok();
    }

    @Operation(summary = "获取未读通知数（含通知+私信）")
    @GetMapping("/notifications/unread-count")
    public R<Long> getUnreadCount() {
        Long userId = userContext.getRequiredUserId();
        return R.ok(messageService.getUnreadCount(userId));
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/messages/conversations")
    public R<java.util.List<java.util.Map<String, Object>>> getConversations() {
        Long userId = userContext.getRequiredUserId();
        return R.ok(messageService.getConversations(userId));
    }

    @Operation(summary = "标记私信已读")
    @PutMapping("/messages/{id}/read")
    public R<Void> markMessageRead(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        messageService.markMessageRead(userId, id);
        return R.ok();
    }

    @Operation(summary = "标记某会话全部已读")
    @PutMapping("/messages/read-all")
    public R<Void> markAllMessagesRead(@RequestParam Long targetUserId) {
        Long userId = userContext.getRequiredUserId();
        messageService.markAllMessagesRead(userId, targetUserId);
        return R.ok();
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/notifications/{id}")
    public R<Void> deleteNotification(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        messageService.deleteNotification(userId, id);
        return R.ok();
    }

    @Operation(summary = "删除私信")
    @DeleteMapping("/messages/{id}")
    public R<Void> deleteMessage(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        messageService.deleteMessage(userId, id);
        return R.ok();
    }
}
