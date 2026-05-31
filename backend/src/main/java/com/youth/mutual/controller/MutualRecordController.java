package com.youth.mutual.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.dto.MutualRequestDTO;
import com.youth.mutual.entity.MutualRecord;
import com.youth.mutual.service.MutualRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 互助记录控制器
 */
@Tag(name = "互助记录管理")
@RestController
@RequestMapping("/v1/mutual")
@RequiredArgsConstructor
public class MutualRecordController {

    private final MutualRecordService mutualRecordService;
    private final UserContext userContext;

    @Operation(summary = "发起互助请求")
    @PostMapping("/requests")
    public R<Long> createRequest(@Valid @RequestBody MutualRequestDTO dto) {
        Long userId = userContext.getRequiredUserId();
        Long id = mutualRecordService.createRequest(userId, dto);
        return R.ok(id);
    }

    @Operation(summary = "接受互助请求")
    @PutMapping("/records/{id}/accept")
    public R<Void> acceptRequest(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        mutualRecordService.acceptRequest(userId, id);
        return R.ok();
    }

    @Operation(summary = "拒绝互助请求")
    @PutMapping("/records/{id}/reject")
    public R<Void> rejectRequest(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        mutualRecordService.rejectRequest(userId, id, null);
        return R.ok();
    }

    @Operation(summary = "确认互助完成")
    @PutMapping("/records/{id}/confirm")
    public R<Void> confirmComplete(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        mutualRecordService.confirmComplete(userId, id);
        return R.ok();
    }

    @Operation(summary = "取消互助")
    @PutMapping("/records/{id}/cancel")
    public R<Void> cancelRecord(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        mutualRecordService.cancelRecord(userId, id);
        return R.ok();
    }

    @Operation(summary = "获取我的互助记录")
    @GetMapping("/records")
    public R<PageResult<MutualRecord>> getMyRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = userContext.getRequiredUserId();
        PageResult<MutualRecord> result = mutualRecordService.getMyRecords(userId, status, pageNum, pageSize);
        return R.ok(result);
    }

    @Operation(summary = "获取互助详情")
    @GetMapping("/records/{id}")
    public R<MutualRecord> getRecordDetail(@PathVariable Long id) {
        MutualRecord record = mutualRecordService.getRecordDetail(id);
        return R.ok(record);
    }
}
