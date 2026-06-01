package com.youth.mutual.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.entity.CreditLog;

import java.util.List;
import java.util.Map;

/**
 * 信用评分服务接口
 */
public interface CreditService {

    /** 增加信用分 */
    void addCredit(Long userId, int changeValue, String reason, Long relatedId);

    /** 获取用户信用信息 */
    Map<String, Object> getCreditInfo(Long userId);

    /** 获取信用变动记录 */
    PageResult<CreditLog> getCreditLogs(Long userId, int pageNum, int pageSize);

    /** 获取信用分排行榜 */
    List<Map<String, Object>> getLeaderboard(int limit);
}
