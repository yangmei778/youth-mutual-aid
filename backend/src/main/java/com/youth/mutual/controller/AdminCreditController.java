package com.youth.mutual.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.User;
import com.youth.mutual.mapper.UserMapper;
import com.youth.mutual.service.CreditService;
import com.youth.mutual.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final OperationLogService logService;
    private final UserContext userContext;
    private final UserMapper userMapper;

    @Operation(summary = "调整用户信用分")
    @PutMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> adjustCredit(
            @RequestParam Long userId,
            @RequestParam Integer changeValue,
            @RequestParam String reason) {
        creditService.addCredit(userId, changeValue, reason, null);
        User user = userMapper.selectById(userId);
        String sign = changeValue >= 0 ? "+" : "";
        logService.log(userContext.getRequiredUserId(), "adjust_credit",
                "调整了用户@" + (user != null ? user.getNickname() : userId) + "的信用分" + sign + changeValue,
                "原因:" + reason);
        return R.ok();
    }
}
