package com.opspilot.controller;

import com.opspilot.common.result.ErrorCode;
import com.opspilot.common.result.Result;
import com.opspilot.entity.Alarm;
import com.opspilot.entity.Device;
import com.opspilot.entity.DeviceCommand;
import com.opspilot.entity.DeviceCommandAckRequest;
import com.opspilot.entity.DeviceCommandRequest;
import com.opspilot.entity.DeviceConfig;
import com.opspilot.entity.DeviceConfigRequest;
import com.opspilot.entity.DeviceData;
import com.opspilot.entity.DeviceReportRequest;
import com.opspilot.entity.DeviceRegistrationResponse;
import com.opspilot.service.AlarmService;
import com.opspilot.service.DeviceAuthService;
import com.opspilot.service.DeviceCommandService;
import com.opspilot.service.DeviceConfigService;
import com.opspilot.service.DeviceDataService;
import com.opspilot.service.DeviceReportService;
import com.opspilot.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
public class DeviceController {
    private final DeviceReportService reportService;

    private final DeviceService deviceService;

    private final DeviceDataService deviceDataService;

    private final AlarmService alarmService;

    private final DeviceConfigService deviceConfigService;

    private final DeviceCommandService deviceCommandService;

    private final DeviceAuthService deviceAuthService;


    public DeviceController(DeviceReportService reportService,
                            DeviceService deviceService,
                            DeviceDataService deviceDataService,
                            AlarmService alarmService,
                            DeviceConfigService deviceConfigService,
                            DeviceCommandService deviceCommandService,
                            DeviceAuthService deviceAuthService) {
        this.reportService = reportService;
        this.deviceService = deviceService;
        this.deviceDataService = deviceDataService;
        this.alarmService = alarmService;
        this.deviceConfigService = deviceConfigService;
        this.deviceCommandService = deviceCommandService;
        this.deviceAuthService = deviceAuthService;
    }
    @PostMapping("/report")
    public Result<String> report(@Valid @RequestBody DeviceReportRequest request,
                                 @RequestHeader(name = "X-Device-Token", required = false) String deviceToken) {
        Device device = deviceService.getByDeviceCode(request.getDeviceCode());
        if (device == null) {
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }
        if (!deviceAuthService.canWrite(device, deviceToken)) {
            return Result.fail(ErrorCode.FORBIDDEN, "设备 Token 不正确");
        }

        return Result.ok(reportService.handleReport(
                device,
                request.getTemperature(),
                request.getHumidity(),
                request.getRssi(),
                request.getUptimeSeconds(),
                request.getFirmwareVersion()
        ));
    }

    @GetMapping("/list")
    public Result<List<Device>> list() {
        return Result.ok(deviceService.listDevices());
    }

    @GetMapping("/{deviceCode}/data")
    public Result<List<DeviceData>> history(@PathVariable String deviceCode){
        Device device = deviceService.getByDeviceCode(deviceCode);
        if(device == null){
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }
        List<DeviceData> list = deviceDataService.listByDeviceId(device.getId());
        return Result.ok(list);
    }

    @GetMapping("/{deviceCode}/alarms")
    public Result<List<Alarm>> alarms(@PathVariable String deviceCode) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if(device == null){
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }
        List<Alarm> list = alarmService.listByDeviceId(device.getId());
        return Result.ok(list);
    }

    @GetMapping("/{deviceCode}/config")
    public Result<DeviceConfig> config(@PathVariable String deviceCode) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if(device == null){
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }
        return Result.ok(deviceConfigService.getOrCreateByDeviceId(device.getId()));
    }

    @PutMapping("/{deviceCode}/config")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DeviceConfig> updateConfig(@PathVariable String deviceCode,
                                             @Valid @RequestBody DeviceConfigRequest request) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if(device == null){
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }

        DeviceConfig config = deviceConfigService.updateConfig(device.getId(), request);
        if (request.getSampleIntervalSeconds() != null) {
            DeviceCommandRequest command = new DeviceCommandRequest();
            command.setCommandType("SET_SAMPLE_INTERVAL");
            command.setPayload(String.valueOf(config.getSampleIntervalSeconds()));
            deviceCommandService.createCommand(device, command, currentOperator());
        }
        if (request.getDisplayMode() != null && !request.getDisplayMode().isBlank()) {
            DeviceCommandRequest command = new DeviceCommandRequest();
            command.setCommandType("SET_DISPLAY_MODE");
            command.setPayload(config.getDisplayMode());
            deviceCommandService.createCommand(device, command, currentOperator());
        }

        return Result.ok(config);
    }

    @PostMapping("/{deviceCode}/commands")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DeviceCommand> createCommand(@PathVariable String deviceCode,
                                               @Valid @RequestBody DeviceCommandRequest request) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if(device == null){
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }
        try {
            return Result.ok(deviceCommandService.createCommand(device, request, currentOperator()));
        } catch (IllegalArgumentException e) {
            return Result.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{deviceCode}/commands")
    public Result<List<DeviceCommand>> commands(@PathVariable String deviceCode) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if(device == null){
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }
        return Result.ok(deviceCommandService.listByDeviceId(device.getId()));
    }

    @GetMapping("/{deviceCode}/commands/next")
    public Result<DeviceCommand> nextCommand(@PathVariable String deviceCode,
                                             @RequestHeader(name = "X-Device-Token", required = false) String deviceToken) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if(device == null){
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }
        if (!deviceAuthService.hasValidDeviceToken(device, deviceToken)) {
            return Result.fail(ErrorCode.FORBIDDEN, "设备 Token 不正确");
        }
        return Result.ok(deviceCommandService.pollNextCommand(device.getId()));
    }

    @PostMapping("/{deviceCode}/commands/{commandId}/ack")
    public Result<DeviceCommand> ackCommand(@PathVariable String deviceCode,
                                            @PathVariable Long commandId,
                                            @RequestHeader(name = "X-Device-Token", required = false) String deviceToken,
                                            @RequestBody(required = false) DeviceCommandAckRequest request) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if(device == null){
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }
        if (!deviceAuthService.hasValidDeviceToken(device, deviceToken)) {
            return Result.fail(ErrorCode.FORBIDDEN, "设备 Token 不正确");
        }
        boolean success = request == null || request.getSuccess() == null || request.getSuccess();
        String resultMessage = request == null ? null : request.getResultMessage();
        DeviceCommand command = deviceCommandService.ackCommand(device.getId(), commandId, success, resultMessage);
        if (command == null) {
            return Result.fail(ErrorCode.NOT_FOUND, "设备命令不存在");
        }
        return Result.ok(command);
    }

    @PatchMapping("/{deviceCode}/alarms/{alarmId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> resolveAlarm(@PathVariable String deviceCode, @PathVariable Long alarmId) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return Result.fail(ErrorCode.DEVICE_NOT_FOUND);
        }
        if (!alarmService.markResolved(device.getId(), alarmId)) {
            return Result.fail(ErrorCode.NOT_FOUND, "该设备下不存在此告警");
        }
        return Result.ok("告警已处理");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DeviceRegistrationResponse> addDevice(@Valid @RequestBody Device device) {
        String rawToken = deviceService.addDevice(device);
        return Result.ok(new DeviceRegistrationResponse(device.getDeviceCode(), rawToken));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateDevice(@PathVariable Long id, @Valid @RequestBody Device device) {
        device.setId(id);
        device.setDeviceToken(null);
        deviceService.updateDevice(device);
        return Result.ok("设备已更新");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> removeDevice(@PathVariable Long id) {
        deviceService.removeDevice(id);
        return Result.ok("设备已删除");
    }

    private String currentOperator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "manager";
        }
        return authentication.getName();
    }
}
