package com.opspilot.service;

import com.opspilot.entity.Device;
import com.opspilot.entity.DeviceCommand;
import com.opspilot.entity.DeviceCommandRequest;
import com.opspilot.mapper.DeviceCommandMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceCommandServiceTest {

    @Mock
    private DeviceCommandMapper deviceCommandMapper;

    @InjectMocks
    private DeviceCommandService deviceCommandService;

    @Test
    void 创建命令应进入Pending状态() {
        when(deviceCommandMapper.insert(any(DeviceCommand.class))).thenAnswer(invocation -> {
            DeviceCommand command = invocation.getArgument(0);
            command.setId(100L);
            return 1;
        });

        Device device = new Device();
        device.setId(1L);
        DeviceCommandRequest request = new DeviceCommandRequest();
        request.setCommandType("DISPLAY_MESSAGE");
        request.setPayload("正在巡检");

        DeviceCommand command = deviceCommandService.createCommand(device, request, "agent");

        assertEquals(100L, command.getId());
        assertEquals("DISPLAY_MESSAGE", command.getCommandType());
        assertEquals("PENDING", command.getStatus());
        assertEquals("agent", command.getIssuedBy());
    }

    @Test
    void 拉取命令应从Pending变为Sent() {
        DeviceCommand command = new DeviceCommand();
        command.setId(10L);
        command.setDeviceId(1L);
        command.setStatus("PENDING");
        when(deviceCommandMapper.selectOne(any())).thenReturn(command);
        when(deviceCommandMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

        DeviceCommand result = deviceCommandService.pollNextCommand(1L);

        assertNotNull(result);
        assertEquals("SENT", result.getStatus());
        assertNotNull(result.getSentTime());
        verify(deviceCommandMapper).update(isNull(), any(UpdateWrapper.class));
    }

    @Test
    void 回执成功应标记Done() {
        DeviceCommand command = new DeviceCommand();
        command.setId(10L);
        command.setDeviceId(1L);
        command.setStatus("SENT");
        when(deviceCommandMapper.selectById(10L)).thenReturn(command);

        DeviceCommand result = deviceCommandService.ackCommand(1L, 10L, true, "屏幕已显示");

        assertNotNull(result);
        assertEquals("DONE", result.getStatus());
        assertEquals("屏幕已显示", result.getResultMessage());
        assertNotNull(result.getDoneTime());
        verify(deviceCommandMapper).updateById(command);
    }

    @Test
    void 不支持的命令类型应拒绝() {
        Device device = new Device();
        device.setId(1L);
        DeviceCommandRequest request = new DeviceCommandRequest();
        request.setCommandType("POWER_OFF");

        assertThrows(IllegalArgumentException.class,
                () -> deviceCommandService.createCommand(device, request, "agent"));
        verify(deviceCommandMapper, never()).insert(any());
    }

    @Test
    void 重启命令没有二次确认应拒绝() {
        Device device = new Device();
        device.setId(1L);
        DeviceCommandRequest request = new DeviceCommandRequest();
        request.setCommandType("REBOOT");
        request.setPayload("now");

        assertThrows(IllegalArgumentException.class,
                () -> deviceCommandService.createCommand(device, request, "admin"));
        verify(deviceCommandMapper, never()).insert(any());
    }

    @Test
    void 并发领取失败时不应返回已被其他请求领取的命令() {
        DeviceCommand command = new DeviceCommand();
        command.setId(10L);
        command.setDeviceId(1L);
        command.setStatus("PENDING");
        when(deviceCommandMapper.selectOne(any())).thenReturn(command);
        when(deviceCommandMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(0);

        assertNull(deviceCommandService.pollNextCommand(1L));
    }

    @Test
    void 超过超时时间的Sent命令应标记为Failed() {
        DeviceCommand stale1 = new DeviceCommand();
        stale1.setId(1L);
        stale1.setDeviceId(1L);
        stale1.setStatus("SENT");
        stale1.setSentTime(java.time.LocalDateTime.now().minusSeconds(60));

        DeviceCommand stale2 = new DeviceCommand();
        stale2.setId(2L);
        stale2.setDeviceId(1L);
        stale2.setStatus("SENT");
        stale2.setSentTime(java.time.LocalDateTime.now().minusSeconds(90));

        when(deviceCommandMapper.selectList(any())).thenReturn(java.util.List.of(stale1, stale2));

        int count = deviceCommandService.expireStaleCommands(30);

        assertEquals(2, count);
        assertEquals("FAILED", stale1.getStatus());
        assertEquals("FAILED", stale2.getStatus());
        assertTrue(stale1.getResultMessage().contains("超时"));
        verify(deviceCommandMapper, times(2)).updateById(any(DeviceCommand.class));
    }
}
