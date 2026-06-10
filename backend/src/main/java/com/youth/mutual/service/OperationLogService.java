package com.youth.mutual.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youth.mutual.entity.OperationLog;
import com.youth.mutual.entity.User;
import com.youth.mutual.mapper.OperationLogMapper;
import com.youth.mutual.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper logMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CACHE_KEY = "admin:logs:recent";
    private static final int CACHE_SIZE = 100;

    /** 记录操作日志（写MySQL + 更新Redis缓存） */
    public void log(Long adminId, String action, String target, String detail) {
        User admin = userMapper.selectById(adminId);
        OperationLog log = new OperationLog();
        log.setAdminId(adminId);
        log.setAdminName(admin != null ? admin.getNickname() : "未知");
        log.setAction(action);
        log.setTarget(target);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);

        // 更新Redis缓存
        try {
            String json = objectMapper.writeValueAsString(log);
            redisTemplate.opsForList().leftPush(CACHE_KEY, json);
            redisTemplate.opsForList().trim(CACHE_KEY, 0, CACHE_SIZE - 1);
            redisTemplate.expire(CACHE_KEY, 7, TimeUnit.DAYS);
        } catch (Exception ignored) {}
    }

    /** 获取最近日志（Redis缓存优先） */
    public List<OperationLog> getRecentLogs() {
        return getRecentLogs(1, CACHE_SIZE);
    }

    /** 分页获取操作日志（Redis缓存优先，仅首页命中缓存） */
    public List<OperationLog> getRecentLogs(int pageNum, int pageSize) {
        // 仅第1页（大小<=CACHE_SIZE时）尝试Redis缓存
        if (pageNum == 1 && pageSize <= CACHE_SIZE) {
            try {
                List<Object> cached = redisTemplate.opsForList().range(CACHE_KEY, 0, pageSize - 1);
                if (cached != null && !cached.isEmpty()) {
                    return cached.stream().map(o -> {
                        try { return objectMapper.readValue(o.toString(), OperationLog.class); }
                        catch (Exception e) { return null; }
                    }).filter(Objects::nonNull).toList();
                }
            } catch (Exception ignored) {}
        }
        // 缓存未命中或非首页，从MySQL查
        return logMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreatedAt)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + "," + pageSize)
        );
    }

    /** 统计总日志条数（始终查MySQL保证准确） */
    public long countLogs() {
        return logMapper.selectCount(null);
    }
}
