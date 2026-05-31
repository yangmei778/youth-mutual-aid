package com.youth.mutual.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.dto.ReviewRequestDTO;
import com.youth.mutual.entity.Review;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 提交评价
     */
    Long submitReview(Long reviewerId, ReviewRequestDTO dto);

    /**
     * 查看收到的评价
     */
    PageResult<Review> getReceivedReviews(Long userId, int pageNum, int pageSize);

    /**
     * 查看发出的评价
     */
    PageResult<Review> getSentReviews(Long userId, int pageNum, int pageSize);
}
