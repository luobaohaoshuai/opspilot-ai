package com.opspilot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.opspilot.entity.Device;
import com.opspilot.entity.DeviceCommand;
import com.opspilot.entity.DeviceCommandRequest;
import com.opspilot.mapper.DeviceCommandMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class DeviceCommandService {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "DISPLAY_MESSAGE",
            "SET_DISPLAY_MODE",
            "SET_SAMPLE_INTERVAL",
            "RUN_SELF_TEST",
            "REBOOT",
            "RELAY_ON",
            "RELAY_OFF",
            "RELAY_PULSE"
    );

    private final DeviceCommandMapper deviceCommandMapper;

    public DeviceCommandService(DeviceCommandMapper deviceCommandMapper) {
        this.deviceCommandMapper = deviceCommandMapper;
    }

    public DeviceCommand createCommand(Device device, DeviceCommandRequest request, String issuedBy) {
        String commandType = normalizeCommandType(request.getCommandType());
        String payload = request.getPayload() == null ? "" : request.getPayload().trim();
        validateHighRiskCommand(commandType, payload);
        DeviceCommand command = new DeviceCommand();
        command.setDeviceId(device.getId());
        command.setCommandType(commandType);
        command.setPayload(payload);
        command.setStatus(STATUS_PENDING);
        command.setIssuedBy(issuedBy);
        command.setCreateTime(LocalDateTime.now());
        deviceCommandMapper.insert(command);
        return command;
    }

    public List<DeviceCommand> listByDeviceId(Long deviceId) {
        QueryWrapper<DeviceCommand> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .orderByDesc("create_time")
                .last("LIMIT 100");
        return deviceCommandMapper.selectList(wrapper);
    }

    @Transactional
    public DeviceCommand pollNextCommand(Long deviceId) {
        QueryWrapper<DeviceCommand> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq("status", STATUS_PENDING)
                .orderByAsc("create_time")
                .last("LIMIT 1");
        DeviceCommand command = deviceCommandMapper.selectOne(wrapper);
        if (command == null) {
            return null;
        }

        LocalDateTime sentTime = LocalDateTime.now();
        UpdateWrapper<DeviceCommand> claim = new UpdateWrapper<>();
        claim.eq("id", command.getId())
                .eq("status", STATUS_PENDING)
                .set("status", STATUS_SENT)
                .set("sent_time", sentTime);
        if (deviceCommandMapper.update(null, claim) != 1) {
            return null;
        }

        command.setStatus(STATUS_SENT);
        command.setSentTime(sentTime);
        return command;
    }

    public DeviceCommand ackCommand(Long deviceId, Long commandId, boolean success, String resultMessage) {
        DeviceCommand command = deviceCommandMapper.selectById(commandId);
        if (command == null || !deviceId.equals(command.getDeviceId())) {
            return null;
        }

        command.setStatus(success ? STATUS_DONE : STATUS_FAILED);
        command.setResultMessage(resultMessage == null || resultMessage.isBlank()
                ? (success ? "执行成功" : "执行失败")
                : resultMessage.trim());
        command.setDoneTime(LocalDateTime.now());
        deviceCommandMapper.updateById(command);
        return command;
    }

    public String normalizeCommandType(String commandType) {
        String normalized = commandType == null ? "" : commandType.trim().toUpperCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("不支持的设备命令: " + commandType);
        }
        return normalized;
    }

    private void validateHighRiskCommand(String commandType, String payload) {
        if ("REBOOT".equals(commandType)
                && !"CONFIRM_REBOOT".equalsIgnoreCase(payload)
                && !payload.contains("确认重启")) {
            throw new IllegalArgumentException("REBOOT 命令需要 payload=CONFIRM_REBOOT 或包含“确认重启”");
        }
    }

    public boolean hasRecentCommand(Long deviceId, String commandType, int withinSeconds) {
        QueryWrapper<DeviceCommand> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq("command_type", commandType)
                .ge("create_time", LocalDateTime.now().minusSeconds(withinSeconds));
        return deviceCommandMapper.selectCount(wrapper) > 0;
    }

    @Transactional
    public int expireStaleCommands(int timeoutSeconds) {
        QueryWrapper<DeviceCommand> wrapper = new QueryWrapper<>();
        wrapper.eq("status", STATUS_SENT);
        LocalDateTime deadline = LocalDateTime.now().minusSeconds(timeoutSeconds);
        wrapper.le("sent_time", deadline);

        List<DeviceCommand> staleList = deviceCommandMapper.selectList(wrapper);
        int count = 0;
        for (DeviceCommand cmd : staleList) {
            cmd.setStatus(STATUS_FAILED);
            cmd.setResultMessage("命令超时，ESP32 未在 " + timeoutSeconds + " 秒内回传 ack");
            cmd.setDoneTime(LocalDateTime.now());
            deviceCommandMapper.updateById(cmd);
            count++;
        }
        return count;
    }
}
