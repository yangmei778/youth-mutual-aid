package com.youth.mutual.mutual.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.mutual.dto.MutualRequestDTO;
import com.youth.mutual.mutual.entity.MutualRecord;

/**
 * 互助记录服务接口
 */
public interface MutualRecordService {

    /**
     * 发起互助请求
     */
    Long createRequest(Long initiatorId, MutualRequestDTO dto);

    /**
     * 接受互助请求
     */
    void acceptRequest(Long userId, Long recordId);

    /**
     * 拒绝互助请求
     */
    void rejectRequest(Long userId, Long recordId, String reason);

    /**
     * 确认完成（当前用户确认）
     */
    void confirmComplete(Long userId, Long recordId);

    /**
     * 取消互助
     */
    void cancelRecord(Long userId, Long recordId);

    /**
     * 获取我的互助记录
     */
    PageResult<MutualRecord> getMyRecords(Long userId, String status, int pageNum, int pageSize);

    /**
     * 获取互助详情
     */
    MutualRecord getRecordDetail(Long id);
}
