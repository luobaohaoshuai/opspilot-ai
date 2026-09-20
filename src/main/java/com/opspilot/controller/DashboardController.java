package com.opspilot.controller;

import com.opspilot.common.result.Result;
import com.opspilot.service.AlarmService;
import com.opspilot.service.DeviceService;
import com.opspilot.service.PdfService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DeviceService deviceService;
    private final AlarmService alarmService;
    private final PdfService pdfService;

    public DashboardController(DeviceService deviceService,
                               AlarmService alarmService,
                               PdfService pdfService) {
        this.deviceService = deviceService;
        this.alarmService = alarmService;
        this.pdfService = pdfService;
    }

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalDevices", deviceService.countDevices());
        data.put("onlineDevices", deviceService.countByOnline(true));
        data.put("offlineDevices", deviceService.countByOnline(false));
        data.put("openAlerts", alarmService.countByStatus("未处理"));
        data.put("resolvedAlerts", alarmService.countByStatus("已处理"));
        data.put("knowledgeDocuments", pdfService.countDocuments());
        return Result.ok(data);
    }
}
