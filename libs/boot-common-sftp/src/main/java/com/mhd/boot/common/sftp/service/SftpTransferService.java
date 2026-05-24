package com.mhd.boot.common.sftp.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
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
import java.util.List;

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
     * SFTP连接池管理器
     */
    private final SftpPoolManager poolManager;

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
        this.poolManager = poolManager;
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
        // 步骤1：校验本地文件是否存在
        File localFile = new File(localFilePath);
        if (!localFile.exists()) {
            throw new SftpTransferException(
                    SftpTransferException.FILE_NOT_FOUND,
                    "本地文件不存在: " + localFilePath);
        }

        // 步骤2：校验文件大小是否超过限制
        if (localFile.length() > MAX_FILE_SIZE) {
            throw new SftpTransferException(
                    SftpTransferException.FILE_SIZE_EXCEEDED,
                    "文件大小超过限制: " + localFile.length() + " bytes, 最大允许: " + MAX_FILE_SIZE + " bytes");
        }

        // 步骤3：如果未指定远程文件名，使用本地文件名
        if (remoteFileName == null || remoteFileName.isEmpty()) {
            remoteFileName = localFile.getName();
        }

        ChannelSftp channel = null;
        boolean success = false;
        long startTime = System.currentTimeMillis();

        try {
            // 步骤4：从连接池借出一个可用连接
            channel = poolManager.borrow();
            // 步骤5：确保远程目录存在，如果不存在则自动创建
            ensureRemoteDirExists(channel, remoteDir);
            // 步骤6：切换到远程目标目录
            channel.cd(remoteDir);
            // 步骤7：使用流式传输上传文件，避免大文件占用过多内存
            try (FileInputStream fis = new FileInputStream(localFile)) {
                channel.put(fis, remoteFileName);
            }
            success = true;
            long cost = System.currentTimeMillis() - startTime;
            log.info("文件上传成功 | 本地:{} | 远程:{}/{} | 大小:{} | 耗时:{}ms",
                    localFilePath, remoteDir, remoteFileName,
                    formatFileSize(localFile.length()), cost);

        } catch (Exception e) {
            // 步骤8：传输异常时，废弃当前连接，避免坏连接被复用
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "上传文件 " + localFilePath + " 到 " + remoteDir);
        } finally {
            // 步骤9：如果连接正常，归还到连接池
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
        ChannelSftp channel = null;
        boolean success = false;
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
                    long totalBytes = 0;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;
                    }
                }
            }

            success = true;
            long cost = System.currentTimeMillis() - startTime;
            log.info("文件下载成功 | 远程:{} | 本地:{} | 耗时:{}ms",
                    remoteFilePath, localFilePath, cost);

        } catch (Exception e) {
            // 步骤4：异常时废弃连接
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "下载文件 " + remoteFilePath + " 到 " + localFilePath);
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
        ChannelSftp channel = null;

        try {
            channel = poolManager.borrow();
            channel.rm(remoteFilePath);
            log.info("文件删除成功: {}", remoteFilePath);

        } catch (Exception e) {
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "删除文件 " + remoteFilePath);
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

            log.debug("列出目录文件: {}, 共{}个文件", remoteDir, fileNames.size());

        } catch (Exception e) {
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "列出目录 " + remoteDir);
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
            throw SftpTransferException.fromJSchException(e, "检查文件是否存在 " + remoteFilePath);
        } catch (Exception e) {
            if (channel != null) {
                poolManager.invalidateObject(channel);
                channel = null;
            }
            throw SftpTransferException.fromJSchException(
                    new Exception(e), "检查文件是否存在 " + remoteFilePath);
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
        // 步骤2：尝试切换到目标目录，如果成功则说明目录已存在
        try {
            channel.cd(remoteDir);
            return;
        } catch (SftpException e) {
            // 目录不存在，需要创建
        }
        // 步骤3：递归创建目录，逐级检查并创建
        String[] dirs = remoteDir.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            currentPath.append("/").append(dir);
            String path = currentPath.toString();
            try {
                channel.cd(path);
            } catch (SftpException e) {
                // 当前级目录不存在，创建它
                channel.mkdir(path);
                log.debug("创建远程目录: {}", path);
            }
        }
        // 步骤4：最后切换到目标目录
        channel.cd(remoteDir);
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
}

