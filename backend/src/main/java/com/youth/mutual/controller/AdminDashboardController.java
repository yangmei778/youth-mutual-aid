package com.youth.mutual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.*;
import com.youth.mutual.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

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
    private final MutualRecordMapper mutualRecordMapper;
    private final ActivityMemberMapper activityMemberMapper;
    private final ReportMapper reportMapper;

    @Operation(summary = "获取统计数据")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 基础计数
        stats.put("userCount", userMapper.selectCount(null));
        stats.put("skillCount", skillPostMapper.selectCount(
                new LambdaQueryWrapper<SkillPost>().eq(SkillPost::getStatus, 1)));
        stats.put("goodsCount", goodsPostMapper.selectCount(
                new LambdaQueryWrapper<GoodsPost>().eq(GoodsPost::getStatus, 1)));
        stats.put("activityCount", activityPostMapper.selectCount(
                new LambdaQueryWrapper<ActivityPost>().eq(ActivityPost::getStatus, 1)));

        // 互助记录总数
        stats.put("mutualRecordCount", mutualRecordMapper.selectCount(null));

        // 待审核报名数
        stats.put("pendingAuditCount", activityMemberMapper.selectCount(
                new LambdaQueryWrapper<ActivityMember>().eq(ActivityMember::getStatus, 0)));
        stats.put("pendingReportCount", reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, "pending")));

        // 最近7天新增用户
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        stats.put("recentUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreatedAt, sevenDaysAgo)));

        // 最近30天活跃用户（有登录记录）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.put("activeUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getLastLoginTime, thirtyDaysAgo)));

        // 技能分类分布
        List<SkillPost> skills = skillPostMapper.selectList(
                new LambdaQueryWrapper<SkillPost>().eq(SkillPost::getStatus, 1));
        Map<String, Integer> categoryDist = new HashMap<>();
        for (SkillPost s : skills) {
            String cat = s.getCategory() != null ? s.getCategory() : "未分类";
            categoryDist.merge(cat, 1, Integer::sum);
        }
        // 转为 [{name, value}] 格式方便前端图表使用
        List<Map<String, Object>> catList = new ArrayList<>();
        categoryDist.forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", k);
            item.put("value", v);
            catList.add(item);
        });
        stats.put("categoryDistribution", catList);

        // 近7天每日新增内容趋势（技能+物品+活动）
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            long skillDay = skillPostMapper.selectCount(
                    new LambdaQueryWrapper<SkillPost>().between(SkillPost::getCreatedAt, dayStart, dayEnd));
            long goodsDay = goodsPostMapper.selectCount(
                    new LambdaQueryWrapper<GoodsPost>().between(GoodsPost::getCreatedAt, dayStart, dayEnd));
            long activityDay = activityPostMapper.selectCount(
                    new LambdaQueryWrapper<ActivityPost>().between(ActivityPost::getCreatedAt, dayStart, dayEnd));
            Map<String, Object> day = new HashMap<>();
            day.put("date", dayStart.toLocalDate().toString());
            day.put("skill", skillDay);
            day.put("goods", goodsDay);
            day.put("activity", activityDay);
            day.put("total", skillDay + goodsDay + activityDay);
            trend.add(day);
        }
        stats.put("dailyTrend", trend);

        return R.ok(stats);
    }
}
