package com.youth.mutual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youth.mutual.entity.ActivityPost;
import com.youth.mutual.mapper.ActivityPostMapper;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.GoodsPost;
import com.youth.mutual.mapper.GoodsPostMapper;
import com.youth.mutual.entity.SkillPost;
import com.youth.mutual.mapper.SkillPostMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台 - 内容审核
 */
@Tag(name = "管理后台-内容审核")
@RestController
@RequestMapping("/v1/admin/content")
@RequiredArgsConstructor
public class AdminContentController {

    private final SkillPostMapper skillPostMapper;
    private final GoodsPostMapper goodsPostMapper;
    private final ActivityPostMapper activityPostMapper;

    @Operation(summary = "获取技能列表")
    @GetMapping("/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<SkillPost>> getSkillPosts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SkillPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SkillPost> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(SkillPost::getTitle, keyword).or().like(SkillPost::getDescription, keyword));
        }
        w.orderByDesc(SkillPost::getCreatedAt);
        Page<SkillPost> result = skillPostMapper.selectPage(page, w);
        return R.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "下架技能")
    @PutMapping("/skills/{id}/offline")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> offlineSkill(@PathVariable Long id) {
        SkillPost post = skillPostMapper.selectById(id);
        if (post != null) {
            post.setStatus(0);
            skillPostMapper.updateById(post);
        }
        return R.ok();
    }

    @Operation(summary = "获取物品列表")
    @GetMapping("/goods")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<GoodsPost>> getGoodsPosts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<GoodsPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GoodsPost> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(GoodsPost::getTitle, keyword).or().like(GoodsPost::getDescription, keyword));
        }
        w.orderByDesc(GoodsPost::getCreatedAt);
        Page<GoodsPost> result = goodsPostMapper.selectPage(page, w);
        return R.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "下架物品")
    @PutMapping("/goods/{id}/offline")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> offlineGoods(@PathVariable Long id) {
        GoodsPost post = goodsPostMapper.selectById(id);
        if (post != null) {
            post.setStatus(0);
            goodsPostMapper.updateById(post);
        }
        return R.ok();
    }

    @Operation(summary = "获取活动列表")
    @GetMapping("/activities")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<ActivityPost>> getActivityPosts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<ActivityPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivityPost> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(ActivityPost::getTitle, keyword).or().like(ActivityPost::getDescription, keyword));
        }
        w.orderByDesc(ActivityPost::getCreatedAt);
        Page<ActivityPost> result = activityPostMapper.selectPage(page, w);
        return R.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "取消活动")
    @PutMapping("/activities/{id}/offline")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> offlineActivity(@PathVariable Long id) {
        ActivityPost post = activityPostMapper.selectById(id);
        if (post != null) { post.setStatus(0); activityPostMapper.updateById(post); }
        return R.ok();
    }

    // ── 管理员删除（不检查所有权）──

    @Operation(summary = "管理员删除技能")
    @DeleteMapping("/skills/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> deleteSkill(@PathVariable Long id) {
        skillPostMapper.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "管理员删除物品")
    @DeleteMapping("/goods/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> deleteGoods(@PathVariable Long id) {
        goodsPostMapper.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "管理员删除活动")
    @DeleteMapping("/activities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> deleteActivity(@PathVariable Long id) {
        activityPostMapper.deleteById(id);
        return R.ok();
    }
}
