package com.youth.mutual.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youth.mutual.activity.entity.ActivityPost;
import com.youth.mutual.activity.mapper.ActivityPostMapper;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.goods.entity.GoodsPost;
import com.youth.mutual.goods.mapper.GoodsPostMapper;
import com.youth.mutual.skill.entity.SkillPost;
import com.youth.mutual.skill.mapper.SkillPostMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "获取待审核技能列表")
    @GetMapping("/skills")
    public R<PageResult<SkillPost>> getSkillPosts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<SkillPost> page = new Page<>(pageNum, pageSize);
        Page<SkillPost> result = skillPostMapper.selectPage(page,
                new LambdaQueryWrapper<SkillPost>().orderByDesc(SkillPost::getCreatedAt));
        return R.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "下架技能")
    @PutMapping("/skills/{id}/offline")
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
    public R<PageResult<GoodsPost>> getGoodsPosts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<GoodsPost> page = new Page<>(pageNum, pageSize);
        Page<GoodsPost> result = goodsPostMapper.selectPage(page,
                new LambdaQueryWrapper<GoodsPost>().orderByDesc(GoodsPost::getCreatedAt));
        return R.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "下架物品")
    @PutMapping("/goods/{id}/offline")
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
    public R<PageResult<ActivityPost>> getActivityPosts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<ActivityPost> page = new Page<>(pageNum, pageSize);
        Page<ActivityPost> result = activityPostMapper.selectPage(page,
                new LambdaQueryWrapper<ActivityPost>().orderByDesc(ActivityPost::getCreatedAt));
        return R.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "取消活动")
    @PutMapping("/activities/{id}/offline")
    public R<Void> offlineActivity(@PathVariable Long id) {
        ActivityPost post = activityPostMapper.selectById(id);
        if (post != null) {
            post.setStatus(0);
            activityPostMapper.updateById(post);
        }
        return R.ok();
    }
}
