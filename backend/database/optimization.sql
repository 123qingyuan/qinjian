-- 老人监护管理系统数据库优化脚本
-- 作者: System
-- 版本: 1.0.0

-- 1. 索引优化
-- 为健康监控数据表创建复合索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_health_data_device_time 
ON health_monitoring_data (device_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_health_data_user_time 
ON health_monitoring_data (user_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_health_data_type_time 
ON health_monitoring_data (data_type, created_at DESC);

-- 为预警记录表创建索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_alert_records_user_time 
ON alert_records (user_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_alert_records_device_time 
ON alert_records (device_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_alert_records_status_time 
ON alert_records (status, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_alert_records_severity_time 
ON alert_records (severity, created_at DESC);

-- 为历史数据表创建索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_history_data_user_time 
ON historical_data (user_id, data_date DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_history_data_type_time 
ON historical_data (data_type, data_date DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_history_data_device_time 
ON historical_data (device_id, data_date DESC);

-- 为通知记录表创建索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_recipient_time 
ON notification_records (recipient_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_status_time 
ON notification_records (status, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_type_time 
ON notification_records (notification_type, created_at DESC);

-- 2. 分区表优化
-- 为健康监控数据表创建按月分区
DO $$
DECLARE
    start_date DATE := DATE_TRUNC('month', CURRENT_DATE - INTERVAL '6 months');
    end_date DATE := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '6 months');
    current_date DATE := start_date;
BEGIN
    WHILE current_date < end_date LOOP
        EXECUTE format('CREATE TABLE IF NOT EXISTS health_monitoring_data_%s PARTITION OF health_monitoring_data 
                        FOR VALUES FROM (%L) TO (%L)',
                       TO_CHAR(current_date, 'YYYY_MM'),
                       current_date,
                       current_date + INTERVAL '1 month');
        current_date := current_date + INTERVAL '1 month';
    END LOOP;
END $$;

-- 为预警记录表创建按月分区
DO $$
DECLARE
    start_date DATE := DATE_TRUNC('month', CURRENT_DATE - INTERVAL '6 months');
    end_date DATE := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '6 months');
    current_date DATE := start_date;
BEGIN
    WHILE current_date < end_date LOOP
        EXECUTE format('CREATE TABLE IF NOT EXISTS alert_records_%s PARTITION OF alert_records 
                        FOR VALUES FROM (%L) TO (%L)',
                       TO_CHAR(current_date, 'YYYY_MM'),
                       current_date,
                       current_date + INTERVAL '1 month');
        current_date := current_date + INTERVAL '1 month';
    END LOOP;
END $$;

-- 为历史数据表创建按月分区
DO $$
DECLARE
    start_date DATE := DATE_TRUNC('month', CURRENT_DATE - INTERVAL '12 months');
    end_date DATE := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '12 months');
    current_date DATE := start_date;
BEGIN
    WHILE current_date < end_date LOOP
        EXECUTE format('CREATE TABLE IF NOT EXISTS historical_data_%s PARTITION OF historical_data 
                        FOR VALUES FROM (%L) TO (%L)',
                       TO_CHAR(current_date, 'YYYY_MM'),
                       current_date,
                       current_date + INTERVAL '1 month');
        current_date := current_date + INTERVAL '1 month';
    END LOOP;
END $$;

-- 3. 视图优化
-- 创建用户健康统计视图
CREATE OR REPLACE VIEW user_health_stats AS
SELECT 
    u.id as user_id,
    u.username,
    u.full_name,
    COUNT(hmd.id) as total_readings,
    AVG(CASE WHEN hmd.data_type = 'HEART_RATE' THEN hmd.numeric_value::numeric END) as avg_heart_rate,
    AVG(CASE WHEN hmd.data_type = 'BLOOD_PRESSURE_SYSTOLIC' THEN hmd.numeric_value::numeric END) as avg_systolic,
    AVG(CASE WHEN hmd.data_type = 'BLOOD_PRESSURE_DIASTOLIC' THEN hmd.numeric_value::numeric END) as avg_diastolic,
    AVG(CASE WHEN hmd.data_type = 'TEMPERATURE' THEN hmd.numeric_value::numeric END) as avg_temperature,
    MAX(hmd.created_at) as last_reading_time,
    COUNT(CASE WHEN ar.id IS NOT NULL THEN 1 END) as alert_count
FROM users u
LEFT JOIN health_monitoring_data hmd ON u.id = hmd.user_id
LEFT JOIN alert_records ar ON u.id = ar.user_id AND ar.created_at >= CURRENT_DATE - INTERVAL '30 days'
WHERE hmd.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY u.id, u.username, u.full_name;

-- 创建设备状态统计视图
CREATE OR REPLACE VIEW device_status_stats AS
SELECT 
    d.id as device_id,
    d.device_name,
    d.device_type,
    d.status,
    d.location,
    COUNT(hmd.id) as total_readings,
    MAX(hmd.created_at) as last_reading_time,
    COUNT(CASE WHEN hmd.created_at >= CURRENT_TIMESTAMP - INTERVAL '1 hour' THEN 1 END) as readings_last_hour,
    CASE 
        WHEN MAX(hmd.created_at) >= CURRENT_TIMESTAMP - INTERVAL '5 minutes' THEN 'ONLINE'
        WHEN MAX(hmd.created_at) >= CURRENT_TIMESTAMP - INTERVAL '1 hour' THEN 'WARNING'
        ELSE 'OFFLINE'
    END as connection_status
FROM devices d
LEFT JOIN health_monitoring_data hmd ON d.id = hmd.device_id
GROUP BY d.id, d.device_name, d.device_type, d.status, d.location;

-- 4. 存储过程优化
-- 创建批量插入健康数据的存储过程
CREATE OR REPLACE FUNCTION batch_insert_health_data(
    p_device_id BIGINT,
    p_user_id BIGINT,
    p_data_type VARCHAR(50),
    p_numeric_value DECIMAL(10,2),
    p_unit VARCHAR(20),
    p_text_value TEXT,
    p_timestamp TIMESTAMP
) RETURNS VOID AS $$
BEGIN
    INSERT INTO health_monitoring_data (
        device_id, user_id, data_type, numeric_value, unit, text_value, created_at
    ) VALUES (
        p_device_id, p_user_id, p_data_type, p_numeric_value, p_unit, p_text_value, p_timestamp
    );
    
    -- 检查是否需要触发预警
    PERFORM check_and_create_alerts(p_user_id, p_device_id, p_data_type, p_numeric_value);
END;
$$ LANGUAGE plpgsql;

-- 创建预警检查存储过程
CREATE OR REPLACE FUNCTION check_and_create_alerts(
    p_user_id BIGINT,
    p_device_id BIGINT,
    p_data_type VARCHAR(50),
    p_value DECIMAL(10,2)
) RETURNS VOID AS $$
DECLARE
    alert_rule RECORD;
    alert_id BIGINT;
BEGIN
    -- 获取匹配的预警规则
    FOR alert_rule IN 
        SELECT * FROM alert_rules 
        WHERE data_type = p_data_type 
        AND is_active = true
    LOOP
        -- 简单的数值比较逻辑（实际应用中需要更复杂的规则解析）
        IF alert_rule.rule_condition LIKE '%>%' AND p_value > REPLACE(alert_rule.rule_condition, '>', '')::DECIMAL THEN
            INSERT INTO alert_records (
                user_id, device_id, alert_type, severity, title, description, rule_id
            ) VALUES (
                p_user_id, p_device_id, 'HEALTH_ALERT', alert_rule.severity, 
                alert_rule.rule_name || '触发', 
                '检测到' || p_data_type || '值异常: ' || p_value,
                alert_rule.id
            ) RETURNING id INTO alert_id;
            
            -- 创建通知
            PERFORM create_notification_for_alert(alert_id);
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 创建通知存储过程
CREATE OR REPLACE FUNCTION create_notification_for_alert(
    p_alert_id BIGINT
) RETURNS VOID AS $$
DECLARE
    alert_record RECORD;
    notification_id BIGINT;
BEGIN
    -- 获取预警记录
    SELECT * INTO alert_record FROM alert_records WHERE id = p_alert_id;
    
    IF FOUND THEN
        -- 创建通知记录
        INSERT INTO notification_records (
            recipient_id, notification_type, title, content, status, send_method
        ) VALUES (
            alert_record.user_id, 'HEALTH_ALERT', 
            alert_record.title, alert_record.description, 'PENDING', 'SYSTEM'
        ) RETURNING id INTO notification_id;
        
        -- 异步发送通知（实际应用中可以使用消息队列）
        PERFORM send_notification_async(notification_id);
    END IF;
END;
$$ LANGUAGE plpgsql;

-- 创建异步发送通知存储过程
CREATE OR REPLACE FUNCTION send_notification_async(
    p_notification_id BIGINT
) RETURNS VOID AS $$
BEGIN
    -- 这里可以实现实际的发送逻辑
    -- 例如：邮件、短信、推送等
    UPDATE notification_records 
    SET status = 'SENT', sent_at = CURRENT_TIMESTAMP 
    WHERE id = p_notification_id;
END;
$$ LANGUAGE plpgsql;

-- 5. 触发器优化
-- 创建自动更新时间戳的触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为需要的表应用触发器
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_devices_updated_at
    BEFORE UPDATE ON devices
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alert_rules_updated_at
    BEFORE UPDATE ON alert_rules
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 6. 数据清理策略
-- 创建数据清理存储过程
CREATE OR REPLACE FUNCTION cleanup_old_data()
RETURNS VOID AS $$
BEGIN
    -- 清理6个月前的健康监控数据
    DELETE FROM health_monitoring_data 
    WHERE created_at < CURRENT_DATE - INTERVAL '6 months';
    
    -- 清理1年前的已解决预警记录
    DELETE FROM alert_records 
    WHERE created_at < CURRENT_DATE - INTERVAL '1 year' 
    AND status IN ('RESOLVED', 'CLOSED');
    
    -- 清理3个月前的已发送通知记录
    DELETE FROM notification_records 
    WHERE created_at < CURRENT_DATE - INTERVAL '3 months' 
    AND status = 'SENT';
    
    -- 更新统计信息
    ANALYZE;
END;
$$ LANGUAGE plpgsql;

-- 创建定期清理任务（需要pg_cron扩展）
-- SELECT cron.schedule('cleanup-old-data', '0 2 * * 0', 'SELECT cleanup_old_data();');

-- 7. 性能监控视图
-- 创建慢查询监控视图
CREATE OR REPLACE VIEW slow_queries AS
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    rows,
    100.0 * shared_blks_hit / nullif(shared_blks_hit + shared_blks_read, 0) AS hit_percent
FROM pg_stat_statements
WHERE mean_time > 1000  -- 超过1秒的查询
ORDER BY mean_time DESC;

-- 创建表大小监控视图
CREATE OR REPLACE VIEW table_sizes AS
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    pg_total_relation_size(schemaname||'.'||tablename) AS size_bytes
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- 8. 配置优化建议
-- 显示当前数据库配置
SELECT name, setting, unit, short_desc
FROM pg_settings
WHERE name IN ('shared_buffers', 'effective_cache_size', 'work_mem', 'maintenance_work_mem', 
               'checkpoint_completion_target', 'wal_buffers', 'default_statistics_target',
               'random_page_cost', 'effective_io_concurrency')
ORDER BY name;

-- 分析表统计信息
ANALYZE;

-- 重新索引（如果需要）
-- REINDEX DATABASE elderly_monitoring;

COMMIT;