package com.mhd.generator.util;

import cn.hutool.core.util.StrUtil;
import com.mhd.generator.constant.GlobalConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * @author zhao-hao-dong
 * @since 2025-04-02
 **/
public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class + GlobalConstant.LOG_PREFIX);

    private FileUtils() {
    }

    /**
     * 读取类路径下的资源文件，返回文件内容字符串
     *
     * @param filePath 资源路径（相对于src/main/resources的路径，如"data.txt"或"config/settings.json"）
     * @return 文件内容字符串，文件不存在时返回null
     */
    public static String readResourceAsString(String filePath) {
        // 使用ClassLoader获取资源流
        if (StrUtil.isBlank(filePath)) {
            log.error("文件路径不能为空");
            return null;
        }
        if (filePath.startsWith("\\") || filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        try (InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                log.error("文件未找到: {}", filePath);
                return null; // 或抛出异常 throw new IllegalArgumentException("文件未找到: " + filePath);
            }
            // 使用Scanner高效读取内容
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                return scanner.useDelimiter("\\A").next(); // 一次性读取全部内容
            }
        } catch (IOException e) {
            log.error("读取资源文件失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 读取绝对路径下的资源文件，返回文件内容字符串
     *
     * @param filePath 资源路径（相对于src/main/resources的路径，如"data.txt"或"config/settings.json"）
     * @return 文件内容字符串，文件不存在时返回null
     */
    public static String readAbsolutePathAsString(String filePath) {
        //
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) { // 使用绝对路径读取文件{
            // 使用Scanner高效读取内容
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                return scanner.useDelimiter("\\A").next(); // 一次性读取全部内容
            }
        } catch (IOException e) {
            log.error("读取资源文件失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 从文件路径中提取目录路径和文件名
     *
     * @param filePath 文件路径字符串
     * @return 包含目录路径和文件名的字符串数组
     */
    public static String[] extractFilePathAndName(String filePath) {
        // 将字符串路径转换为 Path 对象
        Path path = Paths.get(filePath);
        // 获取文件名
        Path fileName = path.getFileName();
        String fileNameStr = (fileName != null) ? fileName.toString() : "";
        // 获取目录路径
        Path parentPath = path.getParent();
        String parentPathStr = (parentPath != null) ? parentPath.toString() : "";
        // 返回结果数组
        return new String[]{parentPathStr, fileNameStr};
    }
}