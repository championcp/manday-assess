-- NESMA功能点评估系统 - NESMA功能点详细信息表
-- Version: V4__NESMA_function_point_details.sql
-- Description: 创建ILF, EIF, EI, EO, EQ五种功能点类型的详细信息表

-- 创建ILF（内部逻辑文件）详细信息表
CREATE TABLE IF NOT EXISTS ilf_details (
    id BIGSERIAL PRIMARY KEY,
    function_point_id BIGINT NOT NULL UNIQUE,
    file_name VARCHAR(200) NOT NULL,
    file_description TEXT,
    primary_intent TEXT NOT NULL,
    logical_records_count INTEGER NOT NULL DEFAULT 0,
    data_element_types_count INTEGER NOT NULL DEFAULT 0,
    record_types_count INTEGER NOT NULL DEFAULT 0,
    data_groups JSONB,
    key_fields JSONB,
    business_rules JSONB,
    data_validation_rules JSONB,
    data_retention_policy TEXT,
    security_requirements TEXT,
    performance_requirements TEXT,
    is_master_file BOOLEAN NOT NULL DEFAULT false,
    is_reference_data BOOLEAN NOT NULL DEFAULT false,
    data_volume_estimate DECIMAL(19,4),
    update_frequency VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (function_point_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 创建EIF（外部接口文件）详细信息表
CREATE TABLE IF NOT EXISTS eif_details (
    id BIGSERIAL PRIMARY KEY,
    function_point_id BIGINT NOT NULL UNIQUE,
    interface_name VARCHAR(200) NOT NULL,
    interface_description TEXT,
    external_system_name VARCHAR(200) NOT NULL,
    interface_type VARCHAR(50) NOT NULL,
    data_format VARCHAR(50) NOT NULL,
    logical_records_count INTEGER NOT NULL DEFAULT 0,
    data_element_types_count INTEGER NOT NULL DEFAULT 0,
    record_types_count INTEGER NOT NULL DEFAULT 0,
    interface_protocol VARCHAR(100),
    data_mapping_rules JSONB,
    transformation_rules JSONB,
    validation_rules JSONB,
    error_handling_rules JSONB,
    frequency_of_access VARCHAR(50),
    data_volume_per_transaction DECIMAL(19,4),
    peak_load_requirements TEXT,
    security_requirements TEXT,
    is_real_time BOOLEAN NOT NULL DEFAULT false,
    is_bidirectional BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (function_point_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 创建EI（外部输入）详细信息表
CREATE TABLE IF NOT EXISTS ei_details (
    id BIGSERIAL PRIMARY KEY,
    function_point_id BIGINT NOT NULL UNIQUE,
    input_name VARCHAR(200) NOT NULL,
    input_description TEXT,
    input_source VARCHAR(200) NOT NULL,
    input_type VARCHAR(50) NOT NULL,
    input_method VARCHAR(50) NOT NULL,
    data_element_types_count INTEGER NOT NULL DEFAULT 0,
    file_types_referenced_count INTEGER NOT NULL DEFAULT 0,
    processing_logic_count INTEGER NOT NULL DEFAULT 0,
    input_data_structure JSONB,
    validation_rules JSONB,
    business_rules JSONB,
    error_handling_rules JSONB,
    processing_steps JSONB,
    output_generated TEXT,
    files_updated JSONB,
    transaction_volume_per_day DECIMAL(19,4),
    peak_processing_requirements TEXT,
    response_time_requirements VARCHAR(100),
    security_requirements TEXT,
    is_batch_processing BOOLEAN NOT NULL DEFAULT false,
    is_real_time_processing BOOLEAN NOT NULL DEFAULT false,
    requires_approval BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (function_point_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 创建EO（外部输出）详细信息表
CREATE TABLE IF NOT EXISTS eo_details (
    id BIGSERIAL PRIMARY KEY,
    function_point_id BIGINT NOT NULL UNIQUE,
    output_name VARCHAR(200) NOT NULL,
    output_description TEXT,
    output_destination VARCHAR(200) NOT NULL,
    output_type VARCHAR(50) NOT NULL,
    output_format VARCHAR(50) NOT NULL,
    data_element_types_count INTEGER NOT NULL DEFAULT 0,
    file_types_referenced_count INTEGER NOT NULL DEFAULT 0,
    processing_logic_count INTEGER NOT NULL DEFAULT 0,
    output_data_structure JSONB,
    calculation_rules JSONB,
    formatting_rules JSONB,
    business_rules JSONB,
    data_sources JSONB,
    processing_steps JSONB,
    derived_data_rules JSONB,
    report_parameters JSONB,
    output_volume_estimate DECIMAL(19,4),
    generation_frequency VARCHAR(50),
    performance_requirements TEXT,
    security_requirements TEXT,
    distribution_requirements TEXT,
    is_summary_report BOOLEAN NOT NULL DEFAULT false,
    is_detail_report BOOLEAN NOT NULL DEFAULT false,
    requires_signature BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (function_point_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 创建EQ（外部查询）详细信息表
CREATE TABLE IF NOT EXISTS eq_details (
    id BIGSERIAL PRIMARY KEY,
    function_point_id BIGINT NOT NULL UNIQUE,
    query_name VARCHAR(200) NOT NULL,
    query_description TEXT,
    query_source VARCHAR(200) NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    query_method VARCHAR(50) NOT NULL,
    input_data_element_types_count INTEGER NOT NULL DEFAULT 0,
    output_data_element_types_count INTEGER NOT NULL DEFAULT 0,
    file_types_referenced_count INTEGER NOT NULL DEFAULT 0,
    processing_logic_count INTEGER NOT NULL DEFAULT 0,
    input_criteria JSONB,
    search_logic JSONB,
    output_data_structure JSONB,
    sorting_rules JSONB,
    filtering_rules JSONB,
    business_rules JSONB,
    data_sources JSONB,
    query_parameters JSONB,
    expected_result_volume DECIMAL(19,4),
    query_frequency VARCHAR(50),
    response_time_requirements VARCHAR(100),
    concurrent_user_support INTEGER,
    security_requirements TEXT,
    is_complex_search BOOLEAN NOT NULL DEFAULT false,
    supports_pagination BOOLEAN NOT NULL DEFAULT false,
    supports_sorting BOOLEAN NOT NULL DEFAULT false,
    supports_filtering BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (function_point_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 为所有详细信息表创建索引
CREATE INDEX idx_ilf_details_function_point_id ON ilf_details(function_point_id);
CREATE INDEX idx_ilf_details_file_name ON ilf_details(file_name);
CREATE INDEX idx_ilf_details_is_master_file ON ilf_details(is_master_file);
CREATE INDEX idx_ilf_details_update_frequency ON ilf_details(update_frequency);
CREATE INDEX idx_ilf_details_deleted_at ON ilf_details(deleted_at);

CREATE INDEX idx_eif_details_function_point_id ON eif_details(function_point_id);
CREATE INDEX idx_eif_details_interface_name ON eif_details(interface_name);
CREATE INDEX idx_eif_details_external_system_name ON eif_details(external_system_name);
CREATE INDEX idx_eif_details_interface_type ON eif_details(interface_type);
CREATE INDEX idx_eif_details_deleted_at ON eif_details(deleted_at);

CREATE INDEX idx_ei_details_function_point_id ON ei_details(function_point_id);
CREATE INDEX idx_ei_details_input_name ON ei_details(input_name);
CREATE INDEX idx_ei_details_input_type ON ei_details(input_type);
CREATE INDEX idx_ei_details_input_method ON ei_details(input_method);
CREATE INDEX idx_ei_details_deleted_at ON ei_details(deleted_at);

CREATE INDEX idx_eo_details_function_point_id ON eo_details(function_point_id);
CREATE INDEX idx_eo_details_output_name ON eo_details(output_name);
CREATE INDEX idx_eo_details_output_type ON eo_details(output_type);
CREATE INDEX idx_eo_details_generation_frequency ON eo_details(generation_frequency);
CREATE INDEX idx_eo_details_deleted_at ON eo_details(deleted_at);

CREATE INDEX idx_eq_details_function_point_id ON eq_details(function_point_id);
CREATE INDEX idx_eq_details_query_name ON eq_details(query_name);
CREATE INDEX idx_eq_details_query_type ON eq_details(query_type);
CREATE INDEX idx_eq_details_query_frequency ON eq_details(query_frequency);
CREATE INDEX idx_eq_details_deleted_at ON eq_details(deleted_at);

-- 添加约束条件
ALTER TABLE eif_details ADD CONSTRAINT chk_eif_details_interface_type 
    CHECK (interface_type IN ('FILE_TRANSFER', 'API', 'DATABASE', 'MESSAGE_QUEUE', 'WEB_SERVICE', 'BATCH_INTERFACE'));

ALTER TABLE eif_details ADD CONSTRAINT chk_eif_details_data_format 
    CHECK (data_format IN ('XML', 'JSON', 'CSV', 'EXCEL', 'FIXED_WIDTH', 'DELIMITED', 'BINARY', 'DATABASE_RECORDS'));

ALTER TABLE ei_details ADD CONSTRAINT chk_ei_details_input_type 
    CHECK (input_type IN ('SCREEN_INPUT', 'FILE_INPUT', 'API_INPUT', 'BATCH_INPUT', 'INTERFACE_INPUT'));

ALTER TABLE ei_details ADD CONSTRAINT chk_ei_details_input_method 
    CHECK (input_method IN ('MANUAL_ENTRY', 'FILE_UPLOAD', 'API_CALL', 'BATCH_PROCESS', 'REAL_TIME_FEED'));

ALTER TABLE eo_details ADD CONSTRAINT chk_eo_details_output_type 
    CHECK (output_type IN ('REPORT', 'FILE', 'SCREEN_DISPLAY', 'API_RESPONSE', 'MESSAGE', 'EMAIL'));

ALTER TABLE eo_details ADD CONSTRAINT chk_eo_details_output_format 
    CHECK (output_format IN ('PDF', 'HTML', 'XML', 'JSON', 'CSV', 'EXCEL', 'TEXT', 'BINARY'));

ALTER TABLE eq_details ADD CONSTRAINT chk_eq_details_query_type 
    CHECK (query_type IN ('SIMPLE_QUERY', 'COMPLEX_QUERY', 'REPORT_QUERY', 'LOOKUP', 'SEARCH', 'BROWSE'));

ALTER TABLE eq_details ADD CONSTRAINT chk_eq_details_query_method 
    CHECK (query_method IN ('SCREEN_QUERY', 'API_QUERY', 'REPORT_PARAMETER', 'BATCH_QUERY'));

-- 创建功能点详细信息视图（便于查询）
CREATE OR REPLACE VIEW function_points_with_details AS
SELECT 
    fp.id,
    fp.project_id,
    fp.fp_type,
    fp.fp_name,
    fp.fp_description,
    fp.complexity_level,
    fp.complexity_weight,
    fp.calculated_fp_value,
    fp.status,
    fp.created_at,
    fp.updated_at,
    p.name as project_name,
    CASE 
        WHEN fp.fp_type = 'ILF' THEN 
            jsonb_build_object(
                'file_name', ilf.file_name,
                'logical_records_count', ilf.logical_records_count,
                'data_element_types_count', ilf.data_element_types_count,
                'is_master_file', ilf.is_master_file,
                'update_frequency', ilf.update_frequency
            )
        WHEN fp.fp_type = 'EIF' THEN 
            jsonb_build_object(
                'interface_name', eif.interface_name,
                'external_system_name', eif.external_system_name,
                'interface_type', eif.interface_type,
                'data_format', eif.data_format,
                'is_real_time', eif.is_real_time
            )
        WHEN fp.fp_type = 'EI' THEN 
            jsonb_build_object(
                'input_name', ei.input_name,
                'input_type', ei.input_type,
                'input_method', ei.input_method,
                'data_element_types_count', ei.data_element_types_count,
                'is_batch_processing', ei.is_batch_processing
            )
        WHEN fp.fp_type = 'EO' THEN 
            jsonb_build_object(
                'output_name', eo.output_name,
                'output_type', eo.output_type,
                'output_format', eo.output_format,
                'generation_frequency', eo.generation_frequency,
                'is_summary_report', eo.is_summary_report
            )
        WHEN fp.fp_type = 'EQ' THEN 
            jsonb_build_object(
                'query_name', eq.query_name,
                'query_type', eq.query_type,
                'query_method', eq.query_method,
                'query_frequency', eq.query_frequency,
                'supports_pagination', eq.supports_pagination
            )
        ELSE '{}'::jsonb
    END as detail_info
FROM function_points fp
JOIN projects p ON fp.project_id = p.id
LEFT JOIN ilf_details ilf ON fp.id = ilf.function_point_id AND fp.fp_type = 'ILF'
LEFT JOIN eif_details eif ON fp.id = eif.function_point_id AND fp.fp_type = 'EIF'
LEFT JOIN ei_details ei ON fp.id = ei.function_point_id AND fp.fp_type = 'EI'
LEFT JOIN eo_details eo ON fp.id = eo.function_point_id AND fp.fp_type = 'EO'
LEFT JOIN eq_details eq ON fp.id = eq.function_point_id AND fp.fp_type = 'EQ'
WHERE fp.deleted_at IS NULL
  AND p.deleted = false;

-- 添加表注释
COMMENT ON TABLE ilf_details IS 'ILF（内部逻辑文件）详细信息表';
COMMENT ON TABLE eif_details IS 'EIF（外部接口文件）详细信息表';
COMMENT ON TABLE ei_details IS 'EI（外部输入）详细信息表';
COMMENT ON TABLE eo_details IS 'EO（外部输出）详细信息表';
COMMENT ON TABLE eq_details IS 'EQ（外部查询）详细信息表';

-- 添加重要字段注释
COMMENT ON COLUMN ilf_details.logical_records_count IS '逻辑记录数量（RET）';
COMMENT ON COLUMN ilf_details.data_element_types_count IS '数据元素类型数量（DET）';
COMMENT ON COLUMN ilf_details.is_master_file IS '是否为主数据文件';

COMMENT ON COLUMN eif_details.interface_type IS '接口类型：FILE_TRANSFER, API, DATABASE, MESSAGE_QUEUE, WEB_SERVICE, BATCH_INTERFACE';
COMMENT ON COLUMN eif_details.data_format IS '数据格式：XML, JSON, CSV, EXCEL, FIXED_WIDTH, DELIMITED, BINARY, DATABASE_RECORDS';

COMMENT ON COLUMN ei_details.input_type IS '输入类型：SCREEN_INPUT, FILE_INPUT, API_INPUT, BATCH_INPUT, INTERFACE_INPUT';
COMMENT ON COLUMN ei_details.input_method IS '输入方式：MANUAL_ENTRY, FILE_UPLOAD, API_CALL, BATCH_PROCESS, REAL_TIME_FEED';
COMMENT ON COLUMN ei_details.file_types_referenced_count IS '引用的文件类型数量（FTR）';

COMMENT ON COLUMN eo_details.output_type IS '输出类型：REPORT, FILE, SCREEN_DISPLAY, API_RESPONSE, MESSAGE, EMAIL';
COMMENT ON COLUMN eo_details.output_format IS '输出格式：PDF, HTML, XML, JSON, CSV, EXCEL, TEXT, BINARY';

COMMENT ON COLUMN eq_details.query_type IS '查询类型：SIMPLE_QUERY, COMPLEX_QUERY, REPORT_QUERY, LOOKUP, SEARCH, BROWSE';
COMMENT ON COLUMN eq_details.input_data_element_types_count IS '输入数据元素类型数量';
COMMENT ON COLUMN eq_details.output_data_element_types_count IS '输出数据元素类型数量';

COMMENT ON VIEW function_points_with_details IS '功能点及其详细信息的综合视图';