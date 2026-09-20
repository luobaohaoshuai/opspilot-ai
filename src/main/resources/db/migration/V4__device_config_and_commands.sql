ALTER TABLE device
    ADD COLUMN device_token VARCHAR(100) COMMENT '设备侧访问令牌';

ALTER TABLE device_data
    ADD COLUMN rssi INT COMMENT 'WiFi信号强度',
    ADD COLUMN uptime_seconds BIGINT COMMENT '设备运行秒数',
    ADD COLUMN firmware_version VARCHAR(50) COMMENT '固件版本';

CREATE TABLE device_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL COMMENT '所属设备ID',
    temperature_threshold DECIMAL(5,2) DEFAULT 35.00 COMMENT '高温告警阈值',
    humidity_min_threshold DECIMAL(5,2) DEFAULT 20.00 COMMENT '湿度下限',
    humidity_max_threshold DECIMAL(5,2) DEFAULT 80.00 COMMENT '湿度上限',
    sample_interval_seconds INT DEFAULT 10 COMMENT '采样间隔秒',
    inspection_interval_minutes INT DEFAULT 30 COMMENT '巡检间隔分钟',
    display_mode VARCHAR(30) DEFAULT 'NORMAL' COMMENT '屏幕显示模式',
    update_time DATETIME COMMENT '更新时间'
) COMMENT '设备运行配置表';

CREATE TABLE device_command (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL COMMENT '所属设备ID',
    command_type VARCHAR(50) NOT NULL COMMENT '命令类型',
    payload VARCHAR(500) COMMENT '命令参数',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '命令状态',
    issued_by VARCHAR(100) COMMENT '下发人或Agent',
    result_message VARCHAR(500) COMMENT '执行结果',
    create_time DATETIME COMMENT '创建时间',
    sent_time DATETIME COMMENT '下发时间',
    done_time DATETIME COMMENT '完成时间'
) COMMENT '设备命令队列表';

UPDATE device
SET device_token = 'demo-device-token'
WHERE device_code = 'ESP32-001';

INSERT INTO device_config (
    device_id,
    temperature_threshold,
    humidity_min_threshold,
    humidity_max_threshold,
    sample_interval_seconds,
    inspection_interval_minutes,
    display_mode,
    update_time
)
SELECT id, 35.00, 20.00, 80.00, 10, 30, 'NORMAL', NOW()
FROM device;
