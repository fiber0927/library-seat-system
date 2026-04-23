package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Seat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeatMapper extends BaseMapper<Seat> {
}
