package com.opspilot.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opspilot.entity.Device;
import com.opspilot.mapper.DeviceMapper;
import com.opspilot.common.security.DeviceTokenHasher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {
    private final DeviceMapper deviceMapper;

    public DeviceService(DeviceMapper deviceMapper){
        this.deviceMapper = deviceMapper;
    }

    public String addDevice(Device device){
        String rawToken = device.getDeviceToken();
        if (rawToken == null || rawToken.isBlank()) {
            rawToken = generateDeviceToken();
        }
        device.setDeviceToken(DeviceTokenHasher.hash(rawToken));
        deviceMapper.insert(device);
        return rawToken;
    }
    public List<Device> listDevices() {
        return deviceMapper.selectList(null);
    }
    public Device getById(Long id) {
        return deviceMapper.selectById(id);
    }
    public Device getByDeviceCode(String deviceCode) {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("device_code", deviceCode);
        return deviceMapper.selectOne(wrapper);
    }

    public void updateDevice(Device device){
        deviceMapper.updateById(device);
    }

    public void removeDevice(Long id){
        deviceMapper.deleteById(id);
    }

    public long countDevices() {
        return deviceMapper.selectCount(null);
    }

    public long countByOnline(boolean online) {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("online", online);
        return deviceMapper.selectCount(wrapper);
    }

    public String generateDeviceToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
