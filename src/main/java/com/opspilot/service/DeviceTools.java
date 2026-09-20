package com.opspilot.service;

import com.opspilot.entity.Alarm;
import com.opspilot.entity.Device;
import com.opspilot.entity.DeviceCommand;
import com.opspilot.entity.DeviceCommandRequest;
import com.opspilot.entity.DeviceConfig;
import com.opspilot.entity.DeviceConfigRequest;
import com.opspilot.entity.DeviceData;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class DeviceTools {

    private static final Logger log = LoggerFactory.getLogger(DeviceTools.class);

    private final DeviceService deviceService;
    private final ToolCallGuard guard;
    private final AlarmService alarmService;
    private final DeviceDataService deviceDataService;
    private final DeviceConfigService deviceConfigService;
    private final DeviceCommandService deviceCommandService;
    private final ToolTraceCollector collector;

    public DeviceTools(DeviceService deviceService,
                       DeviceDataService deviceDataService,
                       AlarmService alarmService,
                       DeviceConfigService deviceConfigService,
                       DeviceCommandService deviceCommandService,
                       ToolCallGuard guard,
                       ToolTraceCollector collector) {
        this.deviceService = deviceService;
        this.deviceDataService = deviceDataService;
        this.alarmService = alarmService;
        this.deviceConfigService = deviceConfigService;
        this.deviceCommandService = deviceCommandService;
        this.guard = guard;
        this.collector = collector;
    }

    @Tool(value = {
            "Query the real-time status of a device by its exact deviceCode (e.g. ESP32-001).",
            "Returns online/offline status, latest temperature, humidity, WiFi RSSI, firmware, threshold config, and location.",
            "Use when the user asks: 'ESP32-001 status', 'Is ESP32-001 online?', 'What is the temperature of ESP32-001?'",
            "IMPORTANT: deviceCode must be exact, do not guess or substitute."
    })
    public String getDeviceStatus(String deviceCode, @ToolMemoryId String memoryId) {
        log.info("工具调用: getDeviceStatus(deviceCode={})", deviceCode);
        long start = System.currentTimeMillis();
        String traceKey = memoryKey(memoryId);
        String reject = guard.check(traceKey, "getDeviceStatus", deviceCode);
        if (reject != null) {
            return record(traceKey, "getDeviceStatus", deviceCode, reject, start);
        }

        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return record(traceKey, "getDeviceStatus", deviceCode, "设备不存在: " + deviceCode, start);
        }

        DeviceData latest = deviceDataService.getLatestData(device.getId());
        DeviceConfig config = deviceConfigService.getOrCreateByDeviceId(device.getId());
        if (latest == null) {
            return record(traceKey, "getDeviceStatus", deviceCode,
                    device.getName() + " | 暂无遥测数据 | " + device.getLocation(), start);
        }

        String r = device.getName() + " | " +
                (Boolean.TRUE.equals(device.getOnline()) ? "在线" : "离线") + " | " +
                "温度 " + latest.getTemperature() + "℃" +
                " / 阈值 " + config.getTemperatureThreshold() + "℃ | " +
                "湿度 " + latest.getHumidity() + "% | " +
                "RSSI " + valueOrDash(latest.getRssi()) + "dBm | " +
                "固件 " + valueOrDash(latest.getFirmwareVersion()) + " | " +
                "采样间隔 " + config.getSampleIntervalSeconds() + "s | " +
                device.getLocation();
        return record(traceKey, "getDeviceStatus", deviceCode, r, start);
    }

    @Tool(value = {
            "Query alarm history for a device by exact deviceCode.",
            "Returns alarms with type, value, status, and time.",
            "Use when the user asks for alarms, alerts, or unresolved problems.",
            "IMPORTANT: deviceCode must be exact, do not guess or substitute."
    })
    public String getAlarmHistory(String deviceCode, @ToolMemoryId String memoryId) {
        log.info("工具调用: getAlarmHistory(deviceCode={})", deviceCode);
        long start = System.currentTimeMillis();
        String traceKey = memoryKey(memoryId);

        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return record(traceKey, "getAlarmHistory", deviceCode, "设备不存在: " + deviceCode, start);
        }

        List<Alarm> alarms = alarmService.listByDeviceId(device.getId());
        if (alarms.isEmpty()) {
            return record(traceKey, "getAlarmHistory", deviceCode, device.getName() + " | 暂无告警记录", start);
        }

        StringBuilder result = new StringBuilder(device.getName()).append(" 告警历史:\n");
        for (int i = 0; i < alarms.size(); i++) {
            Alarm a = alarms.get(i);
            result.append(i + 1).append(". [").append(a.getAlarmType()).append("] ")
                    .append(a.getAlarmValue())
                    .append(" | ").append(a.getStatus())
                    .append(" | ").append(a.getCreateTime()).append("\n");
        }
        return record(traceKey, "getAlarmHistory", deviceCode, result.toString(), start);
    }

    @Tool(value = {
            "Replay recent telemetry logs for one device.",
            "Parameters: exact deviceCode and hours (1-72).",
            "Returns average temperature, max temperature, average humidity, sample count, and trend direction.",
            "Use when the user asks for trend, recent logs, or whether temperature is rising."
    })
    public String getDeviceLogs(String deviceCode, int hours, @ToolMemoryId String memoryId) {
        long start = System.currentTimeMillis();
        String traceKey = memoryKey(memoryId);
        int safeHours = Math.max(1, Math.min(72, hours));
        String args = deviceCode + " | hours=" + safeHours;

        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return record(traceKey, "getDeviceLogs", args, "设备不存在: " + deviceCode, start);
        }

        List<DeviceData> data = deviceDataService.listRecentByDeviceId(device.getId(), safeHours);
        if (data.isEmpty()) {
            return record(traceKey, "getDeviceLogs", args, device.getName() + " 最近 " + safeHours + " 小时暂无遥测", start);
        }

        BigDecimal tempSum = BigDecimal.ZERO;
        BigDecimal humiditySum = BigDecimal.ZERO;
        BigDecimal maxTemp = data.get(0).getTemperature();
        for (DeviceData item : data) {
            tempSum = tempSum.add(item.getTemperature());
            humiditySum = humiditySum.add(item.getHumidity());
            if (item.getTemperature().compareTo(maxTemp) > 0) {
                maxTemp = item.getTemperature();
            }
        }
        BigDecimal count = new BigDecimal(data.size());
        BigDecimal avgTemp = tempSum.divide(count, 2, RoundingMode.HALF_UP);
        BigDecimal avgHumidity = humiditySum.divide(count, 2, RoundingMode.HALF_UP);
        DeviceData latest = data.get(0);
        DeviceData oldest = data.get(data.size() - 1);
        BigDecimal delta = latest.getTemperature().subtract(oldest.getTemperature());
        String trend = delta.compareTo(new BigDecimal("0.5")) > 0
                ? "持续上升"
                : delta.compareTo(new BigDecimal("-0.5")) < 0 ? "持续下降" : "基本稳定";

        String r = device.getName() + " 最近 " + safeHours + " 小时遥测: "
                + "样本 " + data.size() + " 条, "
                + "平均温度 " + avgTemp + "℃, "
                + "最高温度 " + maxTemp + "℃, "
                + "平均湿度 " + avgHumidity + "%, "
                + "趋势 " + trend + " (变化 " + delta + "℃)";
        return record(traceKey, "getDeviceLogs", args, r, start);
    }

    @Tool(value = {
            "Dynamically set a device alert threshold.",
            "type supports: temperature, humidity_min, humidity_max.",
            "Use when the user asks to adjust alert threshold or make alerts more/less sensitive."
    })
    public String setAlertThreshold(String deviceCode, String type, double value, @ToolMemoryId String memoryId) {
        long start = System.currentTimeMillis();
        String traceKey = memoryKey(memoryId);
        String args = deviceCode + " | " + type + "=" + value;
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return record(traceKey, "setAlertThreshold", args, "设备不存在: " + deviceCode, start);
        }

        DeviceConfigRequest request = new DeviceConfigRequest();
        BigDecimal threshold = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
        String normalized = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "temperature", "temp", "temp_high", "temperature_high" -> request.setTemperatureThreshold(threshold);
            case "humidity_min", "humidity_low", "humidity_lower" -> request.setHumidityMinThreshold(threshold);
            case "humidity_max", "humidity_high", "humidity_upper" -> request.setHumidityMaxThreshold(threshold);
            default -> {
                return record(traceKey, "setAlertThreshold", args,
                        "不支持的阈值类型: " + type + "，可选 temperature / humidity_min / humidity_max", start);
            }
        }

        DeviceConfig config = deviceConfigService.updateConfig(device.getId(), request);
        String r = "已更新 " + deviceCode + " 阈值: 温度>"
                + config.getTemperatureThreshold() + "℃, 湿度 "
                + config.getHumidityMinThreshold() + "%-" + config.getHumidityMaxThreshold() + "%";
        return record(traceKey, "setAlertThreshold", args, r, start);
    }

    @Tool(value = {
            "Issue a real hardware command to ESP32 through the command queue.",
            "commandType: DISPLAY_MESSAGE, SET_DISPLAY_MODE, SET_SAMPLE_INTERVAL, RUN_SELF_TEST, REBOOT, RELAY_ON, RELAY_OFF, RELAY_PULSE.",
            "RELAY_ON: turn on relay (fan/cooling). RELAY_OFF: turn off relay. RELAY_PULSE: pulse relay 500ms.",
            "For REBOOT, use only when the user explicitly asks to reboot the device.",
            "The ESP32 will poll the next command and acknowledge DONE or FAILED."
    })
    public String issueDeviceCommand(String deviceCode, String commandType, String payload, @ToolMemoryId String memoryId) {
        long start = System.currentTimeMillis();
        String traceKey = memoryKey(memoryId);
        String args = deviceCode + " | " + commandType + " | " + payload;
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return record(traceKey, "issueDeviceCommand", args, "设备不存在: " + deviceCode, start);
        }

        try {
            DeviceCommandRequest request = new DeviceCommandRequest();
            request.setCommandType(commandType);
            request.setPayload(payload);
            DeviceCommand command = deviceCommandService.createCommand(device, request, "agent");
            syncConfigForCommand(device, command.getCommandType(), command.getPayload());
            String r = "已下发命令 #" + command.getId() + " 到 " + deviceCode
                    + "，类型 " + command.getCommandType()
                    + "，状态 " + command.getStatus()
                    + "。等待 ESP32 轮询执行。";
            return record(traceKey, "issueDeviceCommand", args, r, start);
        } catch (IllegalArgumentException e) {
            return record(traceKey, "issueDeviceCommand", args, e.getMessage(), start);
        }
    }

    @Tool(value = {
            "Compare multiple devices side by side.",
            "Pass deviceCodes as a comma-separated string, e.g. ESP32-001,ESP32-002.",
            "Returns latest temperature/humidity and highlights possible abnormal differences."
    })
    public String compareDevices(String deviceCodes, @ToolMemoryId String memoryId) {
        long start = System.currentTimeMillis();
        String traceKey = memoryKey(memoryId);
        List<String> codes = Arrays.stream(deviceCodes.split("[,，\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(8)
                .toList();
        if (codes.isEmpty()) {
            return record(traceKey, "compareDevices", deviceCodes, "请提供至少一个设备编号", start);
        }

        StringBuilder result = new StringBuilder("多设备对比:\n");
        BigDecimal firstTemp = null;
        String firstCode = null;
        for (String code : codes) {
            Device device = deviceService.getByDeviceCode(code);
            if (device == null) {
                result.append("- ").append(code).append(": 设备不存在\n");
                continue;
            }
            DeviceData latest = deviceDataService.getLatestData(device.getId());
            if (latest == null) {
                result.append("- ").append(code).append(": 暂无遥测\n");
                continue;
            }
            result.append("- ").append(code)
                    .append(" | ").append(device.getLocation())
                    .append(" | 温度 ").append(latest.getTemperature()).append("℃")
                    .append(" | 湿度 ").append(latest.getHumidity()).append("%")
                    .append(" | ").append(Boolean.TRUE.equals(device.getOnline()) ? "在线" : "离线")
                    .append("\n");
            if (firstTemp == null) {
                firstTemp = latest.getTemperature();
                firstCode = code;
            } else {
                BigDecimal diff = latest.getTemperature().subtract(firstTemp).abs();
                if (diff.compareTo(new BigDecimal("5")) >= 0) {
                    result.append("  提醒: ").append(code).append(" 与 ").append(firstCode)
                            .append(" 温差 ").append(diff).append("℃，建议检查散热或安装位置。\n");
                }
            }
        }

        return record(traceKey, "compareDevices", deviceCodes, result.toString(), start);
    }

    @Tool(value = {
            "Schedule periodic inspection for one device by setting inspection interval minutes.",
            "Use when the user asks for regular patrol, proactive checks, or scheduled monitoring."
    })
    public String scheduleCheck(String deviceCode, int intervalMinutes, @ToolMemoryId String memoryId) {
        long start = System.currentTimeMillis();
        String traceKey = memoryKey(memoryId);
        String args = deviceCode + " | intervalMinutes=" + intervalMinutes;
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return record(traceKey, "scheduleCheck", args, "设备不存在: " + deviceCode, start);
        }

        DeviceConfigRequest request = new DeviceConfigRequest();
        request.setInspectionIntervalMinutes(intervalMinutes);
        DeviceConfig config = deviceConfigService.updateConfig(device.getId(), request);
        String r = "已设置 " + deviceCode + " 定时巡检间隔为 "
                + config.getInspectionIntervalMinutes() + " 分钟。";
        return record(traceKey, "scheduleCheck", args, r, start);
    }

    private void syncConfigForCommand(Device device, String commandType, String payload) {
        DeviceConfigRequest configRequest = new DeviceConfigRequest();
        if ("SET_SAMPLE_INTERVAL".equals(commandType)) {
            try {
                configRequest.setSampleIntervalSeconds(Integer.parseInt(payload.trim()));
                deviceConfigService.updateConfig(device.getId(), configRequest);
            } catch (NumberFormatException ignored) {
                // The command can still be queued; ESP32 will report failure if payload is invalid.
            }
        }
        if ("SET_DISPLAY_MODE".equals(commandType) && payload != null && !payload.isBlank()) {
            configRequest.setDisplayMode(payload);
            deviceConfigService.updateConfig(device.getId(), configRequest);
        }
    }

    private String record(String memoryId, String toolName, String args, String result, long start) {
        collector.record(memoryId, toolName, args, result, System.currentTimeMillis() - start);
        return result;
    }

    private String memoryKey(String memoryId) {
        return memoryId == null || memoryId.isBlank() ? "default" : memoryId;
    }

    private String valueOrDash(Object value) {
        return value == null ? "--" : String.valueOf(value);
    }
}
