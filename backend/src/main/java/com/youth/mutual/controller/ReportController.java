package com.youth.mutual.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.Report;
import com.youth.mutual.service.OperationLogService;
import com.youth.mutual.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "举报管理")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserContext userContext;
    private final OperationLogService logService;

    @Operation(summary = "提交举报")
    @PostMapping("/v1/reports")
    public R<Void> submitReport(@RequestBody Map<String, Object> body) {
        Long userId = userContext.getRequiredUserId();
        reportService.submitReport(
                userId,
                (String) body.get("targetType"),
                Long.valueOf(body.get("targetId").toString()),
                (String) body.get("reason"),
                (String) body.getOrDefault("description", "").toString()
        );
        return R.ok();
    }

    @Operation(summary = "管理员查看举报列表")
    @GetMapping("/v1/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<Report>> getReportList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(reportService.getReportList(status, pageNum, pageSize));
    }

    @Operation(summary = "管理员处理举报")
    @PutMapping("/v1/admin/reports/{id}/handle")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> handleReport(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long handlerId = userContext.getRequiredUserId();
        String status = (String) body.get("status");
        String note = (String) body.getOrDefault("handleNote", "").toString();
        reportService.handleReport(
                handlerId,
                id,
                status,
                note,
                body.get("deductCredit") != null ? Integer.valueOf(body.get("deductCredit").toString()) : null
        );
        logService.log(handlerId, "handle_report",
                ("approved".equals(status) ? "通过" : "驳回") + "了举报#" + id,
                note);
        return R.ok();
    }

    @Operation(summary = "管理员删除举报记录")
    @DeleteMapping("/v1/admin/reports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        logService.log(userContext.getRequiredUserId(), "delete_post",
                "删除了举报#" + id, "");
        return R.ok();
    }
}
