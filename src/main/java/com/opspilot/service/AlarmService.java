package com.opspilot.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opspilot.entity.Alarm;
import com.opspilot.mapper.AlarmMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmService {
    private final AlarmMapper alarmMapper;
    public AlarmService(AlarmMapper alarmMapper){
        this.alarmMapper = alarmMapper;
    }

    public void addAlarm(Alarm alarm){
        alarmMapper.insert(alarm);
    }

    public List<Alarm> listAll(){
        return alarmMapper.selectList(null);
    }
    public Alarm getById(Long id){
        return alarmMapper.selectById(id);
    }
    public List<Alarm> listByDeviceId(Long deviceId) {
        QueryWrapper<Alarm> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .orderByDesc("create_time")
                .last("LIMIT 200");
        return alarmMapper.selectList(wrapper);
    }

    public boolean markResolved(Long deviceId, Long alarmId) {
        QueryWrapper<Alarm> wrapper = new QueryWrapper<>();
        wrapper.eq("id", alarmId).eq("device_id", deviceId).last("LIMIT 1");
        Alarm alarm = alarmMapper.selectOne(wrapper);
        if (alarm == null) {
            return false;
        }
        alarm.setStatus("已处理");
        return alarmMapper.updateById(alarm) == 1;
    }

    public long countByStatus(String status) {
        QueryWrapper<Alarm> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status);
        return alarmMapper.selectCount(wrapper);
    }

    public boolean hasDuplicateAlarm(Long deviceId, String alarmType, java.time.LocalDateTime since) {
        QueryWrapper<Alarm> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq("alarm_type", alarmType)
                .ge("create_time", since)
                .orderByDesc("create_time")
                .last("LIMIT 1");
        return alarmMapper.selectCount(wrapper) > 0;
    }

    public void escalateAlarm(Long deviceId, String alarmType, java.time.LocalDateTime since, String newValue) {
        QueryWrapper<Alarm> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq("alarm_type", alarmType)
                .ge("create_time", since);
        Alarm existing = alarmMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setStatus("反复触发");
            existing.setAlarmValue(existing.getAlarmValue() + " | " + newValue);
            alarmMapper.updateById(existing);
        }
    }
}
