package com.youth.mutual.controller;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.OperationLog;
import com.youth.mutual.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理后台-操作日志")
@RestController
@RequestMapping("/v1/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final OperationLogService logService;

    @Operation(summary = "获取最近操作日志（首页小窗用）")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<List<OperationLog>> getRecentLogs() {
        return R.ok(logService.getRecentLogs());
    }

    @Operation(summary = "分页获取操作日志")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<OperationLog>> getLogPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<OperationLog> records = logService.getRecentLogs(pageNum, pageSize);
        long total = logService.countLogs();
        return R.ok(new PageResult<>((long) pageNum, (long) pageSize, total, records));
    }
}
