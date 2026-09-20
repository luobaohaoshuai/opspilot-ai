ALTER TABLE device
    ADD CONSTRAINT uk_device_code UNIQUE (device_code);

ALTER TABLE `user`
    ADD CONSTRAINT uk_user_username UNIQUE (username);

ALTER TABLE device_config
    ADD CONSTRAINT uk_device_config_device UNIQUE (device_id);

CREATE INDEX idx_device_data_device_time
    ON device_data (device_id, report_time);

CREATE INDEX idx_alarm_device_time
    ON alarm (device_id, create_time);

CREATE INDEX idx_alarm_device_type_time
    ON alarm (device_id, alarm_type, create_time);

CREATE INDEX idx_command_device_status_time
    ON device_command (device_id, status, create_time);

CREATE INDEX idx_knowledge_document_chunk
    ON knowledge_chunk (document_name, chunk_index);
