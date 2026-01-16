-- V6__create_process_category_tables.sql
-- 创建流程分类表（支持多租户和路径记录）
-- 作者：LingFlow Team
-- 日期：2026-01-16

-- =====================================================
-- 1. 流程分类表
-- =====================================================
CREATE TABLE IF NOT EXISTS process_category (
    -- 主键和基本信息
    id VARCHAR(36) PRIMARY KEY,                  -- 分类ID (UUID)
    name VARCHAR(100) NOT NULL,                  -- 分类名称
    code VARCHAR(50) NOT NULL,                   -- 分类编码（用于API调用、系统引用等）
    parent_id VARCHAR(36),                       -- 父分类ID (NULL表示根节点)

    -- 路径和层级（支持快速查询）
    path VARCHAR(1000),                          -- 分类路径（/id1/id2/id3）
    level INT DEFAULT 0,                         -- 层级深度（根节点为0）

    -- 扩展属性
    description VARCHAR(500),                    -- 分类描述
    sort_order INT DEFAULT 0,                    -- 排序序号（同级分类的排序）
    icon VARCHAR(100),                           -- 图标（Element Plus 图标名或 URL）

    -- 多租户字段
    app_id VARCHAR(50),                          -- 应用ID（可选，用于更细粒度的隔离）
    context_id VARCHAR(50),                      -- 上下文ID（可选，用于特定业务场景）
    tenant_id VARCHAR(50) NOT NULL,              -- 租户ID（必需，用于多租户隔离）

    -- 审计字段
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),                      -- 创建人
    updated_by VARCHAR(50),                      -- 更新人
    is_deleted BOOLEAN DEFAULT FALSE,            -- 软删除标记
    version INT DEFAULT 1                        -- 乐观锁版本号
);

-- =====================================================
-- 2. 索引和约束
-- =====================================================

-- 唯一约束：同一租户、应用、上下文下编码唯一
CREATE UNIQUE INDEX uk_category_code
    ON process_category(code, tenant_id, app_id, context_id)
    WHERE is_deleted = FALSE;

-- 父节点索引（支持快速查找子节点）
CREATE INDEX idx_category_parent
    ON process_category(parent_id)
    WHERE is_deleted = FALSE;

-- 多租户索引（支持租户数据隔离）
CREATE INDEX idx_category_tenant
    ON process_category(tenant_id, app_id, context_id)
    WHERE is_deleted = FALSE;

-- 路径索引（支持路径前缀查询，用于查找某个节点下的所有后代）
CREATE INDEX idx_category_path
    ON process_category(path)
    WHERE is_deleted = FALSE;

-- 排序索引（支持按排序字段查询）
CREATE INDEX idx_category_sort
    ON process_category(sort_order);

-- 层级索引（支持按层级查询）
CREATE INDEX idx_category_level
    ON process_category(level);

-- 组合索引：父节点 + 排序（支持获取同级排序后的子节点列表）
CREATE INDEX idx_category_parent_sort
    ON process_category(parent_id, sort_order)
    WHERE is_deleted = FALSE;

-- 自引用外键约束
ALTER TABLE process_category
ADD CONSTRAINT fk_category_parent
FOREIGN KEY (parent_id) REFERENCES process_category(id)
ON DELETE CASCADE;

-- =====================================================
-- 3. 表和字段注释
-- =====================================================
COMMENT ON TABLE process_category IS '流程分类表（支持多租户和路径记录）';
COMMENT ON COLUMN process_category.id IS '分类ID (UUID)';
COMMENT ON COLUMN process_category.name IS '分类名称';
COMMENT ON COLUMN process_category.code IS '分类编码（用于API调用、系统引用等）';
COMMENT ON COLUMN process_category.parent_id IS '父分类ID (NULL表示根节点)';
COMMENT ON COLUMN process_category.path IS '分类路径（/id1/id2/id3，用于快速查询子树）';
COMMENT ON COLUMN process_category.level IS '层级深度（根节点为0）';
COMMENT ON COLUMN process_category.description IS '分类描述';
COMMENT ON COLUMN process_category.sort_order IS '排序序号（同级分类的排序）';
COMMENT ON COLUMN process_category.icon IS '图标（Element Plus 图标名或 URL）';
COMMENT ON COLUMN process_category.app_id IS '应用ID（可选，用于更细粒度的隔离）';
COMMENT ON COLUMN process_category.context_id IS '上下文ID（可选，用于特定业务场景）';
COMMENT ON COLUMN process_category.tenant_id IS '租户ID（必需，用于多租户隔离）';
COMMENT ON COLUMN process_category.created_by IS '创建人';
COMMENT ON COLUMN process_category.updated_by IS '更新人';
COMMENT ON COLUMN process_category.is_deleted IS '软删除标记';
COMMENT ON COLUMN process_category.version IS '乐观锁版本号';

-- =====================================================
-- 4. 流程定义扩展表
-- =====================================================
CREATE TABLE IF NOT EXISTS process_definition_extension (
    -- 主键
    id VARCHAR(36) PRIMARY KEY,

    -- 关联字段
    process_definition_id VARCHAR(255) NOT NULL UNIQUE,  -- 关联Flowable的流程定义ID
    category_id VARCHAR(36),                             -- 分类ID（外键关联到分类表）

    -- 扩展属性
    tags VARCHAR(500),                                   -- 标签（JSON数组格式：["tag1","tag2"]）

    -- 多租户字段（冗余，方便查询和报表）
    app_id VARCHAR(50),
    context_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,

    -- 审计字段
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 外键约束
    CONSTRAINT fk_pde_category
        FOREIGN KEY (category_id)
        REFERENCES process_category(id)
        ON DELETE SET NULL
);

-- =====================================================
-- 5. 扩展表索引和约束
-- =====================================================

-- 唯一约束：流程定义ID唯一
CREATE UNIQUE INDEX uk_pde_process_def
    ON process_definition_extension(process_definition_id);

-- 分类ID索引（支持按分类查询流程）
CREATE INDEX idx_pde_category
    ON process_definition_extension(category_id)
    WHERE category_id IS NOT NULL;

-- 多租户索引（支持租户数据隔离）
CREATE INDEX idx_pde_tenant
    ON process_definition_extension(tenant_id, app_id, context_id);

-- PostgreSQL 全文搜索索引（支持标签搜索）
CREATE INDEX idx_pde_tags_gin
    ON process_definition_extension
    USING gin(to_tsvector('simple', COALESCE(tags, '')));

-- =====================================================
-- 6. 扩展表注释
-- =====================================================
COMMENT ON TABLE process_definition_extension IS '流程定义扩展表（关联分类和标签）';
COMMENT ON COLUMN process_definition_extension.id IS '主键ID (UUID)';
COMMENT ON COLUMN process_definition_extension.process_definition_id IS '关联Flowable的流程定义ID';
COMMENT ON COLUMN process_definition_extension.category_id IS '所属分类ID';
COMMENT ON COLUMN process_definition_extension.tags IS '标签（JSON数组格式：["tag1","tag2"]）';
COMMENT ON COLUMN process_definition_extension.app_id IS '应用ID（冗余，方便查询）';
COMMENT ON COLUMN process_definition_extension.context_id IS '上下文ID（冗余，方便查询）';
COMMENT ON COLUMN process_definition_extension.tenant_id IS '租户ID（冗余，方便查询）';

-- =====================================================
-- 7. 初始化默认分类（每个租户的默认分类）
-- =====================================================
-- 注意：这里只是示例，实际插入时需要根据租户ID动态生成
-- INSERT INTO process_category (id, name, code, parent_id, path, level, sort_order, tenant_id, created_by, updated_by)
-- VALUES
--     (gen_random_uuid()::text, '默认分类', 'default', NULL, '/', 0, 0, 'default_tenant', 'system', 'system');

-- =====================================================
-- 8. 触发器：自动更新 updated_time 字段
-- =====================================================
-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为 process_category 表创建触发器
DROP TRIGGER IF EXISTS update_process_category_updated_time ON process_category;
CREATE TRIGGER update_process_category_updated_time
    BEFORE UPDATE ON process_category
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 为 process_definition_extension 表创建触发器
DROP TRIGGER IF EXISTS update_process_definition_extension_updated_time ON process_definition_extension;
CREATE TRIGGER update_process_definition_extension_updated_time
    BEFORE UPDATE ON process_definition_extension
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- =====================================================
-- 9. 视图：分类树视图（方便查询）
-- =====================================================
-- 创建分类树视图（包含父子关系信息）
CREATE OR REPLACE VIEW v_process_category_tree AS
SELECT
    c.id,
    c.name,
    c.code,
    c.parent_id,
    c.path,
    c.level,
    c.description,
    c.sort_order,
    c.icon,
    c.tenant_id,
    c.app_id,
    c.context_id,
    c.created_time,
    c.updated_time,
    c.created_by,
    c.updated_by,
    c.is_deleted,
    c.version,
    -- 父分类名称
    p.name AS parent_name,
    -- 是否有子分类
    CASE WHEN EXISTS(SELECT 1 FROM process_category cc WHERE cc.parent_id = c.id AND cc.is_deleted = FALSE) THEN TRUE ELSE FALSE END AS has_children,
    -- 子分类数量
    (SELECT COUNT(*) FROM process_category cc WHERE cc.parent_id = c.id AND cc.is_deleted = FALSE) AS children_count
FROM process_category c
LEFT JOIN process_category p ON c.parent_id = p.id AND p.is_deleted = FALSE
WHERE c.is_deleted = FALSE;

COMMENT ON VIEW v_process_category_tree IS '流程分类树视图（包含父子关系信息）';

-- =====================================================
-- 10. 辅助函数：计算路径
-- =====================================================
-- 函数：从父节点ID构建路径
CREATE OR REPLACE FUNCTION build_category_path(category_id VARCHAR(36))
RETURNS VARCHAR(1000) AS $$
DECLARE
    parent_id VARCHAR(36);
    parent_path VARCHAR(1000);
    result_path VARCHAR(1000);
BEGIN
    -- 获取父节点ID
    SELECT parent_id INTO parent_id FROM process_category WHERE id = category_id;

    -- 如果没有父节点，返回 /category_id
    IF parent_id IS NULL THEN
        RETURN '/' || category_id;
    END IF;

    -- 递归获取父节点路径
    parent_path := build_category_path(parent_id);

    -- 拼接完整路径
    result_path := parent_path || '/' || category_id;

    RETURN result_path;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION build_category_path IS '从父节点ID构建分类路径';

-- =====================================================
-- 11. 辅助函数：计算层级
-- =====================================================
-- 函数：从路径计算层级深度
CREATE OR REPLACE FUNCTION calculate_category_level(path VARCHAR(1000))
RETURNS INT AS $$
BEGIN
    -- 路径格式：/id1/id2/id3
    -- 通过计算斜杠数量来确定层级
    -- /id1 = level 1 (但实际上根节点应该是0)
    -- 所以返回 length - 1
    RETURN array_length(string_to_array(path, '/'), 1) - 1;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION calculate_category_level IS '从路径计算层级深度';

-- =====================================================
-- 12. 完成标记
-- =====================================================
-- 迁移脚本执行完成
-- 下一步：实现后端 Entity、Repository、Service、Controller
