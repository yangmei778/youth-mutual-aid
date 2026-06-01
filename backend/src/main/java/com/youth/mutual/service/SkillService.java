package com.youth.mutual.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.dto.SkillPostRequest;
import com.youth.mutual.entity.SkillPost;

import java.util.List;

/**
 * 技能交换服务接口
 */
public interface SkillService {

    /** 发布技能 */
    Long publishSkill(Long userId, SkillPostRequest request);

    /** 获取技能详情 */
    SkillPost getSkillDetail(Long id);

    /** 分页查询技能列表 */
    PageResult<SkillPost> getSkillList(int pageNum, int pageSize, String type, String category, String city, String keyword);

    /** 获取技能匹配推荐 */
    List<SkillPost> getRecommendations(Long userId, int limit);

    /** 下架技能 */
    void offlineSkill(Long userId, Long skillId);

    /** 删除技能（仅发布者） */
    void deleteSkill(Long userId, Long skillId);
}
