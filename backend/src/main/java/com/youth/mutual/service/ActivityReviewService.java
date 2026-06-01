package com.youth.mutual.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.entity.ActivityReview;

public interface ActivityReviewService {

    /** 发布活动回顾 */
    void publishReview(Long userId, Long activityId, String content, String images);

    /** 获取活动回顾列表 */
    PageResult<ActivityReview> getReviews(Long activityId, int pageNum, int pageSize);

    /** 删除回顾 */
    void deleteReview(Long userId, Long reviewId);
}
