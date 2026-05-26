package com.youth.mutual.activity.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.activity.entity.ActivityPost;

/**
 * 临时搭伴服务接口
 */
public interface ActivityService {

    /** 发布活动 */
    Long publishActivity(Long userId, ActivityPost activityPost);

    /** 获取活动详情 */
    ActivityPost getActivityDetail(Long id);

    /** 分页查询活动列表 */
    PageResult<ActivityPost> getActivityList(int pageNum, int pageSize, String type, String city, String keyword);

    /** 报名活动 */
    void joinActivity(Long userId, Long activityId, String applyMessage);

    /** 取消活动 */
    void cancelActivity(Long userId, Long activityId);
}
