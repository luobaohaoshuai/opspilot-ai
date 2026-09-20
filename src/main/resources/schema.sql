-- ========== 建表 ==========

CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    age INT NOT NULL COMMENT '年龄',
    department VARCHAR(50) COMMENT '部门'
) COMMENT '员工表';

CREATE TABLE IF NOT EXISTS device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    name VARCHAR(100) COMMENT '设备名称',
    type VARCHAR(50) COMMENT '设备类型',
    location VARCHAR(100) COMMENT '安装位置',
    online TINYINT(1) DEFAULT 0 COMMENT '在线状态',
    last_online_time DATETIME COMMENT '最近上线时间',
    device_token VARCHAR(100) COMMENT '设备侧访问令牌'
) COMMENT '设备表';

CREATE TABLE IF NOT EXISTS device_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT COMMENT '所属设备ID',
    temperature DECIMAL(5,2) COMMENT '温度',
    humidity DECIMAL(5,2) COMMENT '湿度',
    rssi INT COMMENT 'WiFi信号强度',
    uptime_seconds BIGINT COMMENT '设备运行秒数',
    firmware_version VARCHAR(50) COMMENT '固件版本',
    report_time DATETIME COMMENT '上报时间'
) COMMENT '设备数据表';

CREATE TABLE IF NOT EXISTS device_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL UNIQUE COMMENT '所属设备ID',
    temperature_threshold DECIMAL(5,2) DEFAULT 35.00 COMMENT '高温告警阈值',
    humidity_min_threshold DECIMAL(5,2) DEFAULT 20.00 COMMENT '湿度下限',
    humidity_max_threshold DECIMAL(5,2) DEFAULT 80.00 COMMENT '湿度上限',
    sample_interval_seconds INT DEFAULT 10 COMMENT '采样间隔秒',
    inspection_interval_minutes INT DEFAULT 30 COMMENT '巡检间隔分钟',
    display_mode VARCHAR(30) DEFAULT 'NORMAL' COMMENT '屏幕显示模式',
    update_time DATETIME COMMENT '更新时间'
) COMMENT '设备运行配置表';

CREATE TABLE IF NOT EXISTS device_command (
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

CREATE TABLE IF NOT EXISTS alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT COMMENT '所属设备ID',
    alarm_type VARCHAR(50) COMMENT '告警类型',
    alarm_value VARCHAR(50) COMMENT '告警值',
    status VARCHAR(20) DEFAULT '未处理' COMMENT '处理状态',
    create_time DATETIME COMMENT '告警时间'
) COMMENT '告警表';

CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_name VARCHAR(200),
    chunk_index INT,
    content TEXT,
    vector_json MEDIUMTEXT
    );

-- ========== 初始数据 ==========

INSERT INTO device (device_code, name, type, location, device_token) VALUES
('ESP32-001', '温湿度传感器', '传感器', '3号机房', 'cc93436068ad48868a6a19979fc28cf759fbe5a672ed5b240ef23f74ec7ec9a2');

INSERT INTO device_config (
    device_id,
    temperature_threshold,
    humidity_min_threshold,
    humidity_max_threshold,
    sample_interval_seconds,
    inspection_interval_minutes,
    display_mode,
    update_time
) VALUES
(1, 35.00, 20.00, 80.00, 10, 30, 'NORMAL', CURRENT_TIMESTAMP);

INSERT INTO employee (name, age, department) VALUES
('张三', 20, '技术部'),
('李四', 21, '研发部');

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'OPERATOR'
);

INSERT INTO `user` (username, password, role) VALUES
('admin', '$2b$12$ndqUmMOGZ7rPwFKYSei46OF3V9E90W8GjQS6H/BmwsMpM7QT.AhVq', 'ADMIN');

CREATE TABLE IF NOT EXISTS agent_long_term_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    memory_type VARCHAR(50) NOT NULL DEFAULT 'note',
    content VARCHAR(1000) NOT NULL,
    source VARCHAR(50) NOT NULL DEFAULT 'user_explicit',
    create_time DATETIME,
    update_time DATETIME
);

CREATE INDEX idx_device_data_device_time ON device_data (device_id, report_time);
CREATE INDEX idx_alarm_device_time ON alarm (device_id, create_time);
CREATE INDEX idx_alarm_device_type_time ON alarm (device_id, alarm_type, create_time);
CREATE INDEX idx_command_device_status_time ON device_command (device_id, status, create_time);
CREATE INDEX idx_knowledge_document_chunk ON knowledge_chunk (document_name, chunk_index);
CREATE INDEX idx_agent_memory_user_update ON agent_long_term_memory (username, update_time);
