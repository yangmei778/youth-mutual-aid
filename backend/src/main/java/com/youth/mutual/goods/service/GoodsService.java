package com.youth.mutual.goods.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.goods.entity.GoodsPost;

/**
 * 闲置共享服务接口
 */
public interface GoodsService {

    /** 发布物品 */
    Long publishGoods(Long userId, GoodsPost goodsPost);

    /** 获取物品详情 */
    GoodsPost getGoodsDetail(Long id);

    /** 分页查询物品列表 */
    PageResult<GoodsPost> getGoodsList(int pageNum, int pageSize, String category, String exchangeType, String city, String keyword);

    /** 下架物品 */
    void offlineGoods(Long userId, Long goodsId);
}
