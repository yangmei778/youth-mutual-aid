package com.youth.mutual.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.credit.service.CreditService;
import com.youth.mutual.goods.entity.GoodsPost;
import com.youth.mutual.goods.mapper.GoodsPostMapper;
import com.youth.mutual.skill.entity.SkillPost;
import com.youth.mutual.skill.mapper.SkillPostMapper;
import com.youth.mutual.activity.entity.ActivityPost;
import com.youth.mutual.activity.mapper.ActivityPostMapper;
import com.youth.mutual.user.entity.User;
import com.youth.mutual.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台 - 统计看板
 */
@Tag(name = "管理后台-统计")
@RestController
@RequestMapping("/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserMapper userMapper;
    private final SkillPostMapper skillPostMapper;
    private final GoodsPostMapper goodsPostMapper;
    private final ActivityPostMapper activityPostMapper;

    @Operation(summary = "获取统计数据")
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userMapper.selectCount(null));
        stats.put("skillCount", skillPostMapper.selectCount(
                new LambdaQueryWrapper<SkillPost>().eq(SkillPost::getStatus, 1)));
        stats.put("goodsCount", goodsPostMapper.selectCount(
                new LambdaQueryWrapper<GoodsPost>().eq(GoodsPost::getStatus, 1)));
        stats.put("activityCount", activityPostMapper.selectCount(
                new LambdaQueryWrapper<ActivityPost>().eq(ActivityPost::getStatus, 1)));
        return R.ok(stats);
    }
}
