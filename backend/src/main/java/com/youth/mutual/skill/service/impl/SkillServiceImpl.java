package com.youth.mutual.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.ResultCode;
import com.youth.mutual.skill.dto.SkillPostRequest;
import com.youth.mutual.skill.entity.SkillPost;
import com.youth.mutual.skill.entity.SkillTag;
import com.youth.mutual.skill.mapper.SkillPostMapper;
import com.youth.mutual.skill.mapper.SkillTagMapper;
import com.youth.mutual.skill.service.SkillService;
import com.youth.mutual.user.entity.User;
import com.youth.mutual.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能交换服务实现
 */
@Service
@RequiredArgsConstructor
public class SkillServiceImpl extends ServiceImpl<SkillPostMapper, SkillPost> implements SkillService {

    private final SkillPostMapper skillPostMapper;
    private final SkillTagMapper skillTagMapper;
    private final UserMapper userMapper;

    @Override
    public Long publishSkill(Long userId, SkillPostRequest request) {
        SkillPost post = new SkillPost();
        post.setUserId(userId);
        post.setType(request.getType());
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setCategory(request.getCategory());
        post.setAvailableTime(request.getAvailableTime());
        post.setPreferredLocation(request.getPreferredLocation());
        post.setOnlineSupport(request.getOnlineSupport() != null ? request.getOnlineSupport() : 0);
        post.setStatus(1);
        post.setViewCount(0);
        skillPostMapper.insert(post);
        return post.getId();
    }

    @Override
    public SkillPost getSkillDetail(Long id) {
        SkillPost post = skillPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.SKILL_POST_NOT_FOUND);
        }
        // 增加浏览次数
        post.setViewCount(post.getViewCount() + 1);
        skillPostMapper.updateById(post);
        return post;
    }

    @Override
    public PageResult<SkillPost> getSkillList(int pageNum, int pageSize, String type, String category, String city, String keyword) {
        Page<SkillPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SkillPost> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(SkillPost::getStatus, 1);

        if (StringUtils.hasText(type)) {
            wrapper.eq(SkillPost::getType, type);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(SkillPost::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SkillPost::getTitle, keyword).or().like(SkillPost::getDescription, keyword));
        }

        // 城市筛选：查找该城市的用户发布的技能
        if (StringUtils.hasText(city)) {
            List<Long> cityUserIds = getUserIdsByCity(city);
            if (cityUserIds.isEmpty()) {
                return new PageResult<>((long) pageNum, (long) pageSize, 0L, new ArrayList<>());
            }
            wrapper.in(SkillPost::getUserId, cityUserIds);
        }

        wrapper.orderByDesc(SkillPost::getCreatedAt);
        Page<SkillPost> result = skillPostMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<SkillPost> getRecommendations(Long userId, int limit) {
        // 获取用户"想学"的标签
        List<SkillTag> learnTags = skillTagMapper.selectList(
                new LambdaQueryWrapper<SkillTag>()
                        .eq(SkillTag::getUserId, userId)
                        .eq(SkillTag::getType, "learn")
        );

        if (learnTags.isEmpty()) {
            // 没有标签，返回最新发布的"能教"技能
            Page<SkillPost> page = new Page<>(1, limit);
            LambdaQueryWrapper<SkillPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SkillPost::getType, "teach")
                    .eq(SkillPost::getStatus, 1)
                    .orderByDesc(SkillPost::getCreatedAt);
            return skillPostMapper.selectPage(page, wrapper).getRecords();
        }

        // 根据标签匹配"能教"的技能
        List<String> tagNames = learnTags.stream().map(SkillTag::getTagName).toList();
        LambdaQueryWrapper<SkillPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillPost::getType, "teach")
                .eq(SkillPost::getStatus, 1)
                .ne(SkillPost::getUserId, userId)
                .in(SkillPost::getCategory, tagNames)
                .orderByDesc(SkillPost::getCreatedAt)
                .last("LIMIT " + limit);

        return skillPostMapper.selectList(wrapper);
    }

    @Override
    public void offlineSkill(Long userId, Long skillId) {
        SkillPost post = skillPostMapper.selectById(skillId);
        if (post == null) {
            throw new BusinessException(ResultCode.SKILL_POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }
        post.setStatus(0);
        skillPostMapper.updateById(post);
    }

    private List<Long> getUserIdsByCity(String city) {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getCity, city)
        );
        return users.stream().map(User::getId).toList();
    }
}
