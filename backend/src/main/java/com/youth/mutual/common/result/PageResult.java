package com.youth.mutual.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果
 */
@Data
public class PageResult<T> implements Serializable {

    /** 当前页码 */
    private Long pageNum;
    /** 每页数量 */
    private Long pageSize;
    /** 总记录数 */
    private Long total;
    /** 总页数 */
    private Long pages;
    /** 数据列表 */
    private List<T> records;

    public PageResult() {}

    public PageResult(Long pageNum, Long pageSize, Long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (total + pageSize - 1) / pageSize;
        this.records = records;
    }
}
