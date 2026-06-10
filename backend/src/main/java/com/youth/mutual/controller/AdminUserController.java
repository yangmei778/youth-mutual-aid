package com.youth.mutual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.User;
import com.youth.mutual.mapper.UserMapper;
import com.youth.mutual.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台 - 用户管理
 */
@Tag(name = "管理后台-用户管理")
@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;
    private final OperationLogService logService;
    private final UserContext userContext;

    @Operation(summary = "获取用户列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<User>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getUsername, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "封禁/解封用户")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return R.fail("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        logService.log(userContext.getRequiredUserId(), status == 0 ? "ban_user" : "unban_user",
                (status == 0 ? "封禁" : "解封") + "了用户@" + user.getNickname(),
                "用户ID:" + id);
        return R.ok();
    }
}
