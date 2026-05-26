package com.youth.mutual.credit.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.credit.entity.CreditLog;

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
}
