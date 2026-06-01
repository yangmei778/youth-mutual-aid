package com.youth.mutual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.*;
import com.youth.mutual.mapper.*;
import com.youth.mutual.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 信用中心控制器
 */
@Tag(name = "信用评分")
@RestController
@RequestMapping("/v1/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;
    private final UserContext userContext;
    private final MutualRecordMapper mutualRecordMapper;
    private final ReviewMapper reviewMapper;
    private final SkillPostMapper skillPostMapper;
    private final ActivityPostMapper activityPostMapper;

    @Operation(summary = "获取信用分信息")
    @GetMapping("/info")
    public R<Map<String, Object>> getCreditInfo() {
        Long userId = userContext.getRequiredUserId();
        return R.ok(creditService.getCreditInfo(userId));
    }

    @Operation(summary = "获取信用变动记录")
    @GetMapping("/logs")
    public R<PageResult<CreditLog>> getCreditLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = userContext.getRequiredUserId();
        return R.ok(creditService.getCreditLogs(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取信用分排行榜")
    @GetMapping("/leaderboard")
    public R<List<Map<String, Object>>> getLeaderboard(
            @RequestParam(defaultValue = "20") int limit) {
        return R.ok(creditService.getLeaderboard(limit));
    }

    @Operation(summary = "获取互助成就徽章")
    @GetMapping("/achievements")
    public R<List<Map<String, Object>>> getAchievements() {
        Long userId = userContext.getRequiredUserId();
        List<Map<String, Object>> achievements = new ArrayList<>();

        // 统计用户数据
        long mutualCount = mutualRecordMapper.selectCount(
                new LambdaQueryWrapper<MutualRecord>()
                        .eq(MutualRecord::getStatus, "completed")
                        .and(w -> w.eq(MutualRecord::getInitiatorId, userId).or().eq(MutualRecord::getParticipantId, userId)));
        long reviewCount = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getRevieweeId, userId));
        long skillCount = skillPostMapper.selectCount(
                new LambdaQueryWrapper<SkillPost>().eq(SkillPost::getUserId, userId).eq(SkillPost::getStatus, 1));
        long activityCount = activityPostMapper.selectCount(
                new LambdaQueryWrapper<ActivityPost>().eq(ActivityPost::getUserId, userId));

        // 成就定义
        addAchievement(achievements, "初次互助", "完成第1次互助", "🔰", mutualCount >= 1);
        addAchievement(achievements, "互助新星", "完成5次互助", "🌟", mutualCount >= 5);
        addAchievement(achievements, "互助达人", "完成10次互助", "⭐", mutualCount >= 10);
        addAchievement(achievements, "互助先锋", "完成20次互助", "🏅", mutualCount >= 20);
        addAchievement(achievements, "好评初体验", "收到第1条好评", "👍", reviewCount >= 1);
        addAchievement(achievements, "广受好评", "收到10条好评", "🎖️", reviewCount >= 10);
        addAchievement(achievements, "技能大师", "发布10个技能", "📚", skillCount >= 10);
        addAchievement(achievements, "活动达人", "发起5个活动", "🤝", activityCount >= 5);

        return R.ok(achievements);
    }

    private void addAchievement(List<Map<String, Object>> list, String name, String desc, String icon, boolean earned) {
        Map<String, Object> a = new HashMap<>();
        a.put("name", name);
        a.put("desc", desc);
        a.put("icon", icon);
        a.put("earned", earned);
        list.add(a);
    }
}
