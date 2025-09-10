# 🏭 长沙市财政评审中心软件规模评估系统
## 生产环境部署配置方案

### 🎯 部署目标

**总体目标**: 为长沙市财政评审中心软件规模评估系统构建稳定、安全、高性能的生产环境，确保系统7×24小时稳定运行，满足政府级信息化项目的部署要求。

**核心要求**:
- **高可用性**: 系统可用性≥99.5%，年故障时间<43.8小时
- **高性能**: 系统响应时间<2秒，支持50+并发用户
- **高安全性**: 满足政府信息安全等级保护三级要求
- **可扩展性**: 支持用户数量和数据量增长50%以上
- **可维护性**: 支持热部署升级，运维操作自动化

### 🏗️ 系统架构设计

#### 整体架构图
```
┌─────────────────────────────────────────────────────────┐
│                    Internet                             │
│                        │                               │
│                  ┌─────▼─────┐                         │
│                  │   Nginx    │                         │
│                  │Load Balance│                         │
│                  │  & Proxy   │                         │
│                  └─────┬─────┘                         │
│                        │                               │
│              ┌─────────┼─────────┐                     │
│              │         │         │                     │
│         ┌────▼────┐ ┌──▼──┐ ┌────▼────┐                │
│         │Frontend │ │     │ │Frontend │                │
│         │ Vue App │ │     │ │ Vue App │                │
│         │ (主服务器)│ │     │ │(备服务器)│                │
│         └────┬────┘ │     │ └────┬────┘                │
│              │      │     │      │                     │
│              │      │     │      │                     │
│         ┌────▼──────▼──────▼──────▼────┐                │
│         │        Backend API           │                │
│         │     Spring Boot Cluster     │                │
│         │   ┌─────────┐ ┌─────────┐   │                │
│         │   │ Backend │ │ Backend │   │                │
│         │   │Node 1   │ │ Node 2  │   │                │
│         │   └─────────┘ └─────────┘   │                │
│         └────┬──────────────────┬────┘                │
│              │                  │                     │
│    ┌─────────▼────┐    ┌────────▼─────┐               │
│    │ PostgreSQL   │    │     Redis    │               │
│    │   Master     │    │    Cache     │               │
│    │              │    │   Cluster    │               │
│    └─────┬────────┘    └──────────────┘               │
│          │                                            │
│    ┌─────▼────────┐                                   │
│    │ PostgreSQL   │                                   │
│    │   Replica    │                                   │
│    │   (只读)     │                                   │
│    └──────────────┘                                   │
└─────────────────────────────────────────────────────────┘
```

#### 服务器配置规格

**Web服务器 (Nginx)** - 2台
```yaml
服务器规格:
  CPU: 4核心 Intel Xeon处理器
  内存: 8GB DDR4 ECC
  存储: 200GB SSD
  网络: 双千兆网卡，双上联链路
  操作系统: CentOS 8 / Ubuntu 20.04 LTS

软件配置:
  Nginx: 1.22+
  SSL证书: 政府CA颁发的SSL证书
  防火墙: iptables + fail2ban
  监控: Nginx监控模块
```

**应用服务器 (Spring Boot)** - 2台
```yaml
服务器规格:
  CPU: 8核心 Intel Xeon处理器
  内存: 16GB DDR4 ECC
  存储: 500GB SSD
  网络: 双千兆网卡
  操作系统: CentOS 8 / Ubuntu 20.04 LTS

软件环境:
  Java: OpenJDK 17
  Spring Boot: 2.7.18
  Docker: 24.0+
  Docker Compose: 2.20+
  监控: Micrometer + Prometheus
```

**数据库服务器 (PostgreSQL)** - 主从2台
```yaml
主数据库服务器:
  CPU: 12核心 Intel Xeon处理器
  内存: 32GB DDR4 ECC
  存储: 1TB NVMe SSD (数据) + 500GB SSD (日志)
  网络: 万兆网卡
  操作系统: CentOS 8专用数据库版本

从数据库服务器:
  CPU: 8核心 Intel Xeon处理器
  内存: 16GB DDR4 ECC
  存储: 1TB NVMe SSD
  网络: 千兆网卡
  操作系统: CentOS 8专用数据库版本

数据库软件:
  PostgreSQL: 15.4+
  连接池: PgBouncer
  备份: pg_dump + pg_basebackup
  监控: pg_stat_monitor
```

**缓存服务器 (Redis)** - 主从2台
```yaml
缓存服务器规格:
  CPU: 4核心处理器
  内存: 16GB DDR4 (缓存专用)
  存储: 200GB SSD
  网络: 千兆网卡
  操作系统: CentOS 8

Redis配置:
  Redis: 7.0+
  集群模式: Master-Replica
  持久化: RDB + AOF
  监控: Redis Sentinel
```

### 🐳 Docker容器化部署

#### Docker Compose 生产配置

**docker-compose.prod.yml**
```yaml
version: '3.8'

services:
  # Nginx负载均衡和反向代理
  nginx:
    image: nginx:1.22-alpine
    container_name: manday-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
      - ./logs/nginx:/var/log/nginx
    depends_on:
      - backend-1
      - backend-2
    restart: always
    networks:
      - manday-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # 后端应用服务 - 节点1
  backend-1:
    build:
      context: ./src/backend
      dockerfile: Dockerfile.prod
    container_name: manday-backend-1
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-master:5432/manday_assess
      - SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - SPRING_REDIS_HOST=redis-master
      - SPRING_REDIS_PORT=6379
      - SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}
      - SERVER_PORT=8080
      - JVM_OPTS=-Xms2g -Xmx4g -XX:+UseG1GC
    ports:
      - "8081:8080"
    volumes:
      - ./logs/backend-1:/app/logs
      - ./uploads:/app/uploads
    depends_on:
      - postgres-master
      - redis-master
    restart: always
    networks:
      - manday-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # 后端应用服务 - 节点2
  backend-2:
    build:
      context: ./src/backend
      dockerfile: Dockerfile.prod
    container_name: manday-backend-2
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-master:5432/manday_assess
      - SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - SPRING_REDIS_HOST=redis-master
      - SPRING_REDIS_PORT=6379
      - SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}
      - SERVER_PORT=8080
      - JVM_OPTS=-Xms2g -Xmx4g -XX:+UseG1GC
    ports:
      - "8082:8080"
    volumes:
      - ./logs/backend-2:/app/logs
      - ./uploads:/app/uploads
    depends_on:
      - postgres-master
      - redis-master
    restart: always
    networks:
      - manday-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # PostgreSQL主数据库
  postgres-master:
    image: postgres:15.4
    container_name: manday-postgres-master
    environment:
      - POSTGRES_DB=manday_assess
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
      - POSTGRES_INITDB_ARGS="--encoding=UTF8 --locale=C"
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init:/docker-entrypoint-initdb.d
      - ./logs/postgres:/var/log/postgresql
      - ./database/postgresql.conf:/etc/postgresql/postgresql.conf
    command: postgres -c config_file=/etc/postgresql/postgresql.conf
    restart: always
    networks:
      - manday-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME} -d manday_assess"]
      interval: 30s
      timeout: 10s
      retries: 3

  # PostgreSQL从数据库 (只读)
  postgres-replica:
    image: postgres:15.4
    container_name: manday-postgres-replica
    environment:
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
      - PGUSER=${DB_USERNAME}
      - POSTGRES_DB=manday_assess
      - POSTGRES_MASTER_SERVICE=postgres-master
    ports:
      - "5433:5432"
    volumes:
      - postgres_replica_data:/var/lib/postgresql/data
      - ./logs/postgres-replica:/var/log/postgresql
    depends_on:
      - postgres-master
    restart: always
    networks:
      - manday-network

  # Redis主缓存
  redis-master:
    image: redis:7.0-alpine
    container_name: manday-redis-master
    ports:
      - "6379:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes
    volumes:
      - redis_data:/data
      - ./logs/redis:/var/log/redis
    restart: always
    networks:
      - manday-network
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Redis从缓存
  redis-replica:
    image: redis:7.0-alpine
    container_name: manday-redis-replica
    ports:
      - "6380:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD} --replicaof redis-master 6379 --appendonly yes
    volumes:
      - redis_replica_data:/data
    depends_on:
      - redis-master
    restart: always
    networks:
      - manday-network

  # 前端静态文件服务
  frontend:
    build:
      context: ./src/frontend
      dockerfile: Dockerfile.prod
    container_name: manday-frontend
    ports:
      - "3000:80"
    volumes:
      - ./logs/frontend:/var/log/nginx
    restart: always
    networks:
      - manday-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Prometheus监控
  prometheus:
    image: prom/prometheus:latest
    container_name: manday-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    restart: always
    networks:
      - manday-network

  # Grafana可视化
  grafana:
    image: grafana/grafana:latest
    container_name: manday-grafana
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
    volumes:
      - grafana_data:/var/lib/grafana
    restart: always
    networks:
      - manday-network

volumes:
  postgres_data:
    driver: local
  postgres_replica_data:
    driver: local
  redis_data:
    driver: local
  redis_replica_data:
    driver: local
  prometheus_data:
    driver: local
  grafana_data:
    driver: local

networks:
  manday-network:
    driver: bridge
```

#### Nginx负载均衡配置

**nginx.conf**
```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 2048;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    # 日志格式
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                   '$status $body_bytes_sent "$http_referer" '
                   '"$http_user_agent" "$http_x_forwarded_for" '
                   'rt=$request_time uct="$upstream_connect_time" '
                   'uht="$upstream_header_time" urt="$upstream_response_time"';
    
    access_log /var/log/nginx/access.log main;
    
    # 基础设置
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    client_max_body_size 50M;
    
    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;
    
    # 后端服务器池
    upstream backend_pool {
        least_conn;
        server backend-1:8080 max_fails=3 fail_timeout=30s;
        server backend-2:8080 max_fails=3 fail_timeout=30s;
        keepalive 32;
    }
    
    # HTTP重定向到HTTPS
    server {
        listen 80;
        server_name manday-assess.changsha.gov.cn;
        return 301 https://$server_name$request_uri;
    }
    
    # HTTPS主服务
    server {
        listen 443 ssl http2;
        server_name manday-assess.changsha.gov.cn;
        
        # SSL配置
        ssl_certificate /etc/nginx/ssl/server.crt;
        ssl_certificate_key /etc/nginx/ssl/server.key;
        ssl_session_cache shared:SSL:10m;
        ssl_session_timeout 10m;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
        ssl_prefer_server_ciphers on;
        
        # 安全头
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
        add_header X-Frame-Options DENY always;
        add_header X-Content-Type-Options nosniff always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Referrer-Policy "strict-origin-when-cross-origin" always;
        
        # 前端静态资源
        location / {
            proxy_pass http://frontend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # 缓存设置
            expires 1d;
            add_header Cache-Control "public, immutable";
        }
        
        # API接口代理
        location /api/ {
            proxy_pass http://backend_pool;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # 超时设置
            proxy_connect_timeout 30s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
            
            # 缓冲设置
            proxy_buffering on;
            proxy_buffer_size 4k;
            proxy_buffers 8 4k;
            proxy_busy_buffers_size 8k;
            
            # 不缓存动态内容
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
        
        # 健康检查
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
        
        # 监控端点
        location /nginx_status {
            stub_status on;
            access_log off;
            allow 127.0.0.1;
            allow 10.0.0.0/8;
            allow 172.16.0.0/12;
            allow 192.168.0.0/16;
            deny all;
        }
    }
}
```

### 🔧 生产环境配置

#### Spring Boot生产配置

**application-prod.yml**
```yaml
# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /
  tomcat:
    threads:
      max: 200
      min-spare: 20
    connection-timeout: 30000
    accept-count: 300
    max-connections: 8192
  compression:
    enabled: true
    mime-types: text/html,text/css,application/json,application/javascript
    min-response-size: 1024

# 数据源配置
spring:
  application:
    name: manday-assess-backend
  profiles:
    active: prod
    
  # 数据库配置
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    url: jdbc:postgresql://postgres-master:5432/manday_assess?useSSL=true&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:manday_user}
    password: ${DB_PASSWORD:}
    driver-class-name: org.postgresql.Driver
    hikari:
      pool-name: MandayHikariPool
      minimum-idle: 10
      maximum-pool-size: 50
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
      
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: validate
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: false
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    properties:
      hibernate:
        format_sql: false
        use_sql_comments: false
        jdbc:
          batch_size: 20
        cache:
          use_second_level_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
            
  # Redis配置
  redis:
    host: redis-master
    port: 6379
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 3000ms
    jedis:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 3000ms
        
  # 缓存配置
  cache:
    type: redis
    redis:
      time-to-live: 3600000
      key-prefix: "manday:"
      use-key-prefix: true
      cache-null-values: false

# 日志配置
logging:
  level:
    root: INFO
    gov.changsha.finance: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
    org.hibernate.type.descriptor.sql.BasicBinder: WARN
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n'
    file: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n'
  file:
    name: /app/logs/manday-assess.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 10GB

# 安全配置
security:
  jwt:
    secret: ${JWT_SECRET:MandayAssessJwtSecretKeyForProductionEnvironment2025}
    expiration: 86400  # 24小时
    refresh-expiration: 604800  # 7天
    
# 监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
      roles: ADMIN
  metrics:
    export:
      prometheus:
        enabled: true
        
# 文件上传配置
file:
  upload:
    path: /app/uploads/
    max-size: 50MB
    allowed-types: .pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg

# 业务配置
manday:
  nesma:
    calculation:
      timeout: 300  # 5分钟超时
      batch-size: 100  # 批量处理大小
    validation:
      strict-mode: true
      auto-correct: false
  audit:
    enabled: true
    retention-days: 365
    
# 性能配置
spring.jpa.properties.hibernate:
  jdbc.batch_size: 20
  order_inserts: true
  order_updates: true
  batch_versioned_data: true
```

#### PostgreSQL生产配置

**postgresql.conf**
```conf
# 基础配置
listen_addresses = '*'
port = 5432
max_connections = 200
shared_preload_libraries = 'pg_stat_statements'

# 内存配置
shared_buffers = 8GB
effective_cache_size = 24GB
maintenance_work_mem = 2GB
checkpoint_completion_target = 0.9
wal_buffers = 256MB
default_statistics_target = 100

# 检查点配置
checkpoint_timeout = 15min
max_wal_size = 4GB
min_wal_size = 1GB

# 日志配置
logging_collector = on
log_directory = '/var/log/postgresql'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_min_duration_statement = 1000
log_line_prefix = '%t [%p]: [%l-1] user=%u,db=%d,app=%a,client=%h '
log_checkpoints = on
log_connections = on
log_disconnections = on
log_lock_waits = on
log_temp_files = 0

# 性能优化
random_page_cost = 1.1  # SSD优化
effective_io_concurrency = 200
work_mem = 64MB
huge_pages = try

# 归档和复制
wal_level = replica
archive_mode = on
archive_command = 'cp %p /var/lib/postgresql/archive/%f'
max_wal_senders = 3
hot_standby = on
```

### 📊 监控和告警系统

#### Prometheus监控配置

**prometheus.yml**
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "/etc/prometheus/alert.rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  # 应用监控
  - job_name: 'manday-backend'
    static_configs:
      - targets: ['backend-1:8080', 'backend-2:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s

  # Nginx监控
  - job_name: 'nginx'
    static_configs:
      - targets: ['nginx:80']
    metrics_path: '/nginx_status'
    scrape_interval: 30s

  # PostgreSQL监控
  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']
    scrape_interval: 30s

  # Redis监控
  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
    scrape_interval: 30s

  # 系统监控
  - job_name: 'node'
    static_configs:
      - targets: ['node-exporter:9100']
    scrape_interval: 30s
```

#### 关键告警规则

**alert.rules.yml**
```yaml
groups:
- name: manday-assess-alerts
  rules:
  # 应用服务告警
  - alert: ApplicationDown
    expr: up{job="manday-backend"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "应用服务下线"
      description: "{{ $labels.instance }} 应用服务已下线超过1分钟"

  # 响应时间告警
  - alert: HighResponseTime
    expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 2
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "响应时间过高"
      description: "95%请求响应时间超过2秒，持续5分钟"

  # CPU使用率告警
  - alert: HighCpuUsage
    expr: 100 - (avg by(instance) (irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "CPU使用率过高"
      description: "{{ $labels.instance }} CPU使用率超过80%，持续10分钟"

  # 内存使用率告警
  - alert: HighMemoryUsage
    expr: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100 > 85
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "内存使用率过高"
      description: "{{ $labels.instance }} 内存使用率超过85%，持续10分钟"

  # 数据库连接告警
  - alert: DatabaseConnectionHigh
    expr: pg_stat_activity_count > 150
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "数据库连接数过高"
      description: "PostgreSQL连接数超过150，持续5分钟"

  # 磁盘空间告警
  - alert: DiskSpaceLow
    expr: (node_filesystem_avail_bytes{mountpoint="/"} / node_filesystem_size_bytes{mountpoint="/"}) * 100 < 15
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "磁盘空间不足"
      description: "{{ $labels.instance }} 根分区可用空间低于15%"
```

### 🔄 部署自动化脚本

#### 部署脚本

**deploy-prod.sh**
```bash
#!/bin/bash

# 生产环境部署脚本
# 作者：DevOps Team
# 版本：1.0
# 创建时间：2025-09-09

set -e

# 配置变量
PROJECT_NAME="manday-assess"
DEPLOY_DIR="/opt/${PROJECT_NAME}"
BACKUP_DIR="/opt/backup/${PROJECT_NAME}"
LOG_FILE="/var/log/${PROJECT_NAME}-deploy.log"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a $LOG_FILE
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a $LOG_FILE
}

warning() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a $LOG_FILE
}

# 检查运行权限
check_permissions() {
    if [ "$EUID" -ne 0 ]; then
        error "请使用root权限运行此脚本"
        exit 1
    fi
}

# 检查Docker环境
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    log "Docker环境检查通过"
}

# 创建目录结构
create_directories() {
    log "创建部署目录结构..."
    
    mkdir -p $DEPLOY_DIR/{logs,uploads,database,monitoring,nginx/ssl}
    mkdir -p $BACKUP_DIR/{database,uploads,config}
    
    # 设置权限
    chown -R 1000:1000 $DEPLOY_DIR/uploads
    chown -R 999:999 $DEPLOY_DIR/database
    chmod 755 $DEPLOY_DIR
    chmod 777 $DEPLOY_DIR/logs
    
    log "目录创建完成"
}

# 备份现有数据
backup_existing() {
    if [ -d "$DEPLOY_DIR" ]; then
        log "备份现有数据..."
        
        BACKUP_TIMESTAMP=$(date +%Y%m%d_%H%M%S)
        CURRENT_BACKUP="$BACKUP_DIR/backup_$BACKUP_TIMESTAMP"
        
        mkdir -p $CURRENT_BACKUP
        
        # 备份数据库
        if docker ps | grep -q "manday-postgres-master"; then
            log "备份PostgreSQL数据库..."
            docker exec manday-postgres-master pg_dump -U $DB_USERNAME manday_assess > "$CURRENT_BACKUP/database_backup.sql"
        fi
        
        # 备份上传文件
        if [ -d "$DEPLOY_DIR/uploads" ]; then
            log "备份上传文件..."
            cp -r $DEPLOY_DIR/uploads $CURRENT_BACKUP/
        fi
        
        # 备份配置文件
        if [ -f "$DEPLOY_DIR/.env" ]; then
            cp $DEPLOY_DIR/.env $CURRENT_BACKUP/
        fi
        
        log "数据备份完成：$CURRENT_BACKUP"
    fi
}

# 部署应用
deploy_application() {
    log "开始部署应用..."
    
    cd $DEPLOY_DIR
    
    # 拉取最新镜像
    log "拉取最新Docker镜像..."
    docker-compose -f docker-compose.prod.yml pull
    
    # 停止现有服务
    log "停止现有服务..."
    docker-compose -f docker-compose.prod.yml down --remove-orphans
    
    # 启动新服务
    log "启动新服务..."
    docker-compose -f docker-compose.prod.yml up -d
    
    # 等待服务启动
    log "等待服务启动..."
    sleep 30
    
    # 检查服务状态
    check_services
}

# 检查服务状态
check_services() {
    log "检查服务状态..."
    
    services=("nginx" "backend-1" "backend-2" "postgres-master" "redis-master" "frontend")
    failed_services=()
    
    for service in "${services[@]}"; do
        if docker ps | grep -q "manday-$service"; then
            log "✓ $service 运行正常"
        else
            error "✗ $service 运行异常"
            failed_services+=($service)
        fi
    done
    
    if [ ${#failed_services[@]} -eq 0 ]; then
        log "所有服务运行正常"
        return 0
    else
        error "以下服务运行异常：${failed_services[*]}"
        return 1
    fi
}

# 健康检查
health_check() {
    log "执行健康检查..."
    
    # 检查Nginx
    if curl -f http://localhost/health &> /dev/null; then
        log "✓ Nginx健康检查通过"
    else
        error "✗ Nginx健康检查失败"
        return 1
    fi
    
    # 检查后端API
    if curl -f http://localhost/api/actuator/health &> /dev/null; then
        log "✓ 后端API健康检查通过"
    else
        error "✗ 后端API健康检查失败"
        return 1
    fi
    
    # 检查数据库连接
    if docker exec manday-postgres-master pg_isready -U $DB_USERNAME &> /dev/null; then
        log "✓ 数据库连接检查通过"
    else
        error "✗ 数据库连接检查失败"
        return 1
    fi
    
    log "所有健康检查通过"
}

# 数据库初始化
init_database() {
    log "初始化数据库..."
    
    # 等待数据库启动
    while ! docker exec manday-postgres-master pg_isready -U $DB_USERNAME &> /dev/null; do
        log "等待数据库启动..."
        sleep 5
    done
    
    # 执行数据库脚本
    if [ -f "$DEPLOY_DIR/database/init/init.sql" ]; then
        log "执行数据库初始化脚本..."
        docker exec -i manday-postgres-master psql -U $DB_USERNAME -d manday_assess < $DEPLOY_DIR/database/init/init.sql
    fi
    
    log "数据库初始化完成"
}

# 性能测试
performance_test() {
    log "执行性能测试..."
    
    # 简单的负载测试
    ab -n 1000 -c 10 http://localhost/api/actuator/health > /tmp/performance_test.log 2>&1
    
    if [ $? -eq 0 ]; then
        log "✓ 性能测试通过"
        grep "Requests per second" /tmp/performance_test.log | tee -a $LOG_FILE
    else
        warning "性能测试失败，请检查详细日志"
    fi
}

# 清理旧数据
cleanup() {
    log "清理旧数据..."
    
    # 清理旧的Docker镜像
    docker image prune -f
    
    # 清理旧的日志文件（保留30天）
    find $DEPLOY_DIR/logs -type f -mtime +30 -name "*.log" -delete
    
    # 清理旧的备份文件（保留90天）
    find $BACKUP_DIR -type d -mtime +90 -name "backup_*" -exec rm -rf {} \;
    
    log "清理完成"
}

# 部署后配置
post_deploy_config() {
    log "执行部署后配置..."
    
    # 设置防火墙规则
    if command -v ufw &> /dev/null; then
        ufw allow 80/tcp
        ufw allow 443/tcp
        ufw allow 22/tcp
        ufw --force enable
        log "防火墙配置完成"
    fi
    
    # 设置系统服务自启动
    systemctl enable docker
    log "Docker服务设置为开机自启动"
    
    # 设置日志轮转
    cat > /etc/logrotate.d/manday-assess << 'EOF'
/opt/manday-assess/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 root root
}
EOF
    log "日志轮转配置完成"
}

# 主函数
main() {
    log "开始${PROJECT_NAME}生产环境部署..."
    
    # 检查环境文件
    if [ ! -f ".env" ]; then
        error "缺少.env环境配置文件"
        exit 1
    fi
    
    # 加载环境变量
    source .env
    
    # 执行部署步骤
    check_permissions
    check_docker
    create_directories
    backup_existing
    deploy_application
    init_database
    
    if check_services && health_check; then
        log "✓ 部署成功完成"
        performance_test
        cleanup
        post_deploy_config
        
        log "🎉 ${PROJECT_NAME}生产环境部署完成！"
        log "访问地址：https://manday-assess.changsha.gov.cn"
        log "监控地址：https://manday-assess.changsha.gov.cn:3001 (Grafana)"
    else
        error "❌ 部署失败，请检查日志"
        exit 1
    fi
}

# 执行主函数
main "$@"
```

#### 环境变量配置文件

**.env**
```env
# 数据库配置
DB_USERNAME=manday_user
DB_PASSWORD=MandayStrongPassword2025!
DB_NAME=manday_assess

# Redis配置
REDIS_PASSWORD=RedisStrongPassword2025!

# JWT配置
JWT_SECRET=MandayAssessJwtSecretKeyForProductionEnvironment2025ChangshaFinance

# Grafana配置
GRAFANA_PASSWORD=GrafanaAdminPassword2025!

# SSL证书路径
SSL_CERT_PATH=/opt/manday-assess/nginx/ssl/server.crt
SSL_KEY_PATH=/opt/manday-assess/nginx/ssl/server.key

# 域名配置
DOMAIN_NAME=manday-assess.changsha.gov.cn

# 邮件告警配置
SMTP_HOST=mail.changsha.gov.cn
SMTP_PORT=587
SMTP_USER=manday-alerts@changsha.gov.cn
SMTP_PASSWORD=MailPassword2025!

# 备份配置
BACKUP_RETENTION_DAYS=90
DATABASE_BACKUP_SCHEDULE="0 2 * * *"  # 每天凌晨2点备份

# 性能配置
MAX_CONNECTIONS=200
CONNECTION_POOL_SIZE=50
CACHE_TTL=3600

# 安全配置
ENABLE_AUDIT_LOG=true
SESSION_TIMEOUT=1800  # 30分钟
PASSWORD_POLICY=complex
```

### 📋 部署检查清单

#### 部署前检查 (Pre-Deployment)
- [ ] 服务器硬件配置达标
- [ ] 操作系统版本兼容
- [ ] Docker和Docker Compose已安装
- [ ] 网络防火墙配置正确
- [ ] SSL证书已准备并有效
- [ ] 域名DNS解析正确配置
- [ ] 数据库备份策略确定
- [ ] 监控告警配置完成

#### 部署过程检查 (During Deployment)
- [ ] 所有Docker容器正常启动
- [ ] 数据库连接测试通过
- [ ] Redis缓存连接正常
- [ ] Nginx负载均衡工作正常
- [ ] HTTPS证书配置正确
- [ ] 静态资源访问正常
- [ ] API接口响应正常

#### 部署后验证 (Post-Deployment)
- [ ] 系统登录功能正常
- [ ] 项目CRUD操作正常
- [ ] NESMA计算功能正常
- [ ] 报告生成功能正常
- [ ] 文件上传下载正常
- [ ] 性能指标达标（响应时间<2s）
- [ ] 监控图表数据正常
- [ ] 告警规则触发正常
- [ ] 日志记录完整
- [ ] 备份恢复流程验证

#### 安全检查 (Security)
- [ ] SQL注入漏洞扫描通过
- [ ] XSS攻击防护有效
- [ ] CSRF保护机制启用
- [ ] 文件上传安全检查
- [ ] 敏感信息加密存储
- [ ] API访问频率限制
- [ ] 用户权限控制正确
- [ ] 审计日志记录完整

### 🔄 回滚计划

#### 自动回滚条件
- 健康检查失败超过5分钟
- 错误率超过5%
- 响应时间超过5秒
- 数据库连接失败

#### 手动回滚步骤
```bash
# 1. 停止当前服务
docker-compose -f docker-compose.prod.yml down

# 2. 回滚到上一个版本
docker-compose -f docker-compose.prod.yml.backup up -d

# 3. 恢复数据库备份（如需要）
docker exec -i manday-postgres-master psql -U $DB_USERNAME -d manday_assess < backup_latest.sql

# 4. 验证系统功能
./scripts/health-check.sh

# 5. 通知相关人员
echo "系统已回滚到上一个稳定版本" | mail -s "紧急回滚通知" admin@changsha.gov.cn
```

---

**文档编制**: Product Owner  
**技术审核**: Developer Engineer  
**运维审核**: DevOps Engineer  
**安全审核**: Security Specialist  
**版本**: V1.0  
**创建时间**: 2025-09-09  

**重要提醒**: 生产环境部署事关重大，所有操作必须严格按照本配置方案执行，确保系统稳定可靠！