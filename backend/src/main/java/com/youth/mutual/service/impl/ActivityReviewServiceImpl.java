package com.youth.mutual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.ResultCode;
import com.youth.mutual.entity.*;
import com.youth.mutual.mapper.*;
import com.youth.mutual.service.ActivityReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityReviewServiceImpl implements ActivityReviewService {

    private final ActivityReviewMapper reviewMapper;
    private final ActivityMemberMapper memberMapper;
    private final ActivityPostMapper activityPostMapper;
    private final UserMapper userMapper;

    @Override
    public void publishReview(Long userId, Long activityId, String content, String images) {
        // 检查活动存在
        ActivityPost post = activityPostMapper.selectById(activityId);
        if (post == null) throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);

        // 检查用户是否参与过该活动
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<ActivityMember>()
                        .eq(ActivityMember::getActivityId, activityId)
                        .eq(ActivityMember::getUserId, userId)
        );
        if (count == 0) throw new BusinessException(ResultCode.ACCESS_DENIED);

        ActivityReview review = new ActivityReview();
        review.setActivityId(activityId);
        review.setUserId(userId);
        review.setContent(content);
        review.setImages(images);
        reviewMapper.insert(review);
    }

    @Override
    public PageResult<ActivityReview> getReviews(Long activityId, int pageNum, int pageSize) {
        Page<ActivityReview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivityReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityReview::getActivityId, activityId)
                .orderByDesc(ActivityReview::getCreatedAt);
        Page<ActivityReview> result = reviewMapper.selectPage(page, wrapper);

        // 填充用户信息
        List<ActivityReview> records = result.getRecords();
        if (!records.isEmpty()) {
            Set<Long> userIds = records.stream().map(ActivityReview::getUserId).collect(Collectors.toSet());
            Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
                    .stream().collect(Collectors.toMap(User::getId, u -> u));
            for (ActivityReview r : records) {
                User u = userMap.get(r.getUserId());
                if (u != null) {
                    r.setNickname(u.getNickname());
                    r.setAvatar(u.getAvatar());
                }
            }
        }

        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public void deleteReview(Long userId, Long reviewId) {
        ActivityReview review = reviewMapper.selectById(reviewId);
        if (review == null) throw new BusinessException(ResultCode.PARAM_ERROR);
        if (!review.getUserId().equals(userId)) throw new BusinessException(ResultCode.ACCESS_DENIED);
        reviewMapper.deleteById(reviewId);
    }
}
