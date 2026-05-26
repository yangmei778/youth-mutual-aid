package com.youth.mutual.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.ResultCode;
import com.youth.mutual.credit.service.CreditService;
import com.youth.mutual.mutual.entity.MutualRecord;
import com.youth.mutual.mutual.mapper.MutualRecordMapper;
import com.youth.mutual.review.dto.ReviewRequestDTO;
import com.youth.mutual.review.entity.Review;
import com.youth.mutual.review.mapper.ReviewMapper;
import com.youth.mutual.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 评价服务实现
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final MutualRecordMapper mutualRecordMapper;
    private final CreditService creditService;

    @Override
    public Long submitReview(Long reviewerId, ReviewRequestDTO dto) {
        // 验证互助记录存在且已完成
        MutualRecord record = mutualRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException(ResultCode.MUTUAL_RECORD_NOT_FOUND);
        }
        if (!"completed".equals(record.getStatus())) {
            throw new BusinessException("只能评价已完成的互助记录");
        }

        // 验证评价人是互助的参与方
        Long revieweeId;
        if (record.getInitiatorId().equals(reviewerId)) {
            revieweeId = record.getParticipantId();
        } else if (record.getParticipantId().equals(reviewerId)) {
            revieweeId = record.getInitiatorId();
        } else {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 检查是否已评价
        Long count = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getRecordId, dto.getRecordId())
                        .eq(Review::getReviewerId, reviewerId)
        );
        if (count > 0) {
            throw new BusinessException("你已经评价过该互助记录");
        }

        Review review = new Review();
        review.setRecordId(dto.getRecordId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        reviewMapper.insert(review);

        // 评价影响信用分：好评加1~3分，差评减1~3分
        int creditChange;
        String creditReason;
        if (dto.getRating() >= 4) {
            creditChange = dto.getRating() - 3; // 4星+1, 5星+2
            creditReason = "获得好评（" + dto.getRating() + "星）";
        } else if (dto.getRating() <= 2) {
            creditChange = dto.getRating() - 3; // 2星-1, 1星-2
            creditReason = "收到差评（" + dto.getRating() + "星）";
        } else {
            creditChange = 0;
            creditReason = null;
        }

        if (creditChange != 0) {
            creditService.addCredit(revieweeId, creditChange, creditReason, dto.getRecordId());
        }

        return review.getId();
    }

    @Override
    public PageResult<Review> getReceivedReviews(Long userId, int pageNum, int pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRevieweeId, userId)
                .orderByDesc(Review::getCreatedAt);
        Page<Review> result = reviewMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult<Review> getSentReviews(Long userId, int pageNum, int pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getReviewerId, userId)
                .orderByDesc(Review::getCreatedAt);
        Page<Review> result = reviewMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }
}
