-- 老人监护管理系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS elderly_monitoring;
USE elderly_monitoring;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    avatar_url VARCHAR(500),
    last_login_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- 创建用户表索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- 设备表
CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    model VARCHAR(100),
    manufacturer VARCHAR(100),
    serial_number VARCHAR(100),
    firmware_version VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    location VARCHAR(200),
    user_id BIGINT,
    last_heartbeat_time TIMESTAMP,
    configuration JSONB,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 创建设备表索引
CREATE INDEX IF NOT EXISTS idx_devices_device_id ON devices(device_id);
CREATE INDEX IF NOT EXISTS idx_devices_user_id ON devices(user_id);
CREATE INDEX IF NOT EXISTS idx_devices_type ON devices(type);
CREATE INDEX IF NOT EXISTS idx_devices_status ON devices(status);
CREATE INDEX IF NOT EXISTS idx_devices_created_at ON devices(created_at);

-- 健康数据表
CREATE TABLE IF NOT EXISTS health_data (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20),
    recorded_at TIMESTAMP NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建健康数据表索引
CREATE INDEX IF NOT EXISTS idx_health_data_device_id ON health_data(device_id);
CREATE INDEX IF NOT EXISTS idx_health_data_user_id ON health_data(user_id);
CREATE INDEX IF NOT EXISTS idx_health_data_type ON health_data(data_type);
CREATE INDEX IF NOT EXISTS idx_health_data_recorded_at ON health_data(recorded_at);
CREATE INDEX IF NOT EXISTS idx_health_data_created_at ON health_data(created_at);
CREATE INDEX IF NOT EXISTS idx_health_data_user_type_time ON health_data(user_id, data_type, recorded_at);

-- 预警规则表
CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    user_id BIGINT,
    device_id VARCHAR(100),
    data_type VARCHAR(50) NOT NULL,
    condition_operator VARCHAR(20) NOT NULL,
    threshold_value DECIMAL(10,2) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    enabled BOOLEAN NOT NULL DEFAULT true,
    notification_methods JSONB,
    cooldown_minutes INTEGER DEFAULT 5,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建预警规则表索引
CREATE INDEX IF NOT EXISTS idx_alert_rules_user_id ON alert_rules(user_id);
CREATE INDEX IF NOT EXISTS idx_alert_rules_device_id ON alert_rules(device_id);
CREATE INDEX IF NOT EXISTS idx_alert_rules_data_type ON alert_rules(data_type);
CREATE INDEX IF NOT EXISTS idx_alert_rules_enabled ON alert_rules(enabled);
CREATE INDEX IF NOT EXISTS idx_alert_rules_severity ON alert_rules(severity);
CREATE INDEX IF NOT EXISTS idx_alert_rules_created_at ON alert_rules(created_at);

-- 预警记录表
CREATE TABLE IF NOT EXISTS alert_records (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(100),
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    data_value DECIMAL(10,2),
    threshold_value DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    acknowledged_at TIMESTAMP,
    acknowledged_by BIGINT,
    resolved_at TIMESTAMP,
    resolved_by BIGINT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rule_id) REFERENCES alert_rules(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建预警记录表索引
CREATE INDEX IF NOT EXISTS idx_alert_records_rule_id ON alert_records(rule_id);
CREATE INDEX IF NOT EXISTS idx_alert_records_user_id ON alert_records(user_id);
CREATE INDEX IF NOT EXISTS idx_alert_records_device_id ON alert_records(device_id);
CREATE INDEX IF NOT EXISTS idx_alert_records_type ON alert_records(alert_type);
CREATE INDEX IF NOT EXISTS idx_alert_records_severity ON alert_records(severity);
CREATE INDEX IF NOT EXISTS idx_alert_records_status ON alert_records(status);
CREATE INDEX IF NOT EXISTS idx_alert_records_created_at ON alert_records(created_at);

-- 历史数据表
CREATE TABLE IF NOT EXISTS history_data (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20),
    recorded_at TIMESTAMP NOT NULL,
    aggregated_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建历史数据表索引
CREATE INDEX IF NOT EXISTS idx_history_data_device_id ON history_data(device_id);
CREATE INDEX IF NOT EXISTS idx_history_data_user_id ON history_data(user_id);
CREATE INDEX IF NOT EXISTS idx_history_data_type ON history_data(data_type);
CREATE INDEX IF NOT EXISTS idx_history_data_recorded_at ON history_data(recorded_at);
CREATE INDEX IF NOT EXISTS idx_history_data_created_at ON history_data(created_at);
CREATE INDEX IF NOT EXISTS idx_history_data_user_type_time ON history_data(user_id, data_type, recorded_at);

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    send_method VARCHAR(20),
    related_id VARCHAR(100),
    related_type VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMP,
    sent_at TIMESTAMP,
    expire_at TIMESTAMP,
    retry_count INTEGER DEFAULT 0,
    max_retry_count INTEGER DEFAULT 3,
    error_message TEXT,
    extra_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建通知表索引
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_priority ON notifications(priority);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_read_at ON notifications(read_at);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

-- 通知设置表
CREATE TABLE IF NOT EXISTS notification_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    system_enabled BOOLEAN NOT NULL DEFAULT true,
    email_enabled BOOLEAN DEFAULT false,
    sms_enabled BOOLEAN DEFAULT false,
    push_enabled BOOLEAN DEFAULT false,
    wechat_enabled BOOLEAN DEFAULT false,
    voice_enabled BOOLEAN DEFAULT false,
    email_address VARCHAR(100),
    phone_number VARCHAR(20),
    wechat_openid VARCHAR(100),
    push_token TEXT,
    quiet_hours_start VARCHAR(10),
    quiet_hours_end VARCHAR(10),
    weekend_quiet BOOLEAN DEFAULT false,
    min_priority VARCHAR(20) DEFAULT 'NORMAL',
    batch_interval INTEGER DEFAULT 0,
    max_daily_notifications INTEGER DEFAULT 50,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_notification_type (user_id, type)
);

-- 创建通知设置表索引
CREATE INDEX IF NOT EXISTS idx_notification_settings_user_id ON notification_settings(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_settings_type ON notification_settings(type);
CREATE INDEX IF NOT EXISTS idx_notification_settings_enabled ON notification_settings(enabled);

-- 健康报告表
CREATE TABLE IF NOT EXISTS health_reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_name VARCHAR(200) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    health_score INTEGER CHECK (health_score >= 0 AND health_score <= 100),
    summary TEXT,
    recommendations JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'GENERATING',
    file_url VARCHAR(500),
    generated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建健康报告表索引
CREATE INDEX IF NOT EXISTS idx_health_reports_user_id ON health_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_health_reports_type ON health_reports(report_type);
CREATE INDEX IF NOT EXISTS idx_health_reports_status ON health_reports(status);
CREATE INDEX IF NOT EXISTS idx_health_reports_created_at ON health_reports(created_at);

-- 系统日志表
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGSERIAL PRIMARY KEY,
    level VARCHAR(20) NOT NULL,
    logger VARCHAR(100) NOT NULL,
    message TEXT,
    exception TEXT,
    thread_name VARCHAR(100),
    class_name VARCHAR(200),
    method_name VARCHAR(100),
    line_number INTEGER,
    user_id BIGINT,
    session_id VARCHAR(100),
    request_id VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 创建系统日志表索引
CREATE INDEX IF NOT EXISTS idx_system_logs_level ON system_logs(level);
CREATE INDEX IF NOT EXISTS idx_system_logs_logger ON system_logs(logger);
CREATE INDEX IF NOT EXISTS idx_system_logs_user_id ON system_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_system_logs_session_id ON system_logs(session_id);
CREATE INDEX IF NOT EXISTS idx_system_logs_request_id ON system_logs(request_id);
CREATE INDEX IF NOT EXISTS idx_system_logs_created_at ON system_logs(created_at);

-- 插入默认管理员用户
INSERT INTO users (username, password, email, real_name, role, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iEiJKkT3Zp6sFKJRk3u.3qTjv', 'admin@elderly-monitoring.com', '系统管理员', 'SUPER_ADMIN', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

-- 插入默认通知设置
INSERT INTO notification_settings (user_id, type, enabled, system_enabled, email_enabled, sms_enabled, push_enabled)
SELECT u.id, 'SYSTEM_ALERT', true, true, false, false, false FROM users u WHERE u.username = 'admin'
ON CONFLICT (user_id, type) DO NOTHING;

INSERT INTO notification_settings (user_id, type, enabled, system_enabled, email_enabled, sms_enabled, push_enabled)
SELECT u.id, 'HEALTH_ALERT', true, true, false, false, false FROM users u WHERE u.username = 'admin'
ON CONFLICT (user_id, type) DO NOTHING;

INSERT INTO notification_settings (user_id, type, enabled, system_enabled, email_enabled, sms_enabled, push_enabled)
SELECT u.id, 'DEVICE_ALERT', true, true, false, false, false FROM users u WHERE u.username = 'admin'
ON CONFLICT (user_id, type) DO NOTHING;

-- 创建数据库函数：更新时间戳
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为所有表创建更新时间戳触发器
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_devices_updated_at BEFORE UPDATE ON devices FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_health_data_updated_at BEFORE UPDATE ON health_data FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_alert_rules_updated_at BEFORE UPDATE ON alert_rules FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_alert_records_updated_at BEFORE UPDATE ON alert_records FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_history_data_updated_at BEFORE UPDATE ON history_data FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notification_settings_updated_at BEFORE UPDATE ON notification_settings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_health_reports_updated_at BEFORE UPDATE ON health_reports FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 创建视图：用户设备统计
CREATE OR REPLACE VIEW user_device_stats AS
SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    COUNT(d.id) as device_count,
    COUNT(CASE WHEN d.status = 'ONLINE' THEN 1 END) as online_device_count,
    COUNT(CASE WHEN d.status = 'OFFLINE' THEN 1 END) as offline_device_count,
    MAX(d.last_heartbeat_time) as last_device_heartbeat
FROM users u
LEFT JOIN devices d ON u.id = d.user_id
GROUP BY u.id, u.username, u.real_name;

-- 创建视图：健康数据统计
CREATE OR REPLACE VIEW health_data_stats AS
SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    hd.data_type,
    COUNT(hd.id) as data_count,
    AVG(hd.value) as avg_value,
    MIN(hd.value) as min_value,
    MAX(hd.value) as max_value,
    MAX(hd.recorded_at) as last_record_time
FROM users u
JOIN health_data hd ON u.id = hd.user_id
GROUP BY u.id, u.username, u.real_name, hd.data_type;

-- 创建视图：预警统计
CREATE OR REPLACE VIEW alert_stats AS
SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    COUNT(ar.id) as total_alerts,
    COUNT(CASE WHEN ar.status = 'ACTIVE' THEN 1 END) as active_alerts,
    COUNT(CASE WHEN ar.status = 'ACKNOWLEDGED' THEN 1 END) as acknowledged_alerts,
    COUNT(CASE WHEN ar.status = 'RESOLVED' THEN 1 END) as resolved_alerts,
    COUNT(CASE WHEN ar.severity = 'HIGH' THEN 1 END) as high_severity_alerts,
    COUNT(CASE WHEN ar.severity = 'CRITICAL' THEN 1 END) as critical_alerts,
    MAX(ar.created_at) as last_alert_time
FROM users u
LEFT JOIN alert_records ar ON u.id = ar.user_id
GROUP BY u.id, u.username, u.real_name;

-- 创建分区表：健康数据按月分区
CREATE TABLE IF NOT EXISTS health_data_y2024m01 PARTITION OF health_data
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE IF NOT EXISTS health_data_y2024m02 PARTITION OF health_data
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- 创建分区表：历史数据按月分区
CREATE TABLE IF NOT EXISTS history_data_y2024m01 PARTITION OF history_data
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE IF NOT EXISTS history_data_y2024m02 PARTITION OF history_data
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- 创建分区表：系统日志按月分区
CREATE TABLE IF NOT EXISTS system_logs_y2024m01 PARTITION OF system_logs
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE IF NOT EXISTS system_logs_y2024m02 PARTITION OF system_logs
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- 授权
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- 创建数据库用户和权限（生产环境使用）
-- CREATE USER elderly_app WITH PASSWORD 'your_secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO elderly_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO elderly_app;

-- 创建只读用户（报表查询使用）
-- CREATE USER elderly_readonly WITH PASSWORD 'your_readonly_password';
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO elderly_readonly;