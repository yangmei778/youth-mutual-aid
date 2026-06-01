package com.youth.mutual.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.ActivityReview;
import com.youth.mutual.service.ActivityReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "活动回顾")
@RestController
@RequestMapping("/v1/activity")
@RequiredArgsConstructor
public class ActivityReviewController {

    private final ActivityReviewService reviewService;
    private final UserContext userContext;

    @Operation(summary = "发布活动回顾")
    @PostMapping("/posts/{id}/reviews")
    public R<Void> publishReview(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = userContext.getRequiredUserId();
        reviewService.publishReview(userId, id,
                body.getOrDefault("content", ""),
                body.getOrDefault("images", ""));
        return R.ok();
    }

    @Operation(summary = "获取活动回顾列表")
    @GetMapping("/posts/{id}/reviews")
    public R<PageResult<ActivityReview>> getReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(reviewService.getReviews(id, pageNum, pageSize));
    }

    @Operation(summary = "删除活动回顾")
    @DeleteMapping("/posts/{postId}/reviews/{reviewId}")
    public R<Void> deleteReview(@PathVariable Long postId, @PathVariable Long reviewId) {
        Long userId = userContext.getRequiredUserId();
        reviewService.deleteReview(userId, reviewId);
        return R.ok();
    }
}
