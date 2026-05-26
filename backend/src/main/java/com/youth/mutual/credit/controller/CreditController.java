package com.youth.mutual.credit.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.credit.entity.CreditLog;
import com.youth.mutual.credit.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 信用中心控制器
 */
@Tag(name = "信用评分")
@RestController
@RequestMapping("/v1/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;
    private final UserContext userContext;

    @Operation(summary = "获取信用分信息")
    @GetMapping("/info")
    public R<Map<String, Object>> getCreditInfo() {
        Long userId = userContext.getRequiredUserId();
        return R.ok(creditService.getCreditInfo(userId));
    }

    @Operation(summary = "获取信用变动记录")
    @GetMapping("/logs")
    public R<PageResult<CreditLog>> getCreditLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = userContext.getRequiredUserId();
        return R.ok(creditService.getCreditLogs(userId, pageNum, pageSize));
    }
}
