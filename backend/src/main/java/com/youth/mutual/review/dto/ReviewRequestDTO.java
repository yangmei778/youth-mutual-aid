package com.youth.mutual.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交评价DTO
 */
@Data
public class ReviewRequestDTO {

    @NotNull(message = "互助记录ID不能为空")
    private Long recordId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer rating;

    private String content;
}
