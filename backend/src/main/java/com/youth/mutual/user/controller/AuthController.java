package com.youth.mutual.user.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.R;
import com.youth.mutual.common.utils.JwtUtils;
import com.youth.mutual.user.dto.*;
import com.youth.mutual.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserContext userContext;
    private final JwtUtils jwtUtils;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return R.ok();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return R.ok(response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public R<UserInfoResponse> getCurrentUserInfo() {
        Long userId = userContext.getRequiredUserId();
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return R.ok(userInfo);
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh-token")
    public R<Map<String, String>> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
            return R.fail(1003, "Refresh Token无效或已过期");
        }
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        // 重新生成 accessToken
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        String newAccessToken = jwtUtils.generateAccessToken(userId, userInfo.getUsername(), userInfo.getRole());
        return R.ok(Map.of("accessToken", newAccessToken));
    }
}
