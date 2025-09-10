package gov.changsha.finance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * 数据备份和恢复服务
 * 提供自动化数据备份、加密存储和恢复功能
 * 符合政府级数据安全要求
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Service
public class BackupService {
    
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    @Value("${system.backup.location:/data/backups/manday-assess/}")
    private String backupLocation;
    
    @Value("${system.backup.retention-days:30}")
    private int retentionDays;
    
    @Value("${system.backup.encrypt:true}")
    private boolean encryptBackup;
    
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    
    @Value("${spring.datasource.username}")
    private String databaseUsername;
    
    @Value("${spring.datasource.password}")
    private String databasePassword;
    
    @Value("${system.backup.encryption-key:manday-assess-backup-encryption-key}")
    private String encryptionKey;
    
    @Autowired
    private AuditLogService auditLogService;
    
    /**
     * 定时备份任务 - 每天凌晨2点执行
     */
    @Scheduled(cron = "${system.backup.schedule:0 2 * * *}")
    public void scheduleBackup() {
        logger.info("开始执行定时备份任务...");
        
        try {
            CompletableFuture<Boolean> backupResult = performBackupAsync();
            Boolean success = backupResult.get();
            
            if (success) {
                logger.info("定时备份任务执行成功");
                auditLogService.recordSystemOperation("DATA_BACKUP", "定时备份任务执行成功", null, null);
            } else {
                logger.error("定时备份任务执行失败");
                auditLogService.recordSystemOperation("DATA_BACKUP", "定时备份任务执行失败", null, "备份过程中发生错误");
            }
            
            // 清理过期备份
            cleanupOldBackups();
            
        } catch (Exception e) {
            logger.error("定时备份任务异常", e);
            auditLogService.recordSystemException("定时备份", e);
        }
    }
    
    /**
     * 手动触发备份
     */
    public CompletableFuture<Boolean> performBackupAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return performBackup();
            } catch (Exception e) {
                logger.error("异步备份执行失败", e);
                return false;
            }
        });
    }
    
    /**
     * 执行数据备份
     */
    public boolean performBackup() {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String backupFileName = String.format("manday_assess_backup_%s.sql", timestamp);
        
        try {
            // 确保备份目录存在
            Path backupDir = Paths.get(backupLocation);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }
            
            Path backupFile = backupDir.resolve(backupFileName);
            
            // 执行数据库备份
            boolean success = dumpDatabase(backupFile.toString());
            
            if (success) {
                // 压缩备份文件
                Path compressedFile = compressBackup(backupFile);
                
                // 如果启用加密，则加密备份文件
                if (encryptBackup) {
                    Path encryptedFile = encryptBackup(compressedFile);
                    
                    // 删除未加密的压缩文件
                    Files.deleteIfExists(compressedFile);
                    
                    logger.info("数据备份完成 - 加密文件: {}", encryptedFile.toString());
                } else {
                    logger.info("数据备份完成 - 压缩文件: {}", compressedFile.toString());
                }
                
                // 删除原始SQL文件
                Files.deleteIfExists(backupFile);
                
                return true;
            }
            
        } catch (Exception e) {
            logger.error("数据备份失败", e);
        }
        
        return false;
    }
    
    /**
     * 执行数据库导出
     */
    private boolean dumpDatabase(String backupFilePath) {
        try {
            // 解析数据库连接信息
            String dbHost = extractDbHost(databaseUrl);
            String dbPort = extractDbPort(databaseUrl);
            String dbName = extractDbName(databaseUrl);
            
            // 构建pg_dump命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                "pg_dump",
                "-h", dbHost,
                "-p", dbPort,
                "-U", databaseUsername,
                "-d", dbName,
                "--no-password",
                "--create",
                "--clean",
                "--if-exists",
                "--verbose",
                "-f", backupFilePath
            );
            
            // 设置环境变量
            processBuilder.environment().put("PGPASSWORD", databasePassword);
            
            // 执行命令
            Process process = processBuilder.start();
            
            // 读取输出和错误信息
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                logger.info("数据库导出成功: {}", backupFilePath);
                return true;
            } else {
                logger.error("数据库导出失败 - 退出码: {}, 错误信息: {}", exitCode, error);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("执行数据库导出时异常", e);
            return false;
        }
    }
    
    /**
     * 压缩备份文件
     */
    private Path compressBackup(Path backupFile) throws IOException {
        Path compressedFile = Paths.get(backupFile.toString() + ".gz");
        
        try (FileInputStream fis = new FileInputStream(backupFile.toFile());
             FileOutputStream fos = new FileOutputStream(compressedFile.toFile());
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, bytesRead);
            }
        }
        
        logger.info("备份文件压缩完成: {}", compressedFile.toString());
        return compressedFile;
    }
    
    /**
     * 加密备份文件
     */
    private Path encryptBackup(Path backupFile) throws Exception {
        Path encryptedFile = Paths.get(backupFile.toString() + ".enc");
        
        // 生成密钥
        SecretKey secretKey = new SecretKeySpec(encryptionKey.getBytes(), ENCRYPTION_ALGORITHM);
        
        // 创建加密器
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        // 加密文件
        try (FileInputStream fis = new FileInputStream(backupFile.toFile());
             FileOutputStream fos = new FileOutputStream(encryptedFile.toFile())) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] encryptedBytes = cipher.update(buffer, 0, bytesRead);
                if (encryptedBytes != null) {
                    fos.write(encryptedBytes);
                }
            }
            
            // 写入最终的加密块
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                fos.write(finalBytes);
            }
        }
        
        logger.info("备份文件加密完成: {}", encryptedFile.toString());
        return encryptedFile;
    }
    
    /**
     * 恢复数据库
     */
    public boolean restoreDatabase(String backupFilePath) {
        try {
            Path backupFile = Paths.get(backupFilePath);
            
            if (!Files.exists(backupFile)) {
                logger.error("备份文件不存在: {}", backupFilePath);
                return false;
            }
            
            // 记录恢复操作审计日志
            auditLogService.recordSystemOperation("DATA_RESTORE", 
                "开始数据库恢复操作", "Database", backupFilePath);
            
            Path sqlFile = backupFile;
            
            // 如果是加密文件，先解密
            if (backupFilePath.endsWith(".enc")) {
                sqlFile = decryptBackup(backupFile);
            }
            
            // 如果是压缩文件，先解压
            if (sqlFile.toString().endsWith(".gz")) {
                sqlFile = decompressBackup(sqlFile);
            }
            
            // 执行数据库恢复
            boolean success = restoreDatabaseFromSql(sqlFile.toString());
            
            // 清理临时文件
            if (!sqlFile.equals(backupFile)) {
                Files.deleteIfExists(sqlFile);
            }
            
            if (success) {
                logger.info("数据库恢复成功: {}", backupFilePath);
                auditLogService.recordSystemOperation("DATA_RESTORE", 
                    "数据库恢复操作成功", "Database", backupFilePath);
            } else {
                logger.error("数据库恢复失败: {}", backupFilePath);
                auditLogService.recordSystemOperation("DATA_RESTORE", 
                    "数据库恢复操作失败", "Database", backupFilePath);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("数据库恢复异常: {}", backupFilePath, e);
            auditLogService.recordSystemException("数据库恢复", e);
            return false;
        }
    }
    
    /**
     * 解密备份文件
     */
    private Path decryptBackup(Path encryptedFile) throws Exception {
        Path decryptedFile = Paths.get(encryptedFile.toString().replace(".enc", ""));
        
        // 生成密钥
        SecretKey secretKey = new SecretKeySpec(encryptionKey.getBytes(), ENCRYPTION_ALGORITHM);
        
        // 创建解密器
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        // 解密文件
        try (FileInputStream fis = new FileInputStream(encryptedFile.toFile());
             FileOutputStream fos = new FileOutputStream(decryptedFile.toFile())) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decryptedBytes = cipher.update(buffer, 0, bytesRead);
                if (decryptedBytes != null) {
                    fos.write(decryptedBytes);
                }
            }
            
            // 写入最终的解密块
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                fos.write(finalBytes);
            }
        }
        
        logger.info("备份文件解密完成: {}", decryptedFile.toString());
        return decryptedFile;
    }
    
    /**
     * 解压备份文件
     */
    private Path decompressBackup(Path compressedFile) throws IOException {
        Path decompressedFile = Paths.get(compressedFile.toString().replace(".gz", ""));
        
        try (FileInputStream fis = new FileInputStream(compressedFile.toFile());
             java.util.zip.GZIPInputStream gzipIS = new java.util.zip.GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(decompressedFile.toFile())) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = gzipIS.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        logger.info("备份文件解压完成: {}", decompressedFile.toString());
        return decompressedFile;
    }
    
    /**
     * 从SQL文件恢复数据库
     */
    private boolean restoreDatabaseFromSql(String sqlFilePath) {
        try {
            // 解析数据库连接信息
            String dbHost = extractDbHost(databaseUrl);
            String dbPort = extractDbPort(databaseUrl);
            String dbName = extractDbName(databaseUrl);
            
            // 构建psql命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                "psql",
                "-h", dbHost,
                "-p", dbPort,
                "-U", databaseUsername,
                "-d", dbName,
                "--no-password",
                "-f", sqlFilePath
            );
            
            // 设置环境变量
            processBuilder.environment().put("PGPASSWORD", databasePassword);
            
            // 执行命令
            Process process = processBuilder.start();
            
            // 读取输出和错误信息
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                logger.info("数据库恢复成功: {}", sqlFilePath);
                return true;
            } else {
                logger.error("数据库恢复失败 - 退出码: {}, 错误信息: {}", exitCode, error);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("执行数据库恢复时异常", e);
            return false;
        }
    }
    
    /**
     * 清理过期备份文件
     */
    public void cleanupOldBackups() {
        try {
            Path backupDir = Paths.get(backupLocation);
            if (!Files.exists(backupDir)) {
                return;
            }
            
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
            
            List<Path> oldBackups = Files.list(backupDir)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant()
                                    .isBefore(cutoffTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                        } catch (IOException e) {
                            logger.warn("检查文件修改时间失败: {}", path, e);
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            
            for (Path oldBackup : oldBackups) {
                try {
                    Files.delete(oldBackup);
                    logger.info("删除过期备份文件: {}", oldBackup.toString());
                } catch (IOException e) {
                    logger.warn("删除过期备份文件失败: {}", oldBackup, e);
                }
            }
            
            if (!oldBackups.isEmpty()) {
                logger.info("清理过期备份完成，删除文件数: {}", oldBackups.size());
            }
            
        } catch (Exception e) {
            logger.error("清理过期备份时异常", e);
        }
    }
    
    /**
     * 获取可用备份列表
     */
    public List<BackupInfo> getAvailableBackups() {
        try {
            Path backupDir = Paths.get(backupLocation);
            if (!Files.exists(backupDir)) {
                return Collections.emptyList();
            }
            
            return Files.list(backupDir)
                    .filter(path -> path.getFileName().toString().startsWith("manday_assess_backup_"))
                    .map(path -> {
                        try {
                            BackupInfo info = new BackupInfo();
                            info.setFileName(path.getFileName().toString());
                            info.setFilePath(path.toString());
                            info.setFileSize(Files.size(path));
                            info.setCreatedTime(Files.getLastModifiedTime(path).toInstant()
                                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                            info.setEncrypted(path.toString().endsWith(".enc"));
                            info.setCompressed(path.toString().contains(".gz"));
                            return info;
                        } catch (IOException e) {
                            logger.warn("读取备份文件信息失败: {}", path, e);
                            return null;
                        }
                    })
                    .filter(info -> info != null)
                    .sorted((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("获取可用备份列表异常", e);
            return Collections.emptyList();
        }
    }
    
    // 辅助方法
    
    private String extractDbHost(String url) {
        // jdbc:postgresql://localhost:5432/dbname
        try {
            String[] parts = url.split("//")[1].split(":");
            return parts[0];
        } catch (Exception e) {
            return "localhost";
        }
    }
    
    private String extractDbPort(String url) {
        try {
            String[] parts = url.split("//")[1].split(":");
            if (parts.length > 1) {
                return parts[1].split("/")[0];
            }
            return "5432";
        } catch (Exception e) {
            return "5432";
        }
    }
    
    private String extractDbName(String url) {
        try {
            return url.split("/")[url.split("/").length - 1];
        } catch (Exception e) {
            return "manday_assess";
        }
    }
    
    private String readStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            logger.warn("读取流时异常", e);
            return "";
        }
    }
    
    /**
     * 备份信息类
     */
    public static class BackupInfo {
        private String fileName;
        private String filePath;
        private long fileSize;
        private LocalDateTime createdTime;
        private boolean encrypted;
        private boolean compressed;
        
        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public LocalDateTime getCreatedTime() { return createdTime; }
        public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
        
        public boolean isEncrypted() { return encrypted; }
        public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }
        
        public boolean isCompressed() { return compressed; }
        public void setCompressed(boolean compressed) { this.compressed = compressed; }
    }
}