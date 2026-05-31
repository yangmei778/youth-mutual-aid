package com.youth.mutual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.ResultCode;
import com.youth.mutual.service.CreditService;
import com.youth.mutual.service.MessageService;
import com.youth.mutual.dto.MutualRequestDTO;
import com.youth.mutual.entity.MutualRecord;
import com.youth.mutual.mapper.MutualRecordMapper;
import com.youth.mutual.service.MutualRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 互助记录服务实现
 */
@Service
@RequiredArgsConstructor
public class MutualRecordServiceImpl extends ServiceImpl<MutualRecordMapper, MutualRecord> implements MutualRecordService {

    private final MutualRecordMapper mutualRecordMapper;
    private final CreditService creditService;
    private final MessageService messageService;

    @Override
    public Long createRequest(Long initiatorId, MutualRequestDTO dto) {
        // 不能向自己发起请求
        if (initiatorId.equals(dto.getParticipantId())) {
            throw new BusinessException("不能向自己发起互助请求");
        }

        // 检查是否有重复的进行中请求
        Long count = mutualRecordMapper.selectCount(
                new LambdaQueryWrapper<MutualRecord>()
                        .eq(MutualRecord::getInitiatorId, initiatorId)
                        .eq(MutualRecord::getParticipantId, dto.getParticipantId())
                        .eq(MutualRecord::getRelatedId, dto.getRelatedId())
                        .in(MutualRecord::getStatus, "pending", "ongoing")
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.MUTUAL_REQUEST_DUPLICATE);
        }

        MutualRecord record = new MutualRecord();
        record.setInitiatorId(initiatorId);
        record.setParticipantId(dto.getParticipantId());
        record.setRelatedId(dto.getRelatedId());
        record.setType(dto.getType());
        record.setRequestMessage(dto.getRequestMessage());
        record.setStatus("pending");
        record.setInitiatorConfirmed(0);
        record.setParticipantConfirmed(0);
        mutualRecordMapper.insert(record);

        // 通知参与人有新的互助请求
        messageService.createNotification(dto.getParticipantId(),
                "收到互助请求",
                "您收到一条" + getTypeName(dto.getType()) + "互助请求",
                "mutual_request", record.getId());

        return record.getId();
    }

    @Override
    public void acceptRequest(Long userId, Long recordId) {
        MutualRecord record = getAndValidate(recordId);

        // 只有参与人才能接受
        if (!record.getParticipantId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException(ResultCode.MUTUAL_STATUS_ERROR);
        }

        record.setStatus("ongoing");
        record.setStartTime(LocalDateTime.now());
        record.setAcceptMessage("已接受");
        mutualRecordMapper.updateById(record);

        // 通知发起人请求已被接受
        messageService.createNotification(record.getInitiatorId(),
                "互助请求已接受",
                "您的" + getTypeName(record.getType()) + "互助请求已被接受",
                "mutual_confirm", record.getId());
    }

    @Override
    public void rejectRequest(Long userId, Long recordId, String reason) {
        MutualRecord record = getAndValidate(recordId);

        // 只有参与人才能拒绝
        if (!record.getParticipantId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException(ResultCode.MUTUAL_STATUS_ERROR);
        }

        record.setStatus("cancelled");
        mutualRecordMapper.updateById(record);
    }

    @Override
    @Transactional
    public void confirmComplete(Long userId, Long recordId) {
        MutualRecord record = getAndValidate(recordId);

        if (!"ongoing".equals(record.getStatus())) {
            throw new BusinessException(ResultCode.MUTUAL_STATUS_ERROR);
        }

        // 判断当前用户是发起人还是参与人
        if (record.getInitiatorId().equals(userId)) {
            record.setInitiatorConfirmed(1);
        } else if (record.getParticipantId().equals(userId)) {
            record.setParticipantConfirmed(1);
        } else {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 双方都确认完成，则标记为已完成
        if (record.getInitiatorConfirmed() == 1 && record.getParticipantConfirmed() == 1) {
            record.setStatus("completed");
            record.setEndTime(LocalDateTime.now());

            // 互助完成，双方加信用分
            int creditValue = getCreditValueByType(record.getType());
            creditService.addCredit(record.getInitiatorId(), creditValue,
                    "完成" + getTypeName(record.getType()) + "互助", record.getId());
            creditService.addCredit(record.getParticipantId(), creditValue,
                    "完成" + getTypeName(record.getType()) + "互助", record.getId());

            // 通知双方互助完成
            messageService.createNotification(record.getInitiatorId(),
                    "互助已完成",
                    "您的" + getTypeName(record.getType()) + "互助已完成，双方已确认",
                    "mutual_complete", record.getId());
            messageService.createNotification(record.getParticipantId(),
                    "互助已完成",
                    "您的" + getTypeName(record.getType()) + "互助已完成，双方已确认",
                    "mutual_complete", record.getId());
        }

        mutualRecordMapper.updateById(record);
    }

    @Override
    public void cancelRecord(Long userId, Long recordId) {
        MutualRecord record = getAndValidate(recordId);

        // 发起人或参与人都可以取消
        if (!record.getInitiatorId().equals(userId) && !record.getParticipantId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        if ("completed".equals(record.getStatus()) || "cancelled".equals(record.getStatus())) {
            throw new BusinessException(ResultCode.MUTUAL_STATUS_ERROR);
        }

        record.setStatus("cancelled");
        mutualRecordMapper.updateById(record);
    }

    @Override
    public PageResult<MutualRecord> getMyRecords(Long userId, String status, int pageNum, int pageSize) {
        Page<MutualRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MutualRecord> wrapper = new LambdaQueryWrapper<>();

        // 查找我发起的或我参与的
        wrapper.and(w -> w.eq(MutualRecord::getInitiatorId, userId)
                .or().eq(MutualRecord::getParticipantId, userId));

        if (StringUtils.hasText(status)) {
            wrapper.eq(MutualRecord::getStatus, status);
        }

        wrapper.orderByDesc(MutualRecord::getCreatedAt);
        Page<MutualRecord> result = mutualRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public MutualRecord getRecordDetail(Long id) {
        return getAndValidate(id);
    }

    private MutualRecord getAndValidate(Long id) {
        MutualRecord record = mutualRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.MUTUAL_RECORD_NOT_FOUND);
        }
        return record;
    }

    private int getCreditValueByType(String type) {
        return switch (type) {
            case "skill" -> 5;
            case "goods" -> 3;
            case "activity" -> 2;
            default -> 1;
        };
    }

    private String getTypeName(String type) {
        return switch (type) {
            case "skill" -> "技能交换";
            case "goods" -> "物品共享";
            case "activity" -> "临时搭伴";
            default -> "互助";
        };
    }
}
