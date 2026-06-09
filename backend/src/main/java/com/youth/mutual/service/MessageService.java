package com.youth.mutual.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.entity.Message;
import com.youth.mutual.entity.Notification;

/**
 * 消息服务接口
 */
public interface MessageService {

    /** 发送私信 */
    void sendMessage(Long senderId, Long receiverId, String content, String type);

    /** 获取私信列表 */
    PageResult<Message> getMessages(Long userId, Long targetUserId, int pageNum, int pageSize);

    /** 获取系统通知 */
    PageResult<Notification> getNotifications(Long userId, int pageNum, int pageSize);

    /** 标记通知已读 */
    void markNotificationRead(Long userId, Long notificationId);

    /** 标记所有通知已读 */
    void markAllNotificationsRead(Long userId);

    /** 获取未读通知数 */
    Long getUnreadCount(Long userId);

    /** 创建系统通知 */
    void createNotification(Long userId, String title, String content, String type, Long relatedId);

    /** 获取会话列表（按对话对象分组） */
    java.util.List<java.util.Map<String, Object>> getConversations(Long userId);

    /** 标记单条私信已读 */
    void markMessageRead(Long userId, Long messageId);

    /** 标记某会话全部已读 */
    void markAllMessagesRead(Long userId, Long targetUserId);

    /** 删除通知 */
    void deleteNotification(Long userId, Long notificationId);

    /** 删除私信 */
    void deleteMessage(Long userId, Long messageId);
}
