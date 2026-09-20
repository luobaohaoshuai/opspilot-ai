package com.opspilot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opspilot.entity.DeviceData;
import com.opspilot.mapper.DeviceDataMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceDataService {

    private final DeviceDataMapper deviceDataMapper;

    public DeviceDataService(DeviceDataMapper deviceDataMapper){
        this.deviceDataMapper = deviceDataMapper;
    }

    public void addDeviceData(DeviceData deviceData) {
        deviceDataMapper.insert(deviceData);
    }

    public List<DeviceData> listALL() {
        return deviceDataMapper.selectList(null);
    }
    public DeviceData getById(Long id) {
        return deviceDataMapper.selectById(id);

    }
    public List<DeviceData> listByDeviceId(Long deviceId) {
        QueryWrapper<DeviceData> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .orderByDesc("report_time")
                .last("LIMIT 200");
        return deviceDataMapper.selectList(wrapper);
    }

    public DeviceData getLatestData(Long deviceId) {
        QueryWrapper<DeviceData> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
               .orderByDesc("report_time")
               .last("LIMIT 1");
        return deviceDataMapper.selectOne(wrapper);
    }

    public List<DeviceData> listRecentByDeviceId(Long deviceId, int hours) {
        QueryWrapper<DeviceData> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .ge("report_time", LocalDateTime.now().minusHours(Math.max(1, hours)))
                .orderByDesc("report_time")
                .last("LIMIT 500");
        return deviceDataMapper.selectList(wrapper);
    }
}
