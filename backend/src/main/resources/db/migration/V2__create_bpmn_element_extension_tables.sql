-- BPMN元素扩展属性表
CREATE TABLE IF NOT EXISTS bpmn_element_extension (
    id BIGSERIAL PRIMARY KEY,
    process_definition_id VARCHAR(255) NOT NULL,
    element_id VARCHAR(255) NOT NULL,
    element_type VARCHAR(100) NOT NULL,
    extension_attributes JSONB,
    version INT DEFAULT 1,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BPMN元素扩展属性历史表
CREATE TABLE IF NOT EXISTS bpmn_element_extension_history (
    id BIGSERIAL PRIMARY KEY,
    extension_id BIGINT NOT NULL,
    process_definition_id VARCHAR(255) NOT NULL,
    element_id VARCHAR(255) NOT NULL,
    element_type VARCHAR(100) NOT NULL,
    extension_attributes JSONB,
    version INT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_process_element ON bpmn_element_extension(process_definition_id, element_id);
CREATE INDEX IF NOT EXISTS idx_element_type ON bpmn_element_extension(element_type);
CREATE INDEX IF NOT EXISTS idx_extension_id ON bpmn_element_extension_history(extension_id);
CREATE INDEX IF NOT EXISTS idx_history_process_element ON bpmn_element_extension_history(process_definition_id, element_id);

-- 添加表注释
COMMENT ON TABLE bpmn_element_extension IS 'BPMN元素扩展属性表';
COMMENT ON COLUMN bpmn_element_extension.process_definition_id IS '流程定义ID';
COMMENT ON COLUMN bpmn_element_extension.element_id IS 'BPMN元素ID';
COMMENT ON COLUMN bpmn_element_extension.element_type IS 'BPMN元素类型';
COMMENT ON COLUMN bpmn_element_extension.extension_attributes IS '扩展属性JSON';
COMMENT ON COLUMN bpmn_element_extension.version IS '版本号';
COMMENT ON COLUMN bpmn_element_extension.created_time IS '创建时间';
COMMENT ON COLUMN bpmn_element_extension.updated_time IS '更新时间';

COMMENT ON TABLE bpmn_element_extension_history IS 'BPMN元素扩展属性历史表';
COMMENT ON COLUMN bpmn_element_extension_history.extension_id IS '扩展属性ID';
COMMENT ON COLUMN bpmn_element_extension_history.process_definition_id IS '流程定义ID';
COMMENT ON COLUMN bpmn_element_extension_history.element_id IS 'BPMN元素ID';
COMMENT ON COLUMN bpmn_element_extension_history.element_type IS 'BPMN元素类型';
COMMENT ON COLUMN bpmn_element_extension_history.extension_attributes IS '扩展属性JSON';
COMMENT ON COLUMN bpmn_element_extension_history.version IS '版本号';
COMMENT ON COLUMN bpmn_element_extension_history.operation_type IS '操作类型(CREATE/UPDATE/DELETE)';
COMMENT ON COLUMN bpmn_element_extension_history.operation_time IS '操作时间';
