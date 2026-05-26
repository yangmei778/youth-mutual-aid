package com.youth.mutual.admin.controller;

import com.youth.mutual.common.result.R;
import com.youth.mutual.credit.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台 - 信用管理
 */
@Tag(name = "管理后台-信用管理")
@RestController
@RequestMapping("/v1/admin/credit")
@RequiredArgsConstructor
public class AdminCreditController {

    private final CreditService creditService;

    @Operation(summary = "调整用户信用分")
    @PutMapping("/adjust")
    public R<Void> adjustCredit(
            @RequestParam Long userId,
            @RequestParam Integer changeValue,
            @RequestParam String reason) {
        creditService.addCredit(userId, changeValue, reason, null);
        return R.ok();
    }
}
