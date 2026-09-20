CREATE TABLE employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    age INT NOT NULL COMMENT '年龄',
    department VARCHAR(50) COMMENT '部门'
) COMMENT '员工表';

CREATE TABLE device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code VARCHAR(50) NOT NULL COMMENT '设备编号',
    name VARCHAR(100) COMMENT '设备名称',
    type VARCHAR(50) COMMENT '设备类型',
    location VARCHAR(100) COMMENT '安装位置',
    online TINYINT(1) DEFAULT 0 COMMENT '在线状态',
    last_online_time DATETIME COMMENT '最近上线时间'
) COMMENT '设备表';

CREATE TABLE device_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT COMMENT '所属设备ID',
    temperature DECIMAL(5,2) COMMENT '温度',
    humidity DECIMAL(5,2) COMMENT '湿度',
    report_time DATETIME COMMENT '上报时间'
) COMMENT '设备数据表';

CREATE TABLE alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT COMMENT '所属设备ID',
    alarm_type VARCHAR(50) COMMENT '告警类型',
    alarm_value VARCHAR(50) COMMENT '告警值',
    status VARCHAR(20) DEFAULT '未处理' COMMENT '处理状态',
    create_time DATETIME COMMENT '告警时间'
) COMMENT '告警表';

CREATE TABLE knowledge_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_name VARCHAR(200),
    chunk_index INT,
    content TEXT,
    vector_json MEDIUMTEXT
);
