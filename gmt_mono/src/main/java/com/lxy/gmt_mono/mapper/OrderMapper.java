package com.lxy.gmt_mono.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxy.gmt_mono.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
