package com.youth.mutual.review.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.review.dto.ReviewRequestDTO;
import com.youth.mutual.review.entity.Review;
import com.youth.mutual.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器
 */
@Tag(name = "评价管理")
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserContext userContext;

    @Operation(summary = "提交评价")
    @PostMapping
    public R<Long> submitReview(@Valid @RequestBody ReviewRequestDTO dto) {
        Long userId = userContext.getRequiredUserId();
        Long id = reviewService.submitReview(userId, dto);
        return R.ok(id);
    }

    @Operation(summary = "查看收到的评价")
    @GetMapping("/received")
    public R<PageResult<Review>> getReceivedReviews(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = userContext.getRequiredUserId();
        PageResult<Review> result = reviewService.getReceivedReviews(userId, pageNum, pageSize);
        return R.ok(result);
    }

    @Operation(summary = "查看发出的评价")
    @GetMapping("/sent")
    public R<PageResult<Review>> getSentReviews(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = userContext.getRequiredUserId();
        PageResult<Review> result = reviewService.getSentReviews(userId, pageNum, pageSize);
        return R.ok(result);
    }
}
