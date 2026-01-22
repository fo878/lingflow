-- V7__create_process_template_tables.sql
-- 创建流程模板系统表（设计态、发布态、快照）
-- 作者：LingFlow Team
-- 日期：2026-01-22

-- =====================================================
-- 1. 设计态流程模板表
-- =====================================================
CREATE TABLE IF NOT EXISTS process_template_draft (
    -- 主键和基本信息
    id VARCHAR(36) PRIMARY KEY,                       -- 模板ID (UUID)
    template_key VARCHAR(100) NOT NULL,               -- 流程模板Key（同一租户下唯一）
    template_name VARCHAR(200) NOT NULL,              -- 流程模板名称
    description VARCHAR(1000),                        -- 流程模板描述

    -- BPMN 内容
    bpmn_xml TEXT NOT NULL,                           -- BPMN XML内容

    -- 分类关联
    category_id VARCHAR(36) NOT NULL,                 -- 所属分类ID（外键）

    -- 扩展属性（JSONB格式）
    tags JSONB,                                       -- 标签数组：["tag1","tag2"]
    form_config JSONB,                                -- 表单配置：JSON对象

    -- 多租户字段
    app_id VARCHAR(50),                               -- 应用ID
    context_id VARCHAR(50),                           -- 上下文ID
    tenant_id VARCHAR(50) NOT NULL,                   -- 租户ID

    -- 状态字段（固定为DRAFT）
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',      -- 状态：DRAFT

    -- 审计字段
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),                           -- 创建人
    updated_by VARCHAR(50),                           -- 更新人
    version INT DEFAULT 1,                            -- 乐观锁版本号

    -- 外键约束
    CONSTRAINT fk_draft_category
        FOREIGN KEY (category_id)
        REFERENCES process_category(id)
        ON DELETE RESTRICT
);

-- =====================================================
-- 2. 发布态流程模板表
-- =====================================================
CREATE TABLE IF NOT EXISTS process_template_published (
    -- 主键和基本信息
    id VARCHAR(36) PRIMARY KEY,                       -- 模板ID (UUID)
    template_key VARCHAR(100) NOT NULL,               -- 流程模板Key
    template_name VARCHAR(200) NOT NULL,              -- 流程模板名称
    description VARCHAR(1000),                        -- 流程模板描述

    -- BPMN 内容
    bpmn_xml TEXT NOT NULL,                           -- BPMN XML内容

    -- Flowable 关联字段
    flowable_process_definition_id VARCHAR(255) NOT NULL UNIQUE, -- Flowable流程定义ID
    flowable_deployment_id VARCHAR(255),              -- Flowable部署ID
    flowable_version INT NOT NULL,                    -- Flowable版本号

    -- 分类关联
    category_id VARCHAR(36) NOT NULL,                 -- 所属分类ID（外键）

    -- 扩展属性（JSONB格式）
    tags JSONB,                                       -- 标签数组
    form_config JSONB,                                -- 表单配置

    -- 多租户字段
    app_id VARCHAR(50),                               -- 应用ID
    context_id VARCHAR(50),                           -- 上下文ID
    tenant_id VARCHAR(50) NOT NULL,                   -- 租户ID

    -- 状态字段
    status VARCHAR(20) NOT NULL,                      -- ACTIVE/INACTIVE

    -- 统计字段
    instance_count INT DEFAULT 0,                     -- 流程实例总数
    running_instance_count INT DEFAULT 0,             -- 运行中的流程实例数

    -- 审计字段
    published_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- 发布时间
    suspended_time TIMESTAMP,                             -- 停用时间
    created_by VARCHAR(50),                           -- 发布人
    version INT DEFAULT 1,                            -- 乐观锁版本号

    -- 外键约束
    CONSTRAINT fk_published_category
        FOREIGN KEY (category_id)
        REFERENCES process_category(id)
        ON DELETE RESTRICT
);

-- =====================================================
-- 3. 流程模板快照表
-- =====================================================
CREATE TABLE IF NOT EXISTS process_template_snapshot (
    -- 主键
    id VARCHAR(36) PRIMARY KEY,                       -- 快照ID (UUID)

    -- 快照标识
    template_key VARCHAR(100) NOT NULL,               -- 流程模板Key
    snapshot_name VARCHAR(200) NOT NULL,              -- 快照名称

    -- BPMN 内容
    bpmn_xml TEXT NOT NULL,                           -- BPMN XML内容（快照时的完整内容）

    -- 快照来源信息
    source_template_id VARCHAR(36) NOT NULL,          -- 来源模板ID
    source_template_status VARCHAR(20) NOT NULL,      -- 来源模板状态（DRAFT/ACTIVE/INACTIVE）
    source_template_version INT,                      -- 来源模板版本号

    -- 分类信息（冗余，方便查询）
    category_id VARCHAR(36),                          -- 分类ID
    category_name VARCHAR(100),                       -- 分类名称（冗余，快照时的分类名）
    category_code VARCHAR(50),                        -- 分类编码（冗余）

    -- 扩展属性（JSONB格式）
    tags JSONB,                                       -- 标签数组
    form_config JSONB,                                -- 表单配置

    -- 多租户字段
    app_id VARCHAR(50),                               -- 应用ID
    context_id VARCHAR(50),                           -- 上下文ID
    tenant_id VARCHAR(50) NOT NULL,                   -- 租户ID

    -- 版本管理
    snapshot_version INT NOT NULL,                    -- 快照版本号（独立序列）

    -- 审计字段
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)                            -- 创建人
);

-- =====================================================
-- 4. 索引和约束
-- =====================================================

-- ========== process_template_draft 索引 ==========

-- 唯一约束：同一租户、应用、上下文下，模板Key唯一
CREATE UNIQUE INDEX uk_draft_key
    ON process_template_draft(template_key, tenant_id, app_id, context_id);

-- 分类ID索引（支持按分类查询）
CREATE INDEX idx_draft_category
    ON process_template_draft(category_id)
    WHERE status = 'DRAFT';

-- 多租户索引（支持租户数据隔离）
CREATE INDEX idx_draft_tenant
    ON process_template_draft(tenant_id, app_id, context_id);

-- 状态索引
CREATE INDEX idx_draft_status
    ON process_template_draft(status);

-- 创建时间索引（支持按时间排序）
CREATE INDEX idx_draft_created_time
    ON process_template_draft(created_time DESC);

-- 创建人索引
CREATE INDEX idx_draft_created_by
    ON process_template_draft(created_by);

-- ========== process_template_published 索引 ==========

-- 唯一约束：同一租户、应用、上下文、分类下，相同key的激活模板只能有一个
CREATE UNIQUE INDEX uk_published_active_key
    ON process_template_published(template_key, tenant_id, app_id, context_id, category_id)
    WHERE status = 'ACTIVE';

-- 分类ID索引（支持按分类查询）
CREATE INDEX idx_published_category
    ON process_template_published(category_id)
    WHERE status IN ('ACTIVE', 'INACTIVE');

-- 多租户索引（支持租户数据隔离）
CREATE INDEX idx_published_tenant
    ON process_template_published(tenant_id, app_id, context_id);

-- 状态索引
CREATE INDEX idx_published_status
    ON process_template_published(status);

-- Flowable流程定义ID索引
CREATE INDEX idx_published_flowable_def_id
    ON process_template_published(flowable_process_definition_id);

-- Flowable版本号索引
CREATE INDEX idx_published_flowable_version
    ON process_template_published(template_key, flowable_version);

-- 发布时间索引（支持按时间排序）
CREATE INDEX idx_published_time
    ON process_template_published(published_time DESC);

-- ========== process_template_snapshot 索引 ==========

-- 唯一约束：同一租户、应用、上下文、模板key下，快照版本号唯一
CREATE UNIQUE INDEX uk_snapshot_version
    ON process_template_snapshot(template_key, tenant_id, app_id, context_id, snapshot_version);

-- 模板Key索引（支持查询某个模板的所有快照）
CREATE INDEX idx_snapshot_template_key
    ON process_template_snapshot(template_key, tenant_id, app_id, context_id);

-- 来源模板ID索引
CREATE INDEX idx_snapshot_source_id
    ON process_template_snapshot(source_template_id);

-- 多租户索引
CREATE INDEX idx_snapshot_tenant
    ON process_template_snapshot(tenant_id, app_id, context_id);

-- 创建时间索引
CREATE INDEX idx_snapshot_created_time
    ON process_template_snapshot(created_time DESC);

-- =====================================================
-- 5. 表和字段注释
-- =====================================================

COMMENT ON TABLE process_template_draft IS '设计态流程模板表（未发布到Flowable）';
COMMENT ON COLUMN process_template_draft.id IS '模板ID (UUID)';
COMMENT ON COLUMN process_template_draft.template_key IS '流程模板Key（同一租户下唯一）';
COMMENT ON COLUMN process_template_draft.template_name IS '流程模板名称';
COMMENT ON COLUMN process_template_draft.description IS '流程模板描述';
COMMENT ON COLUMN process_template_draft.bpmn_xml IS 'BPMN XML内容';
COMMENT ON COLUMN process_template_draft.category_id IS '所属分类ID（外键）';
COMMENT ON COLUMN process_template_draft.tags IS '标签数组（JSONB格式）';
COMMENT ON COLUMN process_template_draft.form_config IS '表单配置（JSONB格式）';
COMMENT ON COLUMN process_template_draft.app_id IS '应用ID';
COMMENT ON COLUMN process_template_draft.context_id IS '上下文ID';
COMMENT ON COLUMN process_template_draft.tenant_id IS '租户ID';
COMMENT ON COLUMN process_template_draft.status IS '状态（固定为DRAFT）';
COMMENT ON COLUMN process_template_draft.created_by IS '创建人';
COMMENT ON COLUMN process_template_draft.updated_by IS '更新人';
COMMENT ON COLUMN process_template_draft.version IS '乐观锁版本号';

COMMENT ON TABLE process_template_published IS '发布态流程模板表（已发布到Flowable）';
COMMENT ON COLUMN process_template_published.id IS '模板ID (UUID)';
COMMENT ON COLUMN process_template_published.template_key IS '流程模板Key';
COMMENT ON COLUMN process_template_published.template_name IS '流程模板名称';
COMMENT ON COLUMN process_template_published.description IS '流程模板描述';
COMMENT ON COLUMN process_template_published.bpmn_xml IS 'BPMN XML内容';
COMMENT ON COLUMN process_template_published.flowable_process_definition_id IS 'Flowable流程定义ID';
COMMENT ON COLUMN process_template_published.flowable_deployment_id IS 'Flowable部署ID';
COMMENT ON COLUMN process_template_published.flowable_version IS 'Flowable版本号';
COMMENT ON COLUMN process_template_published.category_id IS '所属分类ID（外键）';
COMMENT ON COLUMN process_template_published.tags IS '标签数组（JSONB格式）';
COMMENT ON COLUMN process_template_published.form_config IS '表单配置（JSONB格式）';
COMMENT ON COLUMN process_template_published.app_id IS '应用ID';
COMMENT ON COLUMN process_template_published.context_id IS '上下文ID';
COMMENT ON COLUMN process_template_published.tenant_id IS '租户ID';
COMMENT ON COLUMN process_template_published.status IS '状态（ACTIVE/INACTIVE）';
COMMENT ON COLUMN process_template_published.instance_count IS '流程实例总数';
COMMENT ON COLUMN process_template_published.running_instance_count IS '运行中的流程实例数';
COMMENT ON COLUMN process_template_published.published_time IS '发布时间';
COMMENT ON COLUMN process_template_published.suspended_time IS '停用时间';
COMMENT ON COLUMN process_template_published.created_by IS '发布人';
COMMENT ON COLUMN process_template_published.version IS '乐观锁版本号';

COMMENT ON TABLE process_template_snapshot IS '流程模板快照表（历史版本）';
COMMENT ON COLUMN process_template_snapshot.id IS '快照ID (UUID)';
COMMENT ON COLUMN process_template_snapshot.template_key IS '流程模板Key';
COMMENT ON COLUMN process_template_snapshot.snapshot_name IS '快照名称';
COMMENT ON COLUMN process_template_snapshot.bpmn_xml IS 'BPMN XML内容';
COMMENT ON COLUMN process_template_snapshot.source_template_id IS '来源模板ID';
COMMENT ON COLUMN process_template_snapshot.source_template_status IS '来源模板状态';
COMMENT ON COLUMN process_template_snapshot.source_template_version IS '来源模板版本号';
COMMENT ON COLUMN process_template_snapshot.category_id IS '分类ID（冗余）';
COMMENT ON COLUMN process_template_snapshot.category_name IS '分类名称（冗余）';
COMMENT ON COLUMN process_template_snapshot.category_code IS '分类编码（冗余）';
COMMENT ON COLUMN process_template_snapshot.tags IS '标签数组（JSONB格式）';
COMMENT ON COLUMN process_template_snapshot.form_config IS '表单配置（JSONB格式）';
COMMENT ON COLUMN process_template_snapshot.app_id IS '应用ID';
COMMENT ON COLUMN process_template_snapshot.context_id IS '上下文ID';
COMMENT ON COLUMN process_template_snapshot.tenant_id IS '租户ID';
COMMENT ON COLUMN process_template_snapshot.snapshot_version IS '快照版本号（独立序列）';
COMMENT ON COLUMN process_template_snapshot.created_time IS '创建时间';
COMMENT ON COLUMN process_template_snapshot.created_by IS '创建人';

-- =====================================================
-- 6. 触发器：自动更新 updated_time 字段
-- =====================================================

-- 为 process_template_draft 表创建触发器
DROP TRIGGER IF EXISTS update_process_template_draft_updated_time ON process_template_draft;
CREATE TRIGGER update_process_template_draft_updated_time
    BEFORE UPDATE ON process_template_draft
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- =====================================================
-- 7. 辅助函数：获取下一个模板版本号
-- =====================================================
CREATE OR REPLACE FUNCTION get_next_template_version(
    p_template_key VARCHAR,
    p_tenant_id VARCHAR,
    p_app_id VARCHAR DEFAULT NULL,
    p_context_id VARCHAR DEFAULT NULL
)
RETURNS INT AS $$
DECLARE
    next_version INT;
BEGIN
    -- 获取当前最大版本号
    SELECT COALESCE(MAX(flowable_version), 0) + 1
    INTO next_version
    FROM process_template_published
    WHERE template_key = p_template_key
      AND tenant_id = p_tenant_id
      AND (p_app_id IS NULL OR app_id = p_app_id)
      AND (p_context_id IS NULL OR context_id = p_context_id);

    RETURN next_version;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_next_template_version IS '获取下一个模板版本号';

-- =====================================================
-- 8. 辅助函数：检查是否存在激活的模板
-- =====================================================
CREATE OR REPLACE FUNCTION has_active_template(
    p_template_key VARCHAR,
    p_tenant_id VARCHAR,
    p_app_id VARCHAR DEFAULT NULL,
    p_context_id VARCHAR DEFAULT NULL
)
RETURNS BOOLEAN AS $$
DECLARE
    has_active BOOLEAN;
BEGIN
    SELECT EXISTS(
        SELECT 1
        FROM process_template_published
        WHERE template_key = p_template_key
          AND tenant_id = p_tenant_id
          AND status = 'ACTIVE'
          AND (p_app_id IS NULL OR app_id = p_app_id)
          AND (p_context_id IS NULL OR context_id = p_context_id)
    ) INTO has_active;

    RETURN has_active;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION has_active_template IS '检查是否存在激活的模板';

-- =====================================================
-- 9. 辅助函数：获取下一个快照版本号
-- =====================================================
CREATE OR REPLACE FUNCTION get_next_snapshot_version(
    p_template_key VARCHAR,
    p_tenant_id VARCHAR,
    p_app_id VARCHAR DEFAULT NULL,
    p_context_id VARCHAR DEFAULT NULL
)
RETURNS INT AS $$
DECLARE
    next_version INT;
BEGIN
    -- 获取当前最大快照版本号
    SELECT COALESCE(MAX(snapshot_version), 0) + 1
    INTO next_version
    FROM process_template_snapshot
    WHERE template_key = p_template_key
      AND tenant_id = p_tenant_id
      AND (p_app_id IS NULL OR app_id = p_app_id)
      AND (p_context_id IS NULL OR context_id = p_context_id);

    RETURN next_version;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_next_snapshot_version IS '获取下一个快照版本号';

-- =====================================================
-- 10. 视图：统一流程模板视图
-- =====================================================
CREATE OR REPLACE VIEW v_process_template_all AS
-- 设计态模板
SELECT
    ptd.id,
    ptd.template_key,
    ptd.template_name,
    ptd.description,
    ptd.bpmn_xml,
    NULL::VARCHAR AS flowable_process_definition_id,
    NULL::VARCHAR AS flowable_deployment_id,
    NULL::INT AS flowable_version,
    ptd.category_id,
    pc.name AS category_name,
    pc.code AS category_code,
    ptd.tags,
    ptd.form_config,
    ptd.app_id,
    ptd.context_id,
    ptd.tenant_id,
    ptd.status,
    NULL::INT AS instance_count,
    NULL::INT AS running_instance_count,
    ptd.created_time,
    ptd.updated_time,
    NULL::TIMESTAMP AS published_time,
    NULL::TIMESTAMP AS suspended_time,
    ptd.created_by,
    ptd.updated_by,
    ptd.version,
    'DRAFT'::VARCHAR AS template_type
FROM process_template_draft ptd
LEFT JOIN process_category pc ON ptd.category_id = pc.id

UNION ALL

-- 发布态模板
SELECT
    ptp.id,
    ptp.template_key,
    ptp.template_name,
    ptp.description,
    ptp.bpmn_xml,
    ptp.flowable_process_definition_id,
    ptp.flowable_deployment_id,
    ptp.flowable_version,
    ptp.category_id,
    pc.name AS category_name,
    pc.code AS category_code,
    ptp.tags,
    ptp.form_config,
    ptp.app_id,
    ptp.context_id,
    ptp.tenant_id,
    ptp.status,
    ptp.instance_count,
    ptp.running_instance_count,
    ptp.published_time AS created_time,
    NULL::TIMESTAMP AS updated_time,
    ptp.published_time,
    ptp.suspended_time,
    ptp.created_by,
    NULL::VARCHAR AS updated_by,
    ptp.version,
    'PUBLISHED'::VARCHAR AS template_type
FROM process_template_published ptp
LEFT JOIN process_category pc ON ptp.category_id = pc.id;

COMMENT ON VIEW v_process_template_all IS '统一流程模板视图（包含设计态和发布态）';

-- =====================================================
-- 11. 辅助函数：从快照恢复到设计态
-- =====================================================
CREATE OR REPLACE FUNCTION restore_snapshot_to_draft(
    p_snapshot_id VARCHAR,
    p_new_draft_id VARCHAR,
    p_restored_by VARCHAR
)
RETURNS VARCHAR AS $$
DECLARE
    v_template_key VARCHAR;
    v_template_name VARCHAR;
    v_bpmn_xml TEXT;
    v_category_id VARCHAR;
    v_tags JSONB;
    v_form_config JSONB;
    v_app_id VARCHAR;
    v_context_id VARCHAR;
    v_tenant_id VARCHAR;
BEGIN
    -- 获取快照信息
    SELECT template_key, snapshot_name, bpmn_xml, category_id, tags, form_config,
           app_id, context_id, tenant_id
    INTO v_template_key, v_template_name, v_bpmn_xml, v_category_id, v_tags, v_form_config,
         v_app_id, v_context_id, v_tenant_id
    FROM process_template_snapshot
    WHERE id = p_snapshot_id;

    -- 如果快照不存在，抛出异常
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Snapshot not found: %', p_snapshot_id;
    END IF;

    -- 创建新的设计态模板
    INSERT INTO process_template_draft (
        id,
        template_key,
        template_name,
        description,
        bpmn_xml,
        category_id,
        tags,
        form_config,
        app_id,
        context_id,
        tenant_id,
        status,
        created_by,
        updated_by
    ) VALUES (
        p_new_draft_id,
        v_template_key,
        v_template_name,
        'Restored from snapshot: ' || p_snapshot_id,
        v_bpmn_xml,
        v_category_id,
        v_tags,
        v_form_config,
        v_app_id,
        v_context_id,
        v_tenant_id,
        'DRAFT',
        p_restored_by,
        p_restored_by
    );

    -- 返回新创建的设计态模板ID
    RETURN p_new_draft_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION restore_snapshot_to_draft IS '从快照恢复到设计态';

-- =====================================================
-- 12. 完成标记
-- =====================================================
-- 迁移脚本执行完成
-- 下一步：实现后端 Entity、Repository、Service、Controller
