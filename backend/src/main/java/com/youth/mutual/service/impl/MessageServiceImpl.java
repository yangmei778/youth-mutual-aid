package com.youth.mutual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.entity.Message;
import com.youth.mutual.entity.Notification;
import com.youth.mutual.entity.User;
import com.youth.mutual.mapper.MessageMapper;
import com.youth.mutual.mapper.NotificationMapper;
import com.youth.mutual.mapper.UserMapper;
import com.youth.mutual.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @Override
    public void sendMessage(Long senderId, Long receiverId, String content, String type) {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setType(type != null ? type : "text");
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }

    @Override
    public PageResult<Message> getMessages(Long userId, Long targetUserId, int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (targetUserId != null) {
            // 与特定用户的对话
            wrapper.and(w -> w
                    .and(w1 -> w1.eq(Message::getSenderId, userId).eq(Message::getReceiverId, targetUserId))
                    .or(w2 -> w2.eq(Message::getSenderId, targetUserId).eq(Message::getReceiverId, userId))
            );
        } else {
            // 所有与我相关的消息
            wrapper.and(w -> w
                    .eq(Message::getSenderId, userId)
                    .or()
                    .eq(Message::getReceiverId, userId)
            );
        }
        wrapper.orderByDesc(Message::getCreatedAt);
        Page<Message> result = messageMapper.selectPage(page, wrapper);
        // 填充发送者昵称和头像
        if (!result.getRecords().isEmpty()) {
            Set<Long> senderIds = result.getRecords().stream()
                    .map(Message::getSenderId).collect(Collectors.toSet());
            Map<Long, User> userMap = userMapper.selectBatchIds(senderIds)
                    .stream().collect(Collectors.toMap(User::getId, u -> u));
            for (Message msg : result.getRecords()) {
                User sender = userMap.get(msg.getSenderId());
                if (sender != null) {
                    msg.setSenderName(sender.getNickname());
                    msg.setSenderAvatar(sender.getAvatar());
                }
            }
        }
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult<Notification> getNotifications(Long userId, int pageNum, int pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);
        Page<Notification> result = notificationMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public void markNotificationRead(Long userId, Long notificationId) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getId, notificationId)
                        .eq(Notification::getUserId, userId)
                        .set(Notification::getIsRead, 1));
    }

    @Override
    public void markAllNotificationsRead(Long userId) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
                        .set(Notification::getIsRead, 1));
    }

    @Override
    public Long getUnreadCount(Long userId) {
        long notifCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
        long msgCount = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, userId)
                        .eq(Message::getIsRead, 0));
        return notifCount + msgCount;
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getConversations(Long userId) {
        // 获取所有与我相关的消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId))
                .orderByDesc(Message::getCreatedAt);
        java.util.List<Message> allMsgs = messageMapper.selectList(wrapper);

        // 按对话对象分组，取每个对话的最后一条消息
        Map<Long, Message> convMap = new LinkedHashMap<>();
        for (Message msg : allMsgs) {
            Long otherId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            if (!convMap.containsKey(otherId)) {
                convMap.put(otherId, msg);  // 第一条就是最新的（已按时间倒序）
            }
        }

        // 填充用户信息并统计未读数
        Set<Long> userIds = convMap.keySet();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<Long, Message> entry : convMap.entrySet()) {
            Long otherId = entry.getKey();
            Message lastMsg = entry.getValue();
            User otherUser = userMap.get(otherId);
            if (otherUser == null) continue;

            // 统计该对话的未读数
            long unread = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getSenderId, otherId)
                            .eq(Message::getReceiverId, userId)
                            .eq(Message::getIsRead, 0));

            java.util.Map<String, Object> conv = new java.util.HashMap<>();
            conv.put("userId", otherId);
            conv.put("nickname", otherUser.getNickname());
            conv.put("avatar", otherUser.getAvatar());
            conv.put("lastContent", lastMsg.getContent());
            conv.put("lastTime", lastMsg.getCreatedAt() != null ? lastMsg.getCreatedAt().toString() : "");
            conv.put("unreadCount", unread);
            result.add(conv);
        }
        return result;
    }

    @Override
    public void markMessageRead(Long userId, Long messageId) {
        messageMapper.update(null,
                new LambdaUpdateWrapper<Message>()
                        .eq(Message::getId, messageId)
                        .eq(Message::getReceiverId, userId)
                        .set(Message::getIsRead, 1));
    }

    @Override
    public void markAllMessagesRead(Long userId, Long targetUserId) {
        messageMapper.update(null,
                new LambdaUpdateWrapper<Message>()
                        .eq(Message::getSenderId, targetUserId)
                        .eq(Message::getReceiverId, userId)
                        .eq(Message::getIsRead, 0)
                        .set(Message::getIsRead, 1));
    }

    @Override
    public void createNotification(Long userId, String title, String content, String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }
}
