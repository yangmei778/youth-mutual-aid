package com.youth.mutual.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.entity.Report;

public interface ReportService {

    /** 提交举报 */
    void submitReport(Long reporterId, String targetType, Long targetId, String reason, String description);

    /** 管理员查看举报列表 */
    PageResult<Report> getReportList(String status, int pageNum, int pageSize);

    /** 管理员处理举报 */
    void handleReport(Long handlerId, Long reportId, String status, String handleNote, Integer deductCredit);

    /** 管理员删除举报记录 */
    void deleteReport(Long id);
}
