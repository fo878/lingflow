-- 流程定义快照表
CREATE TABLE IF NOT EXISTS process_snapshot (
    id VARCHAR(36) PRIMARY KEY,
    process_definition_key VARCHAR(255) NOT NULL,
    snapshot_name VARCHAR(255) NOT NULL,
    snapshot_version INT NOT NULL DEFAULT 1,
    bpmn_xml TEXT NOT NULL,
    description TEXT,
    creator VARCHAR(255),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_process_key ON process_snapshot(process_definition_key);
CREATE INDEX IF NOT EXISTS idx_created_time ON process_snapshot(created_time);

-- 添加表注释
COMMENT ON TABLE process_snapshot IS '流程定义快照表';
COMMENT ON COLUMN process_snapshot.id IS '主键ID (UUID)';
COMMENT ON COLUMN process_snapshot.process_definition_key IS '流程定义KEY';
COMMENT ON COLUMN process_snapshot.snapshot_name IS '快照名称';
COMMENT ON COLUMN process_snapshot.snapshot_version IS '快照版本号';
COMMENT ON COLUMN process_snapshot.bpmn_xml IS 'BPMN XML内容';
COMMENT ON COLUMN process_snapshot.description IS '快照描述';
COMMENT ON COLUMN process_snapshot.creator IS '创建人';
COMMENT ON COLUMN process_snapshot.created_time IS '创建时间';
