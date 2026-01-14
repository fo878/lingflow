-- 修复表权限问题
-- 确保lingflow用户有对process_snapshot表的完全权限

-- 授予对process_snapshot表的所有权限
GRANT ALL PRIVILEGES ON TABLE process_snapshot TO lingflow;

-- 授予对bpmn_element_extension表的所有权限
GRANT ALL PRIVILEGES ON TABLE bpmn_element_extension TO lingflow;

-- 授予对bpmn_element_extension_history表的所有权限
GRANT ALL PRIVILEGES ON TABLE bpmn_element_extension_history TO lingflow;

-- 授予对序列的权限
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO lingflow;

-- 授予对表的SELECT权限
GRANT SELECT ON ALL TABLES IN SCHEMA public TO lingflow;

-- 确保对将来的表也有权限
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO lingflow;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO lingflow;
