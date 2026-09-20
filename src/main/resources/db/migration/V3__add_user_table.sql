
CREATE TABLE `user` (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) NOT NULL,
                        password VARCHAR(200) NOT NULL,
                        role VARCHAR(20) NOT NULL DEFAULT 'OPERATOR'
);

INSERT INTO `user` (username, password, role) VALUES
    ('admin', '$2b$12$ndqUmMOGZ7rPwFKYSei46OF3V9E90W8GjQS6H/BmwsMpM7QT.AhVq', 'ADMIN');