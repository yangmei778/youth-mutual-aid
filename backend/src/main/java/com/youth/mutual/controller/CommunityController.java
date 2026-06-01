package com.youth.mutual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.*;
import com.youth.mutual.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "社区动态")
@RestController
@RequestMapping("/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final MutualRecordMapper mutualRecordMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final SkillPostMapper skillPostMapper;
    private final GoodsPostMapper goodsPostMapper;
    private final ActivityPostMapper activityPostMapper;

    @Operation(summary = "获取社区动态流")
    @GetMapping("/feed")
    public R<List<Map<String, Object>>> getFeed(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        List<Map<String, Object>> feed = new ArrayList<>();

        // 1. 最近完成的互助记录
        var completedRecords = mutualRecordMapper.selectList(
                new LambdaQueryWrapper<MutualRecord>()
                        .eq(MutualRecord::getStatus, "completed")
                        .orderByDesc(MutualRecord::getUpdatedAt)
                        .last("LIMIT 10"));
        for (var r : completedRecords) {
            User initiator = userMapper.selectById(r.getInitiatorId());
            User participant = userMapper.selectById(r.getParticipantId());
            if (initiator == null || participant == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "mutual_completed");
            item.put("typeLabel", "互助完成");
            item.put("icon", "🤝");
            item.put("desc", initiator.getNickname() + " 和 " + participant.getNickname() + " 完成了" + typeLabel(r.getType()) + "互助");
            item.put("time", r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : "");
            item.put("user1", initiator.getNickname());
            item.put("user2", participant.getNickname());
            feed.add(item);
        }

        // 2. 最新评价
        var reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .orderByDesc(Review::getCreatedAt)
                        .last("LIMIT 5"));
        for (var rv : reviews) {
            User reviewer = userMapper.selectById(rv.getReviewerId());
            User reviewee = userMapper.selectById(rv.getRevieweeId());
            if (reviewer == null || reviewee == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "review");
            item.put("typeLabel", "新评价");
            item.put("icon", getRatingStars(rv.getRating()));
            item.put("desc", reviewer.getNickname() + " 评价了 " + reviewee.getNickname() + "：" + rv.getRating() + "星");
            item.put("content", rv.getContent());
            item.put("time", rv.getCreatedAt() != null ? rv.getCreatedAt().toString() : "");
            feed.add(item);
        }

        // 3. 最新发布的技能
        var skills = skillPostMapper.selectList(
                new LambdaQueryWrapper<SkillPost>()
                        .eq(SkillPost::getStatus, 1)
                        .orderByDesc(SkillPost::getCreatedAt)
                        .last("LIMIT 5"));
        for (var s : skills) {
            User u = userMapper.selectById(s.getUserId());
            if (u == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "new_skill");
            item.put("typeLabel", "新技能");
            item.put("icon", "📚");
            item.put("desc", u.getNickname() + " 发布了" + (s.getType().equals("teach") ? "能教" : "想学") + "：「" + s.getTitle() + "」");
            item.put("time", s.getCreatedAt() != null ? s.getCreatedAt().toString() : "");
            feed.add(item);
        }

        // 按时间倒序排列
        feed.sort((a, b) -> b.get("time").toString().compareTo(a.get("time").toString()));

        // 分页截取
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, feed.size());
        if (start >= feed.size()) feed = List.of();
        else feed = feed.subList(start, end);

        return R.ok(feed);
    }

    private String typeLabel(String t) {
        return switch (t) {
            case "skill" -> "技能交换";
            case "goods" -> "物品共享";
            case "activity" -> "临时搭伴";
            default -> t;
        };
    }

    private String getRatingStars(int rating) {
        return switch (rating) {
            case 5 -> "⭐⭐⭐⭐⭐";
            case 4 -> "⭐⭐⭐⭐";
            case 3 -> "⭐⭐⭐";
            default -> "⭐".repeat(Math.max(1, rating));
        };
    }
}
