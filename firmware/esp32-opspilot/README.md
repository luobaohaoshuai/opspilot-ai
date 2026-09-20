# ESP32 OpsPilot Firmware

Arduino 示例固件，默认硬件：

- ESP32
- DHT22 温湿度传感器，数据引脚接 GPIO4
- SSD1306 OLED I2C 屏幕，地址 `0x3C`

## 依赖库

在 Arduino IDE Library Manager 安装：

- DHT sensor library
- Adafruit Unified Sensor
- Adafruit SSD1306
- Adafruit GFX Library
- ArduinoJson

## 配置

打开 `esp32-opspilot.ino`，修改：

```cpp
const char* WIFI_SSID = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
const char* API_BASE = "http://YOUR_SERVER_IP:8080";
const char* DEVICE_CODE = "ESP32-001";
const char* DEVICE_TOKEN = "demo-device-token";
```

`DEVICE_TOKEN` 使用设备注册时只返回一次的原始 Token。后端数据库仅保存 SHA-256 摘要，不会保存或通过设备列表返回原始 Token。默认演示设备 `ESP32-001` 的原始 token 是 `demo-device-token`。

## 通信链路

- `POST /device/report`：上报温湿度、RSSI、运行时长、固件版本。
- `GET /device/{deviceCode}/commands/next`：轮询下一条待执行命令。
- `POST /device/{deviceCode}/commands/{commandId}/ack`：回传命令执行结果。

所有设备侧接口都需要请求头：

```text
X-Device-Token: demo-device-token
```

## 支持命令

- `DISPLAY_MESSAGE`：OLED 显示提示文字。
- `SET_DISPLAY_MODE`：切换 `NORMAL`、`ALERT`、`MAINTENANCE`。
- `SET_SAMPLE_INTERVAL`：修改上报间隔，单位秒。
- `RUN_SELF_TEST`：检查传感器、WiFi 和屏幕。
- `REBOOT`：重启 ESP32。
