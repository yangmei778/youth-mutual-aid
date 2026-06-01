package com.youth.mutual.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.dto.SkillPostRequest;
import com.youth.mutual.entity.SkillPost;
import com.youth.mutual.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能交换控制器
 */
@Tag(name = "技能交换")
@RestController
@RequestMapping("/v1/skill")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final UserContext userContext;

    @Operation(summary = "发布技能")
    @PostMapping("/posts")
    public R<Long> publishSkill(@Valid @RequestBody SkillPostRequest request) {
        Long userId = userContext.getRequiredUserId();
        Long id = skillService.publishSkill(userId, request);
        return R.ok(id);
    }

    @Operation(summary = "获取技能列表")
    @GetMapping("/posts")
    public R<PageResult<SkillPost>> getSkillList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword) {
        PageResult<SkillPost> result = skillService.getSkillList(pageNum, pageSize, type, category, city, keyword);
        return R.ok(result);
    }

    @Operation(summary = "获取技能详情")
    @GetMapping("/posts/{id}")
    public R<SkillPost> getSkillDetail(@PathVariable Long id) {
        SkillPost post = skillService.getSkillDetail(id);
        return R.ok(post);
    }

    @Operation(summary = "获取技能匹配推荐")
    @GetMapping("/recommendations")
    public R<List<SkillPost>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = userContext.getRequiredUserId();
        List<SkillPost> list = skillService.getRecommendations(userId, limit);
        return R.ok(list);
    }

    @Operation(summary = "下架技能")
    @PutMapping("/posts/{id}/offline")
    public R<Void> offlineSkill(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        skillService.offlineSkill(userId, id);
        return R.ok();
    }

    @Operation(summary = "删除技能（仅发布者）")
    @DeleteMapping("/posts/{id}")
    public R<Void> deleteSkill(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        skillService.deleteSkill(userId, id);
        return R.ok();
    }
}
