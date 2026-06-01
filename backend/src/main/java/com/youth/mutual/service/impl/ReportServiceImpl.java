package com.youth.mutual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.ResultCode;
import com.youth.mutual.entity.Report;
import com.youth.mutual.entity.User;
import com.youth.mutual.mapper.ReportMapper;
import com.youth.mutual.mapper.UserMapper;
import com.youth.mutual.service.CreditService;
import com.youth.mutual.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final CreditService creditService;

    @Override
    public void submitReport(Long reporterId, String targetType, Long targetId, String reason, String description) {
        // 检查重复举报
        Long count = reportMapper.selectCount(
                new LambdaQueryWrapper<Report>()
                        .eq(Report::getReporterId, reporterId)
                        .eq(Report::getTargetType, targetType)
                        .eq(Report::getTargetId, targetId)
                        .eq(Report::getStatus, "pending")
        );
        if (count > 0) throw new BusinessException(ResultCode.MUTUAL_REQUEST_DUPLICATE);

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus("pending");
        reportMapper.insert(report);
    }

    @Override
    public PageResult<Report> getReportList(String status, int pageNum, int pageSize) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) wrapper.eq(Report::getStatus, status);
        wrapper.orderByDesc(Report::getCreatedAt);
        Page<Report> result = reportMapper.selectPage(page, wrapper);

        // 填充用户名
        if (!result.getRecords().isEmpty()) {
            Set<Long> userIds = result.getRecords().stream()
                    .flatMap(r -> {
                        java.util.Set<Long> ids = new java.util.HashSet<>();
                        ids.add(r.getReporterId());
                        if (r.getHandlerId() != null) ids.add(r.getHandlerId());
                        return ids.stream();
                    })
                    .collect(Collectors.toSet());
            Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
                    .stream().collect(Collectors.toMap(User::getId, u -> u));
            for (Report r : result.getRecords()) {
                User reporter = userMap.get(r.getReporterId());
                if (reporter != null) r.setReporterName(reporter.getNickname());
                if (r.getHandlerId() != null) {
                    User handler = userMap.get(r.getHandlerId());
                    if (handler != null) r.setHandlerName(handler.getNickname());
                }
            }
        }
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public void handleReport(Long handlerId, Long reportId, String status, String handleNote, Integer deductCredit) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) throw new BusinessException(ResultCode.PARAM_ERROR);
        if (!"pending".equals(report.getStatus())) throw new BusinessException(ResultCode.MUTUAL_STATUS_ERROR);

        report.setStatus(status);
        report.setHandlerId(handlerId);
        report.setHandleNote(handleNote);
        reportMapper.updateById(report);

        // 如果确认违规且需要扣分，找到被举报对应的用户并扣分
        if ("approved".equals(status) && deductCredit != null && deductCredit > 0) {
            Long targetUserId = getTargetUserId(report);
            if (targetUserId != null) {
                creditService.addCredit(targetUserId, -deductCredit,
                        "被举报违规（" + report.getReason() + "）", report.getId());
            }
        }
    }

    /** 根据举报类型找到被举报内容对应的用户ID */
    private Long getTargetUserId(Report report) {
        if ("user".equals(report.getTargetType())) {
            return report.getTargetId();
        }
        // 其他类型（skill/goods/activity）可以通过对应的mapper查询userId
        // 简单处理：直接扣target的关联信用
        return null; // 简化处理
    }
}
