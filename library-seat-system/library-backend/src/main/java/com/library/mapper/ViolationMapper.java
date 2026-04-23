package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Violation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ViolationMapper extends BaseMapper<Violation> {
}
