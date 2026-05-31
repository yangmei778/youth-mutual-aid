package com.youth.mutual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youth.mutual.entity.ActivityMember;
import com.youth.mutual.entity.ActivityPost;
import com.youth.mutual.mapper.ActivityMemberMapper;
import com.youth.mutual.mapper.ActivityPostMapper;
import com.youth.mutual.service.ActivityService;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.ResultCode;
import com.youth.mutual.entity.User;
import com.youth.mutual.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl extends ServiceImpl<ActivityPostMapper, ActivityPost> implements ActivityService {

    private final ActivityPostMapper activityPostMapper;
    private final ActivityMemberMapper activityMemberMapper;
    private final UserMapper userMapper;

    @Override
    public Long publishActivity(Long userId, ActivityPost activityPost) {
        activityPost.setUserId(userId);
        activityPost.setStatus(1);
        activityPost.setCurrentMembers(1);
        activityPost.setViewCount(0);
        activityPostMapper.insert(activityPost);
        return activityPost.getId();
    }

    @Override
    public ActivityPost getActivityDetail(Long id) {
        ActivityPost post = activityPostMapper.selectById(id);
        if (post == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        post.setViewCount(post.getViewCount() + 1);
        activityPostMapper.updateById(post);
        // 填充发布者信息
        fillUserInfo(List.of(post));
        return post;
    }

    @Override
    public PageResult<ActivityPost> getActivityList(int pageNum, int pageSize, String type, String city, String keyword) {
        Page<ActivityPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityPost::getStatus, 1);

        if (StringUtils.hasText(type)) wrapper.eq(ActivityPost::getType, type);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ActivityPost::getTitle, keyword).or().like(ActivityPost::getDescription, keyword));
        }
        // 城市筛选：查找该城市的用户发布的活动
        if (StringUtils.hasText(city)) {
            List<Long> cityUserIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getCity, city)
            ).stream().map(User::getId).toList();
            if (cityUserIds.isEmpty()) {
                return new PageResult<>((long) pageNum, (long) pageSize, 0L, new ArrayList<>());
            }
            wrapper.in(ActivityPost::getUserId, cityUserIds);
        }
        wrapper.orderByDesc(ActivityPost::getCreatedAt);
        Page<ActivityPost> result = activityPostMapper.selectPage(page, wrapper);
        // 填充发布者信息
        fillUserInfo(result.getRecords());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public void joinActivity(Long userId, Long activityId, String applyMessage) {
        ActivityPost post = activityPostMapper.selectById(activityId);
        if (post == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        if (post.getCurrentMembers() >= post.getMaxMembers()) throw new BusinessException(ResultCode.ACTIVITY_FULL);

        // 检查是否已报名
        Long count = activityMemberMapper.selectCount(
                new LambdaQueryWrapper<ActivityMember>()
                        .eq(ActivityMember::getActivityId, activityId)
                        .eq(ActivityMember::getUserId, userId)
                        .ne(ActivityMember::getStatus, 3)
        );
        if (count > 0) throw new BusinessException(ResultCode.ACTIVITY_ALREADY_JOINED);

        ActivityMember member = new ActivityMember();
        member.setActivityId(activityId);
        member.setUserId(userId);
        member.setStatus(0);
        member.setApplyMessage(applyMessage);
        activityMemberMapper.insert(member);

        // 增加当前人数
        post.setCurrentMembers(post.getCurrentMembers() + 1);
        if (post.getCurrentMembers() >= post.getMaxMembers()) {
            post.setStatus(2); // 已满员
        }
        activityPostMapper.updateById(post);
    }

    @Override
    public void cancelActivity(Long userId, Long activityId) {
        ActivityPost post = activityPostMapper.selectById(activityId);
        if (post == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        if (!post.getUserId().equals(userId)) throw new BusinessException(ResultCode.ACCESS_DENIED);
        post.setStatus(0);
        activityPostMapper.updateById(post);
    }

    /**
     * 批量填充发布者信息（昵称、头像、信用分）
     */
    private void fillUserInfo(List<ActivityPost> posts) {
        if (posts == null || posts.isEmpty()) return;
        Set<Long> userIds = posts.stream().map(ActivityPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        for (ActivityPost post : posts) {
            User user = userMap.get(post.getUserId());
            if (user != null) {
                post.setNickname(user.getNickname());
                post.setAvatar(user.getAvatar());
                post.setCreditScore(user.getCreditScore());
            }
        }
    }
}
