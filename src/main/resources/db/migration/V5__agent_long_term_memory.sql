CREATE TABLE agent_long_term_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    memory_type VARCHAR(50) NOT NULL DEFAULT 'note',
    content VARCHAR(1000) NOT NULL,
    source VARCHAR(50) NOT NULL DEFAULT 'user_explicit',
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_agent_memory_user_update (username, update_time)
) COMMENT 'Agent user-scoped long-term memory';
