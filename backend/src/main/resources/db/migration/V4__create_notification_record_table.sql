-- 创建通知记录表
CREATE TABLE lf_notification_record (
    id BIGSERIAL PRIMARY KEY,
    notification_id VARCHAR(64) UNIQUE NOT NULL,
    type VARCHAR(32) NOT NULL,
    recipient_id VARCHAR(64) NOT NULL,
    recipient_name VARCHAR(128),
    title VARCHAR(256) NOT NULL,
    content TEXT,
    process_instance_id VARCHAR(64),
    process_definition_id VARCHAR(64),
    process_definition_key VARCHAR(128),
    process_name VARCHAR(256),
    task_id VARCHAR(64),
    task_name VARCHAR(256),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    extra_data TEXT
);

-- 创建索引
CREATE INDEX idx_notification_recipient ON lf_notification_record(recipient_id);
CREATE INDEX idx_notification_process_instance ON lf_notification_record(process_instance_id);
CREATE INDEX idx_notification_task ON lf_notification_record(task_id);
CREATE INDEX idx_notification_type ON lf_notification_record(type);
CREATE INDEX idx_notification_read ON lf_notification_record(is_read);
CREATE INDEX idx_notification_create_time ON lf_notification_record(create_time);

-- 添加注释
COMMENT ON TABLE lf_notification_record IS '流程通知记录表';
COMMENT ON COLUMN lf_notification_record.notification_id IS '通知UUID';
COMMENT ON COLUMN lf_notification_record.type IS '通知类型：TASK_ASSIGNED, TASK_COMPLETED, TASK_DELEGATED, PROCESS_TIMEOUT, PROCESS_APPROVED, PROCESS_REJECTED, PROCESS_WITHDRAWN, CUSTOM';
COMMENT ON COLUMN lf_notification_record.recipient_id IS '接收人ID';
COMMENT ON COLUMN lf_notification_record.recipient_name IS '接收人名称';
COMMENT ON COLUMN lf_notification_record.is_read IS '是否已读';
COMMENT ON COLUMN lf_notification_record.read_time IS '读取时间';
COMMENT ON COLUMN lf_notification_record.extra_data IS '扩展数据（JSON格式）';
