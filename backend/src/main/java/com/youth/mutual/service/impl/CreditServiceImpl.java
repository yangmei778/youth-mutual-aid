package com.youth.mutual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youth.mutual.common.enums.CreditLevelEnum;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.ResultCode;
import com.youth.mutual.entity.CreditLog;
import com.youth.mutual.mapper.CreditLogMapper;
import com.youth.mutual.service.CreditService;
import com.youth.mutual.entity.User;
import com.youth.mutual.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditLogMapper creditLogMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void addCredit(Long userId, int changeValue, String reason, Long relatedId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);

        int beforeScore = user.getCreditScore();
        int afterScore = Math.max(0, beforeScore + changeValue);

        // 更新用户信用分
        user.setCreditScore(afterScore);
        userMapper.updateById(user);

        // 记录变动日志
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setChangeValue(changeValue);
        log.setBeforeScore(beforeScore);
        log.setAfterScore(afterScore);
        log.setReason(reason);
        log.setRelatedId(relatedId);
        creditLogMapper.insert(log);
    }

    @Override
    public Map<String, Object> getCreditInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);

        Map<String, Object> info = new HashMap<>();
        info.put("creditScore", user.getCreditScore());
        CreditLevelEnum level = CreditLevelEnum.getByScore(user.getCreditScore());
        info.put("levelName", level.getName());
        info.put("levelBadge", level.getBadge());
        info.put("nextLevelScore", level != CreditLevelEnum.DIAMOND ? level.getMaxScore() + 1 : null);
        return info;
    }

    @Override
    public PageResult<CreditLog> getCreditLogs(Long userId, int pageNum, int pageSize) {
        Page<CreditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CreditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditLog::getUserId, userId)
                .orderByDesc(CreditLog::getCreatedAt);
        Page<CreditLog> result = creditLogMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<Map<String, Object>> getLeaderboard(int limit) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(User::getId, User::getNickname, User::getAvatar, User::getCreditScore)
                .orderByDesc(User::getCreditScore)
                .last("LIMIT " + Math.max(1, Math.min(limit, 100)));
        List<User> users = userMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (User user : users) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", rank++);
            entry.put("userId", user.getId());
            entry.put("nickname", user.getNickname());
            entry.put("avatar", user.getAvatar());
            entry.put("creditScore", user.getCreditScore());
            CreditLevelEnum level = CreditLevelEnum.getByScore(user.getCreditScore());
            entry.put("levelName", level.getName());
            entry.put("levelBadge", level.getBadge());
            result.add(entry);
        }
        return result;
    }
}
