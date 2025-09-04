#!/bin/bash
set -e

# 创建开发环境数据库
echo "创建开发环境数据库..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- 设置数据库时区
    SET timezone = 'Asia/Shanghai';
    
    -- 创建扩展
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    CREATE EXTENSION IF NOT EXISTS "pg_trgm";
    
    -- 显示数据库信息
    SELECT version();
    SELECT current_database(), current_user, current_timestamp;
    
    -- 显示已安装的扩展
    SELECT name, default_version, installed_version 
    FROM pg_available_extensions 
    WHERE installed_version IS NOT NULL;
EOSQL

echo "数据库初始化完成!"