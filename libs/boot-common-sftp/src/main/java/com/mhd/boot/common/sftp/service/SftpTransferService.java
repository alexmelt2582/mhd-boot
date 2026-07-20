package com.mhd.boot.common.sftp.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.mhd.boot.common.sftp.exception.SftpTransferException;
import com.mhd.boot.common.sftp.pool.SftpPoolManager;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SFTP文件传输服务
 * 提供文件上传、下载、删除、列表查询等核心功能
 * 所有操作都通过连接池获取连接，使用完毕后归还连接
 *
 * @author zhao-hao-dong
 **/
@Slf4j
public class SftpTransferService {
    /**
     * 默认最大重试次数（不含首次尝试）
     */
    private static final int DEFAULT_MAX_RETRIES = 2;

    /**
     * 默认重试间隔（毫秒）
     */
    private static final long DEFAULT_RETRY_INTERVAL_MILLIS = 1000L;

    /**
     * SFTP连接池管理器
     */
    private final SftpPoolManager poolManager;

    /**
     * 最大重试次数（不含首次尝试）
     */
    private final int maxRetries;

    /**
     * 重试间隔（毫秒）
     */
    private final long retryIntervalMillis;

    /**
     * 传输缓冲区大小，8KB
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 文件大小限制，默认2GB
     */
    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024 * 1024;

    /**
     * 构造SFTP传输服务
     *
     * @param poolManager 连接池管理器
     */
    public SftpTransferService(SftpPoolManager poolManager) {
        this(poolManager, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL_MILLIS);
    }

    /**
     * 构造SFTP传输服务（带重试配置）
     *
     * @param poolManager         连接池管理器
     * @param maxRetries          最大重试次数（不含首次）
     * @param retryIntervalMillis 重试间隔（毫秒）
     */
    public SftpTransferService(SftpPoolManager poolManager, int maxRetries, long retryIntervalMillis) {
        this.poolManager = poolManager;
        this.maxRetries = Math.max(0, maxRetries);
        this.retryIntervalMillis = Math.max(0L, retryIntervalMillis);

        // 在服务构造阶段统一打印完整参数，包含连接参数和重试参数
        logStartupConfiguration();
    }

    /**
     * 输出服务启动参数摘要
     * 包含连接池配置、连接参数与重试参数，便于快速确认生效配置
     */
    private void logStartupConfiguration() {
        // 汇总并输出服务级配置，帮助排查线上参数问题
        log.info("{}\n- maxRetries: {}{}\n- retryIntervalMillis: {}{}",
                poolManager.getConfigSummary(),
                maxRetries,
                maxRetries == DEFAULT_MAX_RETRIES ? " (default)" : " (custom)",
                retryIntervalMillis,
                retryIntervalMillis == DEFAULT_RETRY_INTERVAL_MILLIS ? " (default)" : " (custom)");
    }

    /**
     * 上传本地文件到SFTP服务器
     * 支持自动创建远程目录，支持大文件流式传输
     *
     * @param localFilePath  本地文件绝对路径
     * @param remoteDir      远程目标目录
     * @param remoteFileName 远程文件名，如果为null则使用本地文件名
     * @throws SftpTransferException 上传过程中发生异常
     */
    public void upload(String localFilePath, String remoteDir, String remoteFileName)
            throws SftpTransferException {
        // 将上传操作交给统一重试执行器
        executeWithRetry("upload file " + localFilePath, () -> {
            uploadInternal(localFilePath, remoteDir, remoteFileName, null);
            return null;
        });
    }

    /**
     * 上传本地文件到SFTP服务器（支持进度监控）
     *
     * @param localFilePath   本地文件绝对路径
     * @param remoteDir       远程目标目录
     * @param remoteFileName  远程文件名（不包含目录），如果为null则使用本地文件名
     * @param progressMonitor 进度监控器，允许为null
     * @throws SftpTransferException 上传过程中发生异常
     */
    public void upload(String localFilePath, String remoteDir, String remoteFileName,
                       SftpProgressMonitor progressMonitor) throws SftpTransferException {
        // 将带进度回调的上传操作交给统一重试执行器
        executeWithRetry("upload file with progress " + localFilePath, () -> {
            uploadInternal(localFilePath, remoteDir, remoteFileName, progressMonitor);
            return null;
        });
    }

    /**
     * 上传实现
     *
     * @param localFilePath   本地文件绝对路径
     * @param remoteDir       远程目录
     * @param remoteFileName  远程文件名（不包含目录），如果为null则使用本地文件名
     * @param progressMonitor 进度监控器
     * @throws SftpTransferException 上传过程中发生异常
     */
    private void uploadInternal(String localFilePath, String remoteDir, String remoteFileName,
                                SftpProgressMonitor progressMonitor) throws SftpTransferException {
        // 步骤1：校验本地文件是否存在
        File localFile = new File(localFilePath);
        if (!localFile.exists()) {
            throw new SftpTransferException(
                    SftpTransferException.FILE_NOT_FOUND,
                    "Local file does not exist: " + localFilePath);
        }

        // 步骤2：校验文件大小是否超过限制
        if (localFile.length() > MAX_FILE_SIZE) {
            throw new SftpTransferException(
                    SftpTransferException.FILE_SIZE_EXCEEDED,
                    "File size exceeds limit: " + localFile.length() + " bytes, max allowed: " + MAX_FILE_SIZE + " bytes");
        }

        // 步骤3：如果未指定远程文件名，使用本地文件名
        if (remoteFileName == null || remoteFileName.isEmpty()) {
            remoteFileName = localFile.getName();
        }

        ChannelSftp channel = null;
        long startTime = System.currentTimeMillis();

        try {
            // 步骤4：从连接池借出一个可用连接
            channel = poolManager.borrow();
            // 获取绝对路径
            String absoluteRemotePath;
            if (remoteDir.endsWith("/")) {
                absoluteRemotePath = remoteDir + remoteFileName;
            } else {
                absoluteRemotePath = remoteDir + "/" + remoteFileName;
            }
            // 步骤5：确保远程目录存在，如果不存在则自动创建
            ensureRemoteDirExists(channel, remoteDir);
            // 步骤6：使用流式传输上传文件，避免大文件占用过多内存
            try (FileInputStream fis = new FileInputStream(localFile)) {
                // 可选地启用传输进度回调
                if (progressMonitor == null) {
                    channel.put(fis, absoluteRemotePath);
                } else {
                    channel.put(fis, absoluteRemotePath, progressMonitor, ChannelSftp.OVERWRITE);
                }
            }
            long cost = System.currentTimeMillis() - startTime;
            log.info("File uploaded successfully. Local: {} -> Remote: {}, Size: {}, Time: {}ms",
                    localFilePath, absoluteRemotePath,
                    formatFileSize(localFile.length()), cost);

        } catch (Exception e) {
            // 步骤7：传输异常时，废弃当前连接，避免坏连接被复用
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "upload file " + localFilePath + " to " + remoteDir);
        } finally {
            // 步骤8：如果连接正常，归还到连接池
            if (channel != null) {
                poolManager.returnObject(channel);
            }
        }
    }

    /**
     * 从SFTP服务器下载文件到本地
     * 支持自动创建本地目录
     *
     * @param remoteFilePath 远程文件完整路径
     * @param localFilePath  本地保存路径
     * @throws SftpTransferException 下载过程中发生异常
     */
    public void download(String remoteFilePath, String localFilePath)
            throws SftpTransferException {
        // 将下载操作交给统一重试执行器
        executeWithRetry("download file " + remoteFilePath, () -> {
            downloadInternal(remoteFilePath, localFilePath);
            return null;
        });
    }

    /**
     * 下载实现
     *
     * @param remoteFilePath 远程文件路径
     * @param localFilePath  本地文件路径
     * @throws SftpTransferException 下载过程中发生异常
     */
    private void downloadInternal(String remoteFilePath, String localFilePath)
            throws SftpTransferException {
        ChannelSftp channel = null;
        long startTime = System.currentTimeMillis();

        try {
            // 步骤1：从连接池借出连接
            channel = poolManager.borrow();

            // 步骤2：确保本地目录存在，自动创建
            Path localPath = Paths.get(localFilePath);
            Files.createDirectories(localPath.getParent());

            // 步骤3：使用流式下载，逐块写入本地文件
            try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
                // 使用get方法的InputStream版本，支持流式传输
                try (InputStream is = channel.get(remoteFilePath)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }

            long cost = System.currentTimeMillis() - startTime;
            log.info("File downloaded successfully. Remote: {} -> Local: {}, Time: {}ms",
                    remoteFilePath, localFilePath, cost);

        } catch (Exception e) {
            // 步骤4：异常时废弃连接
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "download file " + remoteFilePath + " to " + localFilePath);
        } finally {
            // 步骤5：正常归还连接
            if (channel != null) {
                poolManager.returnObject(channel);
            }
        }
    }

    /**
     * 删除SFTP服务器上的文件
     *
     * @param remoteFilePath 远程文件完整路径
     * @throws SftpTransferException 删除过程中发生异常
     */
    public void delete(String remoteFilePath) throws SftpTransferException {
        // 将删除操作交给统一重试执行器
        executeWithRetry("delete file " + remoteFilePath, () -> {
            deleteInternal(remoteFilePath);
            return null;
        });
    }

    /**
     * 删除实现
     *
     * @param remoteFilePath 远程文件路径
     * @throws SftpTransferException 删除过程中发生异常
     */
    private void deleteInternal(String remoteFilePath) throws SftpTransferException {
        ChannelSftp channel = null;

        try {
            channel = poolManager.borrow();
            channel.rm(remoteFilePath);
            log.debug("File deleted successfully: {}", remoteFilePath);

        } catch (Exception e) {
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "delete file " + remoteFilePath);
        } finally {
            if (channel != null) {
                poolManager.returnObject(channel);
            }
        }
    }

    /**
     * 列出远程目录下的所有文件
     *
     * @param remoteDir 远程目录路径
     * @return 文件名列表
     * @throws SftpTransferException 查询过程中发生异常
     */
    @SuppressWarnings("unchecked")
    public List<String> listFiles(String remoteDir) throws SftpTransferException {
        // 将目录列举交给统一重试执行器
        return executeWithRetry("list files in directory " + remoteDir, () -> listFilesInternal(remoteDir));
    }

    /**
     * 列目录实现
     *
     * @param remoteDir 远程目录
     * @return 文件名列表
     * @throws SftpTransferException 列举过程中发生异常
     */
    private List<String> listFilesInternal(String remoteDir) throws SftpTransferException {
        ChannelSftp channel = null;
        List<String> fileNames = new ArrayList<>();

        try {
            channel = poolManager.borrow();

            // 遍历目录下的所有文件条目
            List<ChannelSftp.LsEntry> entries = channel.ls(remoteDir);
            for (ChannelSftp.LsEntry entry : entries) {
                String filename = entry.getFilename();
                // 过滤掉当前目录和上级目录
                if (!".".equals(filename) && !"..".equals(filename)) {
                    fileNames.add(filename);
                }
            }

            log.debug("Listed files in directory: {}, total {} files", remoteDir, fileNames.size());

        } catch (Exception e) {
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "list files in directory " + remoteDir);
        } finally {
            if (channel != null) {
                poolManager.returnObject(channel);
            }
        }

        return fileNames;
    }

    /**
     * 检查远程文件是否存在
     *
     * @param remoteFilePath 远程文件完整路径
     * @return true表示文件存在，false表示不存在
     * @throws SftpTransferException 检查过程中发生异常
     */
    public boolean fileExists(String remoteFilePath) throws SftpTransferException {
        // 将存在性检查交给统一重试执行器
        return executeWithRetry("check file existence " + remoteFilePath, () -> fileExistsInternal(remoteFilePath));
    }

    /**
     * 检查文件存在实现
     *
     * @param remoteFilePath 远程文件路径
     * @return true存在，false不存在
     * @throws SftpTransferException 检查过程中发生异常
     */
    private boolean fileExistsInternal(String remoteFilePath) throws SftpTransferException {
        ChannelSftp channel = null;

        try {
            channel = poolManager.borrow();
            channel.stat(remoteFilePath);
            return true;

        } catch (SftpException e) {
            // 文件不存在时，JSch会抛出SSH_FX_NO_SUCH_FILE异常
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
            throw SftpTransferException.fromJSchException(e, "check file existence " + remoteFilePath);
        } catch (Exception e) {
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "check file existence " + remoteFilePath);
        } finally {
            if (channel != null) {
                poolManager.returnObject(channel);
            }
        }
    }

    /**
     * 确保远程目录存在，如果不存在则递归创建
     *
     * @param channel   SFTP通道
     * @param remoteDir 远程目录路径
     * @throws SftpException 操作过程中发生异常
     */
    private void ensureRemoteDirExists(ChannelSftp channel, String remoteDir) throws SftpException {
        // 步骤1：如果目录为空或根目录，直接返回
        if (remoteDir == null || remoteDir.isEmpty() || "/".equals(remoteDir)) {
            return;
        }
        // 步骤2：获取当前目录，用于最后恢复
        String currentDir = channel.pwd();
        try {
            // 2. 使用绝对路径检查，避免依赖当前目录
            SftpATTRS attrs = channel.stat(remoteDir);
            if (attrs.isDir()) {
                return; // 目录存在
            } else {
                throw new SftpException(ChannelSftp.SSH_FX_FAILURE, "Path exists but is not a directory: " + remoteDir);
            }
        } catch (SftpException e) {
            if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                throw e;
            }
            // 目录不存在，开始递归创建
            int pos = remoteDir.lastIndexOf('/');
            if (pos > 0) {
                String parent = remoteDir.substring(0, pos);
                ensureRemoteDirExists(channel, parent); // 递归创建父级
            }
            try {
                channel.mkdir(remoteDir);
                log.info("Remote directory created: {}", remoteDir);
            } catch (SftpException mkdirEx) {
                // 双重检查：可能在并发时被其他线程创建了
                try {
                    SftpATTRS attrs = channel.stat(remoteDir);
                    if (!attrs.isDir()) throw mkdirEx;
                } catch (SftpException ex) {
                    log.error("Failed to create remote directory: {}.", remoteDir, mkdirEx);
                    throw mkdirEx;
                }
            }
        } finally {
            // 关键：恢复原来的工作目录，防止污染
            // 增加 currentDir != null 的判断，防止极端异常下的空指针
            if (currentDir != null) {
                try {
                    channel.cd(currentDir);
                } catch (Exception e) {
                    log.warn("Failed to restore working directory to {}. Current directory may be changed. Connection might be unstable.", currentDir, e);
                }
            }
        }
    }

    /**
     * 格式化文件大小为人类可读的字符串
     *
     * @param bytes 文件字节数
     * @return 格式化后的字符串，如 "1.23 MB"
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 获取连接池当前状态
     *
     * @return 连接池状态信息
     */
    public String getPoolStatus() {
        return poolManager.getPoolStatus();
    }

    /**
     * 关闭连接池，释放所有资源
     */
    public void close() {
        poolManager.close();
    }

    /**
     * 创建远程目录（目录已存在时不会报错）
     *
     * @param remoteDir 远程目录
     * @throws SftpTransferException 创建目录过程中发生异常
     */
    public void mkdir(String remoteDir) throws SftpTransferException {
        // 将建目录操作交给统一重试执行器
        executeWithRetry("create directory " + remoteDir, () -> {
            mkdirInternal(remoteDir);
            return null;
        });
    }

    /**
     * 创建目录实现
     *
     * @param remoteDir 远程目录
     * @throws SftpTransferException 创建过程中发生异常
     */
    private void mkdirInternal(String remoteDir) throws SftpTransferException {
        ChannelSftp channel = null;
        try {
            // 步骤1：借出连接并递归创建目录
            channel = poolManager.borrow();
            ensureRemoteDirExists(channel, remoteDir);
            log.debug("Remote directory created successfully: {}", remoteDir);
        } catch (Exception e) {
            // 步骤2：异常时销毁连接
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(new Exception(e), "create directory " + remoteDir);
        } finally {
            // 步骤3：正常归还连接
            if (channel != null) {
                poolManager.returnObject(channel);
            }
        }
    }

    /**
     * 重命名或移动远程文件
     *
     * @param src 源路径
     * @param dst 目标路径
     * @throws SftpTransferException 重命名过程中发生异常
     */
    public void rename(String src, String dst) throws SftpTransferException {
        // 将重命名操作交给统一重试执行器
        executeWithRetry("rename " + src + " to " + dst, () -> {
            renameInternal(src, dst);
            return null;
        });
    }

    /**
     * 重命名实现
     *
     * @param src 源路径
     * @param dst 目标路径
     * @throws SftpTransferException 重命名过程中发生异常
     */
    private void renameInternal(String src, String dst) throws SftpTransferException {
        ChannelSftp channel = null;
        try {
            // 步骤1：借出连接并执行重命名
            channel = poolManager.borrow();
            channel.rename(src, dst);
            log.debug("Remote rename successful: {} -> {}", src, dst);
        } catch (Exception e) {
            // 步骤2：异常时销毁连接
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(new Exception(e), "rename " + src + " to " + dst);
        } finally {
            // 步骤3：正常归还连接
            if (channel != null) {
                poolManager.returnObject(channel);
            }
        }
    }

    /**
     * 递归删除远程文件或目录
     *
     * @param remotePath 远程路径（文件或目录）
     * @throws SftpTransferException 删除过程中发生异常
     */
    public void deleteRecursively(String remotePath) throws SftpTransferException {
        // 将递归删除操作交给统一重试执行器
        executeWithRetry("delete recursively " + remotePath, () -> {
            deleteRecursivelyInternalEntry(remotePath);
            return null;
        });
    }

    /**
     * 递归删除入口实现
     *
     * @param remotePath 远程路径
     * @throws SftpTransferException 删除过程中发生异常
     */
    private void deleteRecursivelyInternalEntry(String remotePath) throws SftpTransferException {
        ChannelSftp channel = null;
        try {
            // 步骤1：借出连接并执行递归删除
            channel = poolManager.borrow();
            deleteRecursivelyInternal(channel, remotePath);
            log.debug("Recursive delete successful: {}", remotePath);
        } catch (Exception e) {
            // 步骤2：异常时销毁连接
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(new Exception(e), "delete recursively " + remotePath);
        } finally {
            // 步骤3：正常归还连接
            if (channel != null) {
                poolManager.returnObject(channel);
            }
        }
    }

    /**
     * 列出目录文件条目并按条件过滤
     *
     * @param remoteDir 远程目录
     * @param suffix    文件后缀过滤，null表示不过滤
     * @param reg       正则过滤，null表示不过滤
     * @param startTime 起始修改时间（Unix秒），0表示不过滤
     * @param endTime   结束修改时间（Unix秒），0表示不过滤
     * @return 过滤后的文件条目列表
     * @throws SftpTransferException 查询过程中发生异常
     */
    @SuppressWarnings("unchecked")
    public List<ChannelSftp.LsEntry> listFiles(String remoteDir, String suffix, String reg, int startTime, int endTime)
            throws SftpTransferException {
        // 将筛选列举操作交给统一重试执行器
        return executeWithRetry("list filtered files in directory " + remoteDir,
                () -> listFilesInternal(remoteDir, suffix, reg, startTime, endTime));
    }

    /**
     * 筛选列举实现
     *
     * @param remoteDir 远程目录
     * @param suffix    后缀过滤
     * @param reg       正则过滤
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 过滤后的条目
     * @throws SftpTransferException 查询过程中发生异常
     */
    private List<ChannelSftp.LsEntry> listFilesInternal(String remoteDir, String suffix, String reg,
                                                        int startTime, int endTime)
            throws SftpTransferException {
        ChannelSftp channel = null;
        try {
            // 步骤1：借出连接并获取原始目录列表
            channel = poolManager.borrow();
            Pattern pattern = reg == null ? null : Pattern.compile(reg);
            List<ChannelSftp.LsEntry> rawEntries;
            try {
                rawEntries = channel.ls(remoteDir);
            } catch (SftpException e) {
                // 步骤2：目录不存在时返回空列表，与旧实现行为保持一致
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE || "No such file".equals(e.getMessage())) {
                    return new ArrayList<>();
                }
                throw e;
            }

            // 步骤3：执行后缀、正则、时间过滤
            List<ChannelSftp.LsEntry> filtered = new ArrayList<>();
            for (ChannelSftp.LsEntry entry : rawEntries) {
                String filename = entry.getFilename();
                if (".".equals(filename) || "..".equals(filename)) {
                    continue;
                }
                int modifyTime = entry.getAttrs().getMTime();
                if (startTime != 0 && modifyTime <= startTime) {
                    continue;
                }
                if (endTime != 0 && modifyTime >= endTime) {
                    continue;
                }
                if (suffix != null && !filename.endsWith(suffix)) {
                    continue;
                }
                if (pattern != null && !pattern.matcher(filename).find()) {
                    continue;
                }
                filtered.add(entry);
            }
            return filtered;
        } catch (Exception e) {
            // 步骤4：异常时销毁连接
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(new Exception(e), "list filtered files in directory " + remoteDir);
        } finally {
            // 步骤5：正常归还连接
            if (channel != null) {
                poolManager.returnObject(channel);
            }
        }
    }

    /**
     * 带重试执行SFTP操作
     *
     * @param operationDesc 操作描述
     * @param supplier      可重试执行体
     * @param <T>           返回类型
     * @return 执行结果
     * @throws SftpTransferException 操作最终失败时抛出
     */
    private <T> T executeWithRetry(String operationDesc, RetryableSupplier<T> supplier)
            throws SftpTransferException {
        // 首次尝试 + maxRetries 次重试
        int totalAttempts = maxRetries + 1;
        SftpTransferException lastException = null;

        for (int attempt = 1; attempt <= totalAttempts; attempt++) {
            try {
                // 每次循环都执行一次完整操作
                return supplier.get();
            } catch (SftpTransferException e) {
                lastException = e;
                // 仅在可重试错误且未达到最大尝试次数时继续
                if (attempt >= totalAttempts || !isRetryableException(e)) {
                    throw e;
                }
                log.warn("{} failed, retry {}/{} will start. errorCode={}, reason={}",
                        operationDesc, attempt, totalAttempts - 1, e.getErrorCode(), e.getMessage());
                sleepBeforeRetry(operationDesc);
            }
        }

        throw lastException == null
                ? new SftpTransferException(SftpTransferException.TRANSFER_FAILED, operationDesc + " failed")
                : lastException;
    }

    /**
     * 判断异常是否可重试
     *
     * @param e 传输异常
     * @return true可重试，false不可重试
     */
    private boolean isRetryableException(SftpTransferException e) {
        // 文件不存在、路径错误、权限问题、文件过大这类业务性错误不应重试
        String errorCode = e.getErrorCode();
        if (SftpTransferException.FILE_NOT_FOUND.equals(errorCode)
                || SftpTransferException.FILE_SIZE_EXCEEDED.equals(errorCode)
                || SftpTransferException.PATH_ERROR.equals(errorCode)
                || SftpTransferException.PERMISSION_DENIED.equals(errorCode)
                || SftpTransferException.AUTH_FAILED.equals(errorCode)) {
            return false;
        }

        // 网络抖动、连接中断、连接池暂不可用等问题允许重试
        if (SftpTransferException.CONNECTION_FAILED.equals(errorCode)
                || SftpTransferException.POOL_EXHAUSTED.equals(errorCode)) {
            return true;
        }

        // 对TRANSFER_FAILED再做关键字判断，避免误重试不可恢复错误
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        return message.contains("timeout")
                || message.contains("timed out")
                || message.contains("socket")
                || message.contains("connection")
                || message.contains("channel")
                || message.contains("session");
    }

    /**
     * 重试前等待
     *
     * @param operationDesc 操作描述
     * @throws SftpTransferException 等待被中断时抛出
     */
    private void sleepBeforeRetry(String operationDesc) throws SftpTransferException {
        if (retryIntervalMillis <= 0L) {
            return;
        }
        try {
            // 在重试前做固定间隔退避，避免瞬时雪崩
            Thread.sleep(retryIntervalMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SftpTransferException(SftpTransferException.TRANSFER_FAILED,
                    operationDesc + " retry wait interrupted", e);
        }
    }

    /**
     * 可抛异常的重试执行器接口
     *
     * @param <T> 返回类型
     */
    @FunctionalInterface
    private interface RetryableSupplier<T> {
        /**
         * 执行一次操作
         *
         * @return 结果
         * @throws SftpTransferException 操作异常
         */
        T get() throws SftpTransferException;
    }

    /**
     * 按最后修改时间升序排序目录条目
     *
     * @param entries 待排序条目列表
     */
    public void sortByModifyTime(List<ChannelSftp.LsEntry> entries) {
        // 步骤1：使用修改时间做稳定比较，保持与旧实现一致
        entries.sort(new Comparator<ChannelSftp.LsEntry>() {
            @Override
            public int compare(ChannelSftp.LsEntry o1, ChannelSftp.LsEntry o2) {
                return Integer.compare(o1.getAttrs().getMTime(), o2.getAttrs().getMTime());
            }
        });
    }

    /**
     * 在同一连接中递归删除文件或目录
     *
     * @param channel    已连接的SFTP通道
     * @param remotePath 待删除路径
     * @throws SftpException 删除过程中发生异常
     */
    @SuppressWarnings("unchecked")
    private void deleteRecursivelyInternal(ChannelSftp channel, String remotePath) throws SftpException {
        // 步骤1：先尝试按文件删除，成功则直接返回
        try {
            // 获取属性，判断是文件还是目录
            SftpATTRS attrs = channel.stat(remotePath);
            if (attrs.isDir()) {
                // 是目录，遍历删除子项
                @SuppressWarnings("unchecked")
                List<ChannelSftp.LsEntry> children = channel.ls(remotePath);
                for (ChannelSftp.LsEntry child : children) {
                    String filename = child.getFilename();
                    if (".".equals(filename) || "..".equals(filename)) continue;
                    String childPath = remotePath.endsWith("/") ? remotePath + filename : remotePath + "/" + filename;
                    if (child.getAttrs().isDir()) {
                        deleteRecursivelyInternal(channel, childPath);
                    } else {
                        channel.rm(childPath);
                    }
                }
                // 子项清空后，删除空目录
                channel.rmdir(remotePath);
            } else {
                // 是文件，直接删除
                channel.rm(remotePath);
            }
        } catch (SftpException e) {
            // 异常兜底：如果是“文件不存在”，视为删除成功（保证幂等性）
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                log.debug("Target path does not exist and is treated as already deleted: {}", remotePath);
                return;
            }
            // 其他异常（如权限不足、网络中断）继续向上抛出，交由外层统一处理
            throw e;
        }
    }
}

