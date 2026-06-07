package com.youth.mutual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youth.mutual.common.exception.BusinessException;
import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.common.result.ResultCode;
import com.youth.mutual.entity.GoodsPost;
import com.youth.mutual.mapper.GoodsPostMapper;
import com.youth.mutual.service.GoodsService;
import com.youth.mutual.entity.User;
import com.youth.mutual.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsPostMapper, GoodsPost> implements GoodsService {

    private final GoodsPostMapper goodsPostMapper;
    private final UserMapper userMapper;

    @Override
    public Long publishGoods(Long userId, GoodsPost goodsPost) {
        goodsPost.setUserId(userId);
        goodsPost.setStatus(1);
        goodsPost.setViewCount(0);
        goodsPostMapper.insert(goodsPost);
        return goodsPost.getId();
    }

    @Override
    public GoodsPost getGoodsDetail(Long id) {
        GoodsPost post = goodsPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.GOODS_POST_NOT_FOUND);
        }
        post.setViewCount(post.getViewCount() + 1);
        goodsPostMapper.updateById(post);
        // 填充发布者信息
        fillUserInfo(List.of(post));
        return post;
    }

    @Override
    public PageResult<GoodsPost> getGoodsList(int pageNum, int pageSize, String category, String exchangeType, String city, String keyword) {
        Page<GoodsPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GoodsPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsPost::getStatus, 1);

        if (StringUtils.hasText(category)) wrapper.eq(GoodsPost::getCategory, category);
        if (StringUtils.hasText(exchangeType)) wrapper.eq(GoodsPost::getExchangeType, exchangeType);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(GoodsPost::getTitle, keyword).or().like(GoodsPost::getDescription, keyword));
        }
        if (StringUtils.hasText(city)) {
            List<Long> cityUserIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getCity, city)
            ).stream().map(User::getId).toList();
            if (cityUserIds.isEmpty()) {
                return new PageResult<>((long) pageNum, (long) pageSize, 0L, new ArrayList<>());
            }
            wrapper.in(GoodsPost::getUserId, cityUserIds);
        }

        wrapper.orderByDesc(GoodsPost::getCreatedAt);
        Page<GoodsPost> result = goodsPostMapper.selectPage(page, wrapper);
        // 填充发布者信息
        fillUserInfo(result.getRecords());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public void offlineGoods(Long userId, Long goodsId) {
        GoodsPost post = goodsPostMapper.selectById(goodsId);
        if (post == null) throw new BusinessException(ResultCode.GOODS_POST_NOT_FOUND);
        if (!post.getUserId().equals(userId)) throw new BusinessException(ResultCode.ACCESS_DENIED);
        post.setStatus(0);
        goodsPostMapper.updateById(post);
    }

    @Override
    public void deleteGoods(Long userId, Long goodsId) {
        GoodsPost post = goodsPostMapper.selectById(goodsId);
        if (post == null) throw new BusinessException(ResultCode.GOODS_POST_NOT_FOUND);
        if (!post.getUserId().equals(userId)) throw new BusinessException(ResultCode.ACCESS_DENIED);
        goodsPostMapper.deleteById(goodsId);
    }

    /**
     * 批量填充发布者信息（昵称、头像、信用分）
     */
    private void fillUserInfo(List<GoodsPost> posts) {
        if (posts == null || posts.isEmpty()) return;
        Set<Long> userIds = posts.stream().map(GoodsPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        for (GoodsPost post : posts) {
            User user = userMap.get(post.getUserId());
            if (user != null) {
                if (post.getIsAnonymous() != null && post.getIsAnonymous() == 1) {
                    post.setNickname("匿名用户");
                    post.setAvatar(null);
                } else {
                    post.setNickname(user.getNickname());
                    post.setAvatar(user.getAvatar());
                }
                post.setCreditScore(user.getCreditScore());
            }
        }
    }
}
