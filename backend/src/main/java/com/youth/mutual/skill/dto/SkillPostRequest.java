package com.youth.mutual.skill.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 技能发布请求
 */
@Data
public class SkillPostRequest {

    @NotBlank(message = "技能类型不能为空")
    private String type; // teach / learn

    @NotBlank(message = "技能标题不能为空")
    private String title;

    private String description;

    private String category;

    private String availableTime;

    private String preferredLocation;

    private Integer onlineSupport;
}
