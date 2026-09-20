package com.opspilot.service;

import com.opspilot.entity.Alarm;
import com.opspilot.entity.Device;
import com.opspilot.entity.DeviceCommand;
import com.opspilot.entity.DeviceCommandRequest;
import com.opspilot.entity.DeviceConfig;
import com.opspilot.entity.DeviceData;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DiagnosticTools {

    private final DeviceService deviceService;
    private final DeviceDataService deviceDataService;
    private final AlarmService alarmService;
    private final DeviceConfigService deviceConfigService;
    private final DeviceCommandService deviceCommandService;
    private final ToolTraceCollector collector;

    public DiagnosticTools(DeviceService deviceService,
                           DeviceDataService deviceDataService,
                           AlarmService alarmService,
                           DeviceConfigService deviceConfigService,
                           DeviceCommandService deviceCommandService,
                           ToolTraceCollector collector) {
        this.deviceService = deviceService;
        this.deviceDataService = deviceDataService;
        this.alarmService = alarmService;
        this.deviceConfigService = deviceConfigService;
        this.deviceCommandService = deviceCommandService;
        this.collector = collector;
    }

    @Tool(value = {
            "Run a complete diagnostic report for one device.",
            "Steps: query status, threshold config, recent trend, alarm history, and generate actionable recommendations.",
            "When high risk is detected, queue a DISPLAY_MESSAGE command so the ESP32 screen can show the maintenance notice.",
            "IMPORTANT: pass the exact deviceCode (e.g. ESP32-001), do not guess."
    })
    public String runDiagnostic(String deviceCode, @ToolMemoryId String memoryId) {
        long start = System.currentTimeMillis();
        String traceKey = memoryId == null || memoryId.isBlank() ? "default" : memoryId;
        StringBuilder report = new StringBuilder();
        report.append("========== 设备排障报告 ==========\n");

        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return record(traceKey, deviceCode, "设备不存在：" + deviceCode, start);
        }

        DeviceConfig config = deviceConfigService.getOrCreateByDeviceId(device.getId());
        DeviceData latest = deviceDataService.getLatestData(device.getId());
        List<DeviceData> recent = deviceDataService.listRecentByDeviceId(device.getId(), 6);

        report.append("【步骤1】设备状态\n");
        report.append("设备名：").append(device.getName()).append("\n");
        report.append("位置：").append(device.getLocation()).append("\n");
        report.append("在线状态：").append(Boolean.TRUE.equals(device.getOnline()) ? "在线" : "离线").append("\n");
        report.append("采样间隔：").append(config.getSampleIntervalSeconds()).append(" 秒\n");
        report.append("高温阈值：").append(config.getTemperatureThreshold()).append("℃\n");
        report.append("湿度阈值：").append(config.getHumidityMinThreshold())
                .append("%-").append(config.getHumidityMaxThreshold()).append("%\n");

        if (latest != null) {
            report.append("最新温度：").append(latest.getTemperature()).append("℃\n");
            report.append("最新湿度：").append(latest.getHumidity()).append("%\n");
            report.append("WiFi RSSI：").append(latest.getRssi() == null ? "--" : latest.getRssi()).append("dBm\n");
            report.append("固件版本：").append(latest.getFirmwareVersion() == null ? "--" : latest.getFirmwareVersion()).append("\n");
        } else {
            report.append("无遥测数据\n");
        }

        report.append("\n【步骤2】最近趋势\n");
        String trend = buildTrend(recent);
        report.append(trend).append("\n");

        report.append("\n【步骤3】告警历史\n");
        List<Alarm> alarms = alarmService.listByDeviceId(device.getId());
        if (alarms.isEmpty()) {
            report.append("无告警记录\n");
        } else {
            int unresolved = 0;
            for (Alarm a : alarms) {
                report.append("- [").append(a.getStatus()).append("] ")
                        .append(a.getAlarmType()).append(" ").append(a.getAlarmValue())
                        .append(" (").append(a.getCreateTime()).append(")\n");
                if ("未处理".equals(a.getStatus()) || "反复触发".equals(a.getStatus())) {
                    unresolved++;
                }
            }
            report.append("共 ").append(alarms.size()).append(" 条告警，")
                    .append(unresolved).append(" 条待处理\n");
        }

        report.append("\n【步骤4】排查建议与执行动作\n");
        boolean highTemperature = latest != null
                && latest.getTemperature() != null
                && latest.getTemperature().compareTo(config.getTemperatureThreshold()) > 0;
        if (highTemperature) {
            report.append("当前温度超过动态阈值，建议立即检查机柜散热、空调出风、传感器安装位置和风道阻塞。\n");
            DeviceCommandRequest commandRequest = new DeviceCommandRequest();
            commandRequest.setCommandType("DISPLAY_MESSAGE");
            commandRequest.setPayload("高温巡检: " + deviceCode + " " + latest.getTemperature() + "C");
            DeviceCommand command = deviceCommandService.createCommand(device, commandRequest, "agent");
            report.append("已下发屏幕提示命令 #").append(command.getId())
                    .append("，等待 ESP32 轮询执行。\n");
        } else if (latest == null) {
            report.append("设备缺少遥测数据，建议检查 WiFi、供电和上报接口 Token。\n");
        } else {
            report.append("设备运行指标未超过阈值，建议按 ").append(config.getInspectionIntervalMinutes())
                    .append(" 分钟巡检间隔继续观察。\n");
        }

        return record(traceKey, deviceCode, report.toString(), start);
    }

    private String buildTrend(List<DeviceData> recent) {
        if (recent == null || recent.isEmpty()) {
            return "最近 6 小时暂无可回放遥测。";
        }
        if (recent.size() == 1) {
            DeviceData only = recent.get(0);
            return "最近 6 小时仅 1 条样本，温度 " + only.getTemperature() + "℃，趋势证据不足。";
        }

        DeviceData latest = recent.get(0);
        DeviceData oldest = recent.get(recent.size() - 1);
        BigDecimal delta = latest.getTemperature().subtract(oldest.getTemperature());
        String direction = delta.compareTo(new BigDecimal("0.5")) > 0
                ? "持续上升"
                : delta.compareTo(new BigDecimal("-0.5")) < 0 ? "持续下降" : "基本稳定";
        return "最近 6 小时样本 " + recent.size()
                + " 条，温度从 " + oldest.getTemperature()
                + "℃ 到 " + latest.getTemperature()
                + "℃，趋势：" + direction + "。";
    }

    private String record(String memoryId, String args, String result, long start) {
        collector.record(memoryId, "runDiagnostic", args, result, System.currentTimeMillis() - start);
        return result;
    }
}
