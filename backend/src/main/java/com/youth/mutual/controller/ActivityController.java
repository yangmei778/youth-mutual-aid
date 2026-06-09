package com.youth.mutual.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.entity.ActivityPost;
import com.youth.mutual.entity.User;
import com.youth.mutual.mapper.UserMapper;
import com.youth.mutual.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 临时搭伴控制器
 */
@Tag(name = "临时搭伴")
@RestController
@RequestMapping("/v1/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final UserContext userContext;
    private final UserMapper userMapper;

    @Operation(summary = "发布活动")
    @PostMapping("/posts")
    public R<Long> publishActivity(@RequestBody ActivityPost activityPost) {
        Long userId = userContext.getRequiredUserId();
        User user = userMapper.selectById(userId);
        if (user != null && user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException("您的账号已被封禁，无法发布");
        }
        Long id = activityService.publishActivity(userId, activityPost);
        return R.ok(id);
    }

    @Operation(summary = "获取活动列表")
    @GetMapping("/posts")
    public R<PageResult<ActivityPost>> getActivityList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword) {
        return R.ok(activityService.getActivityList(pageNum, pageSize, type, city, keyword));
    }

    @Operation(summary = "获取活动详情")
    @GetMapping("/posts/{id}")
    public R<ActivityPost> getActivityDetail(@PathVariable Long id) {
        return R.ok(activityService.getActivityDetail(id));
    }

    @Operation(summary = "报名活动")
    @PostMapping("/posts/{id}/join")
    public R<Void> joinActivity(@PathVariable Long id, @RequestParam(required = false) String applyMessage) {
        Long userId = userContext.getRequiredUserId();
        activityService.joinActivity(userId, id, applyMessage);
        return R.ok();
    }

    @Operation(summary = "取消活动")
    @PutMapping("/posts/{id}/cancel")
    public R<Void> cancelActivity(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        activityService.cancelActivity(userId, id);
        return R.ok();
    }

    @Operation(summary = "删除活动（仅发布者）")
    @DeleteMapping("/posts/{id}")
    public R<Void> deleteActivity(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        activityService.deleteActivity(userId, id);
        return R.ok();
    }

    @Operation(summary = "通过报名申请")
    @PutMapping("/posts/{postId}/members/{memberId}/approve")
    public R<Void> approveMember(@PathVariable Long postId, @PathVariable Long memberId) {
        Long userId = userContext.getRequiredUserId();
        activityService.approveMember(userId, memberId);
        return R.ok();
    }

    @Operation(summary = "拒绝报名申请")
    @PutMapping("/posts/{postId}/members/{memberId}/reject")
    public R<Void> rejectMember(@PathVariable Long postId, @PathVariable Long memberId) {
        Long userId = userContext.getRequiredUserId();
        activityService.rejectMember(userId, memberId);
        return R.ok();
    }
}
