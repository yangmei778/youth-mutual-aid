package com.youth.mutual.goods.controller;

import com.youth.mutual.auth.UserContext;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.R;
import com.youth.mutual.goods.entity.GoodsPost;
import com.youth.mutual.goods.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 闲置共享控制器
 */
@Tag(name = "闲置共享")
@RestController
@RequestMapping("/v1/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final UserContext userContext;

    @Operation(summary = "发布物品")
    @PostMapping("/posts")
    public R<Long> publishGoods(@RequestBody GoodsPost goodsPost) {
        Long userId = userContext.getRequiredUserId();
        Long id = goodsService.publishGoods(userId, goodsPost);
        return R.ok(id);
    }

    @Operation(summary = "获取物品列表")
    @GetMapping("/posts")
    public R<PageResult<GoodsPost>> getGoodsList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String exchangeType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword) {
        return R.ok(goodsService.getGoodsList(pageNum, pageSize, category, exchangeType, city, keyword));
    }

    @Operation(summary = "获取物品详情")
    @GetMapping("/posts/{id}")
    public R<GoodsPost> getGoodsDetail(@PathVariable Long id) {
        return R.ok(goodsService.getGoodsDetail(id));
    }

    @Operation(summary = "下架物品")
    @PutMapping("/posts/{id}/offline")
    public R<Void> offlineGoods(@PathVariable Long id) {
        Long userId = userContext.getRequiredUserId();
        goodsService.offlineGoods(userId, id);
        return R.ok();
    }
}
