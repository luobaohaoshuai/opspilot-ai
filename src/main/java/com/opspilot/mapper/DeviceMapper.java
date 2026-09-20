package com.opspilot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opspilot.entity.Device;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
}