package com.youth.mutual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.ResultCode;
import com.youth.mutual.entity.*;
import com.youth.mutual.mapper.*;
import com.youth.mutual.service.CreditService;
import com.youth.mutual.service.MessageService;
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
    private final MessageService messageService;
    private final SkillPostMapper skillPostMapper;
    private final GoodsPostMapper goodsPostMapper;
    private final ActivityPostMapper activityPostMapper;

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
        if (count > 0) throw new BusinessException(400, "重复举报：您已举报过此内容，请等待管理员处理");

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus("pending");
        reportMapper.insert(report);

        // 通知所有管理员有新举报（被举报者不通知，等管理员处理后再通知）
        User reporter = userMapper.selectById(reporterId);
        String reporterName = reporter != null ? reporter.getNickname() : "用户";
        java.util.List<User> admins = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
        for (User admin : admins) {
            messageService.createNotification(admin.getId(),
                    "新举报待处理",
                    reporterName + " 举报了" + getTargetTypeName(targetType) + "（原因：" + reason + "）",
                    "report_new", report.getId());
        }
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

        // 通知举报人处理结果（简洁）
        String resultText = "approved".equals(status) ? "举报通过，违规内容已处理" : "举报被驳回";
        messageService.createNotification(report.getReporterId(),
                "举报处理结果",
                resultText,
                "report_result", report.getId());

        // 如果确认违规：下架 + 扣分 + 通知被举报者
        if ("approved".equals(status)) {
            offlineReportedContent(report);
            Long targetUserId = getTargetUserId(report);
            if (deductCredit != null && deductCredit > 0 && targetUserId != null) {
                creditService.addCredit(targetUserId, -deductCredit,
                        "被举报违规（" + report.getReason() + "）", report.getId());
            }
            // 通知被举报者：你的帖子因XX原因被下架
            if (targetUserId != null) {
                String itemType = getTargetTypeName(report.getTargetType());
                String reasonText = report.getReason();
                String noteText = handleNote != null && !handleNote.isBlank() ? "。处理备注：" + handleNote : "";
                messageService.createNotification(targetUserId,
                        "你的" + itemType + "已被下架",
                        "因被举报「" + reasonText + "」，你的" + itemType + "已被管理员下架" + noteText,
                        "content_offline|" + report.getTargetType(), report.getTargetId());
            }
        }
    }

    /** 自动下架被举报的内容 */
    private void offlineReportedContent(Report report) {
        try {
            switch (report.getTargetType()) {
                case "skill" -> {
                    SkillPost sp = skillPostMapper.selectById(report.getTargetId());
                    if (sp != null && sp.getStatus() == 1) { sp.setStatus(0); skillPostMapper.updateById(sp); }
                }
                case "goods" -> {
                    GoodsPost gp = goodsPostMapper.selectById(report.getTargetId());
                    if (gp != null && gp.getStatus() == 1) { gp.setStatus(0); goodsPostMapper.updateById(gp); }
                }
                case "activity" -> {
                    ActivityPost ap = activityPostMapper.selectById(report.getTargetId());
                    if (ap != null && ap.getStatus() == 1) { ap.setStatus(0); activityPostMapper.updateById(ap); }
                }
            }
        } catch (Exception ignored) {}
    }

    private String getTargetTypeName(String type) {
        return switch (type) {
            case "skill" -> "技能";
            case "goods" -> "物品";
            case "activity" -> "活动";
            case "user" -> "用户";
            default -> "内容";
        };
    }

    /** 根据举报类型找到被举报内容对应的用户ID */
    private Long getTargetUserId(Report report) {
        if ("user".equals(report.getTargetType())) {
            return report.getTargetId();
        }
        try {
            return switch (report.getTargetType()) {
                case "skill" -> {
                    SkillPost p = skillPostMapper.selectById(report.getTargetId());
                    yield p != null ? p.getUserId() : null;
                }
                case "goods" -> {
                    GoodsPost p = goodsPostMapper.selectById(report.getTargetId());
                    yield p != null ? p.getUserId() : null;
                }
                case "activity" -> {
                    ActivityPost p = activityPostMapper.selectById(report.getTargetId());
                    yield p != null ? p.getUserId() : null;
                }
                default -> null;
            };
        } catch (Exception e) { return null; }
    }
}
