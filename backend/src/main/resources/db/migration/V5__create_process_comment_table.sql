-- 创建流程评论表
CREATE TABLE lf_process_comment (
    id BIGSERIAL PRIMARY KEY,
    comment_id VARCHAR(64) UNIQUE NOT NULL,
    type VARCHAR(16) NOT NULL,
    process_instance_id VARCHAR(64),
    process_definition_id VARCHAR(64),
    task_id VARCHAR(64),
    task_name VARCHAR(256),
    user_id VARCHAR(64),
    user_name VARCHAR(128),
    content TEXT NOT NULL,
    parent_id VARCHAR(64),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delete_time TIMESTAMP,
    extra_data TEXT
);

-- 创建索引
CREATE INDEX idx_comment_process_instance ON lf_process_comment(process_instance_id);
CREATE INDEX idx_comment_task ON lf_process_comment(task_id);
CREATE INDEX idx_comment_user ON lf_process_comment(user_id);
CREATE INDEX idx_comment_type ON lf_process_comment(type);
CREATE INDEX idx_comment_create_time ON lf_process_comment(create_time);
CREATE INDEX idx_comment_parent ON lf_process_comment(parent_id);
CREATE INDEX idx_comment_deleted ON lf_process_comment(is_deleted);

-- 添加注释
COMMENT ON TABLE lf_process_comment IS '流程评论表';
COMMENT ON COLUMN lf_process_comment.comment_id IS '评论UUID';
COMMENT ON COLUMN lf_process_comment.type IS '评论类型：PROCESS(流程评论), TASK(任务评论), SYSTEM(系统评论)';
COMMENT ON COLUMN lf_process_comment.process_instance_id IS '流程实例ID';
COMMENT ON COLUMN lf_process_comment.task_id IS '任务ID';
COMMENT ON COLUMN lf_process_comment.user_id IS '用户ID';
COMMENT ON COLUMN lf_process_comment.user_name IS '用户名称';
COMMENT ON COLUMN lf_process_comment.parent_id IS '父评论ID（用于回复功能）';
COMMENT ON COLUMN lf_process_comment.is_deleted IS '是否已删除（逻辑删除）';
COMMENT ON COLUMN lf_process_comment.delete_time IS '删除时间';
COMMENT ON COLUMN lf_process_comment.extra_data IS '扩展数据（JSON格式）';
