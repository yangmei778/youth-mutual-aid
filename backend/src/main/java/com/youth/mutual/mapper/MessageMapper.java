package com.youth.mutual.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youth.mutual.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
