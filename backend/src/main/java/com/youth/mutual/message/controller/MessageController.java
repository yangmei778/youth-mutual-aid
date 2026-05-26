package com.youth.mutual.message.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.message.entity.Message;
import com.youth.mutual.message.entity.Notification;
import com.youth.mutual.message.service.MessageService;
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
            @RequestParam Long targetUserId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = userContext.getRequiredUserId();
        return R.ok(messageService.getMessages(userId, targetUserId, pageNum, pageSize));
    }

    @Operation(summary = "发送私信")
    @PostMapping("/messages")
    public R<Void> sendMessage(@RequestParam Long receiverId, @RequestParam String content) {
        Long userId = userContext.getRequiredUserId();
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

    @Operation(summary = "获取未读通知数")
    @GetMapping("/notifications/unread-count")
    public R<Long> getUnreadCount() {
        Long userId = userContext.getRequiredUserId();
        return R.ok(messageService.getUnreadCount(userId));
    }
}
