package com.youth.mutual.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.Report;
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
        reportService.handleReport(
                handlerId,
                id,
                (String) body.get("status"),
                (String) body.getOrDefault("handleNote", "").toString(),
                body.get("deductCredit") != null ? Integer.valueOf(body.get("deductCredit").toString()) : null
        );
        return R.ok();
    }

    @Operation(summary = "管理员删除举报记录")
    @DeleteMapping("/v1/admin/reports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return R.ok();
    }
}
