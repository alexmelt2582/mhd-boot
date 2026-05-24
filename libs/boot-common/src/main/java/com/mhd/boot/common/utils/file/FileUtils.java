package com.mhd.boot.common.utils.file;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * File 工具类
 *
 * @author zhao-hao-dong
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class FileUtils {
    /**
     * 系统用户目录路径
     */
    public static final Path BASE_PATH = Paths.get(System.getProperty("user.dir"));
    /**
     * 文件系统分隔符
     */
    public static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();

    /**
     * 获取不带扩展名的文件名
     *
     * @param filename 文件名字符串
     * @return 不带扩展名的文件名，如果输入为 null 或空则返回原字符串
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (!filename.isEmpty())) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 获取文件扩展名（不包含点号）
     *
     * @param filename 文件名字符串
     * @return 文件扩展名，如果未找到则返回原字符串
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (!filename.isEmpty())) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 验证文件大小是否在允许的范围内
     *
     * @param maxSize 限制的最大大小，单位为 MB
     * @param size    待验证的文件大小，单位为字节
     * @return true 表示文件大小合法（小于等于限制），false 表示超出限制
     */
    public static boolean validSize(long maxSize, long size) {
        // 1M
        int len = 1024 * 1024;
        return size <= (maxSize * len);
    }

    /**
     * 格式化文件大小为可读字符串（B, KB, MB, GB）
     *
     * @param size 文件大小，单位为字节
     * @return 格式化后的大小字符串，保留一位小数
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 读取文件内容为字符串
     *
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException 如果文件不存在或读取失败
     */
    public static String readFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + filePath);
        }
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 将内容写入文件
     * 如果文件不存在会创建，如果存在则覆盖。
     * 如果父目录不存在，会自动创建父目录。
     *
     * @param filePath 文件路径
     * @param content  要写入的字符串内容
     * @throws IOException 写入失败时抛出
     */
    public static void writeFileContent(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        // 确保父目录存在
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        Files.writeString(path, content,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * 复制文件
     * 如果目标路径的父目录不存在，会自动创建。
     * 如果目标文件已存在，会进行覆盖。
     *
     * @param sourceFilePath      源文件路径
     * @param destinationFilePath 目标文件路径
     */
    public static void copyFile(Path sourceFilePath, Path destinationFilePath) {
        try {
            if (Files.notExists(destinationFilePath.getParent())) {
                Files.createDirectories(destinationFilePath.getParent());
                log.debug("路径不存在，已创建: {}", destinationFilePath.getParent());
            }
            Files.copy(sourceFilePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("文件复制失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 删除绝对路径下的文件
     *
     * @param absolutePath 文件的绝对路径
     * @return true 表示删除成功或文件不存在，false 表示删除失败
     */
    public static boolean deleteAbsolutePathFile(Path absolutePath) {
        try {
            Files.deleteIfExists(absolutePath);
            log.info("已删除文件: {}", absolutePath);
            return true;
        } catch (IOException e) {
            log.error("删除文件失败: {}", absolutePath, e);
            return false;
        }
    }


    /**
     * 删除目录及其下的所有文件和子目录
     * 使用文件树遍历器进行深度删除。
     *
     * @param directory 要删除的目录路径
     * @throws IOException 如果删除过程中遇到无法访问的文件或目录
     */
    public static void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    if (!dir.equals(directory)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                throw exc;
            }
        });
    }
}
