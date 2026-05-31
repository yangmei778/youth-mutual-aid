package com.youth.mutual.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.Review;
import com.youth.mutual.service.ReviewService;
import com.youth.mutual.entity.SkillPost;
import com.youth.mutual.mapper.SkillPostMapper;
import com.youth.mutual.entity.SkillTag;
import com.youth.mutual.mapper.SkillTagMapper;
import com.youth.mutual.dto.UpdateProfileRequest;
import com.youth.mutual.dto.UserInfoResponse;
import com.youth.mutual.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserContext userContext;
    private final SkillPostMapper skillPostMapper;
    private final SkillTagMapper skillTagMapper;
    private final ReviewService reviewService;

    @Operation(summary = "获取当前用户详细信息")
    @GetMapping("/me")
    public R<UserInfoResponse> getCurrentUser() {
        Long userId = userContext.getRequiredUserId();
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return R.ok(userInfo);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public R<UserInfoResponse> getUserInfo(@PathVariable Long id) {
        UserInfoResponse userInfo = userService.getUserInfo(id);
        return R.ok(userInfo);
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public R<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = userContext.getRequiredUserId();
        userService.updateProfile(userId, request);
        return R.ok();
    }

    @Operation(summary = "获取用户的技能标签")
    @GetMapping("/{id}/skill-tags")
    public R<List<SkillTag>> getUserSkillTags(@PathVariable Long id) {
        List<SkillTag> tags = skillTagMapper.selectList(
                new LambdaQueryWrapper<SkillTag>().eq(SkillTag::getUserId, id)
        );
        return R.ok(tags);
    }

    @Operation(summary = "更新我的技能标签")
    @PutMapping("/skill-tags")
    public R<Void> updateSkillTags(@RequestBody List<SkillTag> tags) {
        Long userId = userContext.getRequiredUserId();
        // 先删除旧的
        skillTagMapper.delete(new LambdaQueryWrapper<SkillTag>().eq(SkillTag::getUserId, userId));
        // 再插入新的
        for (SkillTag tag : tags) {
            tag.setId(null);
            tag.setUserId(userId);
            skillTagMapper.insert(tag);
        }
        return R.ok();
    }

    @Operation(summary = "获取用户的发布")
    @GetMapping("/{id}/posts")
    public R<PageResult<SkillPost>> getUserPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<SkillPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SkillPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillPost::getUserId, id)
                .eq(SkillPost::getStatus, 1)
                .orderByDesc(SkillPost::getCreatedAt);
        Page<SkillPost> result = skillPostMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "获取用户收到的评价")
    @GetMapping("/{id}/reviews")
    public R<PageResult<Review>> getUserReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Review> result = reviewService.getReceivedReviews(id, pageNum, pageSize);
        return R.ok(result);
    }
}
