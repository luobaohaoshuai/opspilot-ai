

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <DHT.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#define DHT_PIN 4
#define DHT_TYPE DHT22
#define RELAY_PIN 26
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1

const char* WIFI_SSID = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
const char* API_BASE = "http://YOUR_SERVER_IP:8080";
const char* DEVICE_CODE = "ESP32-001";
const char* DEVICE_TOKEN = "demo-device-token";
const char* FIRMWARE_VERSION = "1.0.0";

DHT dht(DHT_PIN, DHT_TYPE);
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

unsigned long sampleIntervalMs = 10000;
unsigned long lastReportAt = 0;
unsigned long lastCommandPollAt = 0;
String displayMode = "NORMAL";
String lastMessage = "OpsPilot ready";

void setup() {
  Serial.begin(115200);
  dht.begin();
  Wire.begin();
  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, LOW);

  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println("SSD1306 init failed");
  }
  renderStatus("Booting...");
  connectWiFi();
}

void loop() {
  ensureWiFi();

  unsigned long now = millis();
  if (now - lastReportAt >= sampleIntervalMs) {
    lastReportAt = now;
    reportTelemetry();
  }

  if (now - lastCommandPollAt >= 2000) {
    lastCommandPollAt = now;
    pollCommand();
  }
}

void connectWiFi() {
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  renderStatus("WiFi connecting");

  int retry = 0;
  while (WiFi.status() != WL_CONNECTED && retry < 30) {
    delay(500);
    Serial.print(".");
    retry++;
  }

  renderStatus(WiFi.status() == WL_CONNECTED ? "WiFi connected" : "WiFi failed");
}

void ensureWiFi() {
  if (WiFi.status() == WL_CONNECTED) return;
  WiFi.disconnect();
  connectWiFi();
}

void reportTelemetry() {
  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();

  if (isnan(temperature) || isnan(humidity)) {
    renderStatus("DHT read failed");
    return;
  }

  StaticJsonDocument<256> body;
  body["deviceCode"] = DEVICE_CODE;
  body["temperature"] = temperature;
  body["humidity"] = humidity;
  body["rssi"] = WiFi.RSSI();
  body["uptimeSeconds"] = millis() / 1000;
  body["firmwareVersion"] = FIRMWARE_VERSION;

  String json;
  serializeJson(body, json);

  HTTPClient http;
  http.begin(String(API_BASE) + "/device/report");
  http.addHeader("Content-Type", "application/json");
  http.addHeader("X-Device-Token", DEVICE_TOKEN);
  int status = http.POST(json);
  String response = http.getString();
  http.end();

  Serial.printf("report status=%d response=%s\n", status, response.c_str());
  renderTelemetry(temperature, humidity);
}

void pollCommand() {
  HTTPClient http;
  http.begin(String(API_BASE) + "/device/" + DEVICE_CODE + "/commands/next");
  http.addHeader("X-Device-Token", DEVICE_TOKEN);
  int status = http.GET();
  String response = http.getString();
  http.end();

  if (status != 200 || response.length() == 0) return;

  StaticJsonDocument<1024> doc;
  DeserializationError error = deserializeJson(doc, response);
  if (error) {
    Serial.printf("command json parse failed: %s\n", error.c_str());
    return;
  }

  JsonVariant data = doc["data"];
  if (data.isNull()) return;

  long commandId = data["id"] | 0;
  String commandType = data["commandType"] | "";
  String payload = data["payload"] | "";
  if (commandId == 0 || commandType.length() == 0) return;

  String resultMessage = executeCommand(commandId, commandType, payload);
  bool success = !resultMessage.startsWith("FAILED:");
  ackCommand(commandId, success, resultMessage);
}

String executeCommand(long commandId, String commandType, String payload) {
  commandType.toUpperCase();

  if (commandType == "DISPLAY_MESSAGE") {
    lastMessage = payload.length() ? payload : "OpsPilot command";
    renderStatus(lastMessage);
    return "屏幕消息已显示";
  }

  if (commandType == "SET_DISPLAY_MODE") {
    payload.toUpperCase();
    displayMode = payload.length() ? payload : "NORMAL";
    renderStatus("Mode: " + displayMode);
    return "屏幕模式已切换为 " + displayMode;
  }

  if (commandType == "SET_SAMPLE_INTERVAL") {
    int seconds = payload.toInt();
    if (seconds < 5 || seconds > 3600) {
      return "FAILED: 采样间隔必须在 5-3600 秒之间";
    }
    sampleIntervalMs = (unsigned long)seconds * 1000;
    renderStatus("Sample: " + String(seconds) + "s");
    return "采样间隔已更新为 " + String(seconds) + " 秒";
  }

  if (commandType == "RUN_SELF_TEST") {
    float temperature = dht.readTemperature();
    bool sensorOk = !isnan(temperature);
    bool wifiOk = WiFi.status() == WL_CONNECTED;
    bool screenOk = true;
    String result = String("自检: sensor=") + (sensorOk ? "OK" : "FAIL")
        + ", wifi=" + (wifiOk ? "OK" : "FAIL")
        + ", screen=" + (screenOk ? "OK" : "FAIL")
        + ", rssi=" + String(WiFi.RSSI());
    renderStatus(result);
    return sensorOk && wifiOk && screenOk ? result : "FAILED: " + result;
  }

  if (commandType == "RELAY_ON") {
    digitalWrite(RELAY_PIN, HIGH);
    Serial.println("relay on");
    return "RELAY 已开启";
  }

  if (commandType == "RELAY_OFF") {
    digitalWrite(RELAY_PIN, LOW);
    Serial.println("relay off");
    return "RELAY 已关闭";
  }

  if (commandType == "RELAY_PULSE") {
    digitalWrite(RELAY_PIN, HIGH);
    delay(500);
    digitalWrite(RELAY_PIN, LOW);
    Serial.println("relay pulse 500ms");
    return "RELAY 脉冲已发送";
  }

  if (commandType == "REBOOT") {
    renderStatus("Rebooting...");
    ackCommand(commandId, true, "设备准备重启");
    delay(800);
    ESP.restart();
    return "设备准备重启";
  }

  return "FAILED: 未知命令 " + commandType;
}

void ackCommand(long commandId, bool success, String resultMessage) {
  if (commandId == 0) return;

  StaticJsonDocument<256> body;
  body["success"] = success;
  body["resultMessage"] = resultMessage;

  String json;
  serializeJson(body, json);

  HTTPClient http;
  http.begin(String(API_BASE) + "/device/" + DEVICE_CODE + "/commands/" + String(commandId) + "/ack");
  http.addHeader("Content-Type", "application/json");
  http.addHeader("X-Device-Token", DEVICE_TOKEN);
  int status = http.POST(json);
  String response = http.getString();
  http.end();

  Serial.printf("ack command=%ld status=%d response=%s\n", commandId, status, response.c_str());
}

void renderTelemetry(float temperature, float humidity) {
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println(DEVICE_CODE);
  display.println("Mode: " + displayMode);
  display.printf("Temp: %.1f C\n", temperature);
  display.printf("Hum : %.1f %%\n", humidity);
  display.printf("RSSI: %d dBm\n", WiFi.RSSI());
  display.println(lastMessage);
  display.display();
}

void renderStatus(String message) {
  lastMessage = message;
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println(DEVICE_CODE);
  display.println(displayMode);
  display.println(message);
  display.display();
  Serial.println(message);
}
