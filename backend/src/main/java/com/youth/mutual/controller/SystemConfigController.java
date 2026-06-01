package com.youth.mutual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youth.mutual.common.result.R;
import com.youth.mutual.entity.SystemConfig;
import com.youth.mutual.mapper.SystemConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "系统配置")
@RestController
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigMapper configMapper;

    @Operation(summary = "公开 - 获取单个配置")
    @GetMapping("/v1/public/config/{key}")
    public R<Map<String, String>> getPublicConfig(@PathVariable String key) {
        SystemConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("value", config != null ? config.getConfigValue() : getDefaultValue(key));
        return R.ok(result);
    }

    @Operation(summary = "管理员 - 获取所有配置")
    @GetMapping("/v1/admin/config")
    @PreAuthorize("hasRole('ADMIN')")
    public R<List<SystemConfig>> getAllConfig() {
        List<SystemConfig> list = configMapper.selectList(null);
        // 补充默认值
        Set<String> existing = new HashSet<>();
        for (SystemConfig c : list) existing.add(c.getConfigKey());
        for (String key : getDefaultKeys()) {
            if (!existing.contains(key)) {
                SystemConfig def = new SystemConfig();
                def.setConfigKey(key);
                def.setConfigValue(getDefaultValue(key));
                def.setDescription(getDefaultDesc(key));
                list.add(def);
            }
        }
        return R.ok(list);
    }

    @Operation(summary = "管理员 - 更新配置")
    @PutMapping("/v1/admin/config/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("configValue");
        SystemConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        if (config != null) {
            config.setConfigValue(value);
            configMapper.updateById(config);
        } else {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(body.getOrDefault("description", ""));
            configMapper.insert(config);
        }
        return R.ok();
    }

    // ── 默认值 ──

    private List<String> getDefaultKeys() {
        return List.of("skill_categories", "goods_categories", "activity_types");
    }

    private String getDefaultValue(String key) {
        return switch (key) {
            case "skill_categories" -> "[\"编程\",\"语言\",\"音乐\",\"绘画\",\"健身\",\"烹饪\",\"摄影\",\"设计\",\"写作\",\"其他\"]";
            case "goods_categories" -> "[\"数码\",\"书籍\",\"家居\",\"服饰\",\"运动\",\"食品\",\"其他\"]";
            case "activity_types" -> "[{\"value\":\"meal\",\"label\":\"拼饭\"},{\"value\":\"exhibition\",\"label\":\"拼展\"},{\"value\":\"travel\",\"label\":\"拼旅行\"},{\"value\":\"other\",\"label\":\"其他\"}]";
            default -> "[]";
        };
    }

    private String getDefaultDesc(String key) {
        return switch (key) {
            case "skill_categories" -> "技能分类列表";
            case "goods_categories" -> "物品分类列表";
            case "activity_types" -> "活动类型列表";
            default -> "";
        };
    }
}
