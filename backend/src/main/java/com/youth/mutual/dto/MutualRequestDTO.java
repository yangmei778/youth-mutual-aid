package com.youth.mutual.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发起互助请求DTO
 */
@Data
public class MutualRequestDTO {

    @NotBlank(message = "互助类型不能为空")
    private String type;

    @NotNull(message = "参与人ID不能为空")
    private Long participantId;

    @NotNull(message = "关联ID不能为空")
    private Long relatedId;

    private String requestMessage;
}
