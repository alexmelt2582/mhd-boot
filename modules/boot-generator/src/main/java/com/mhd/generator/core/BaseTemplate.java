package com.mhd.generator.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
@ToString
@Getter
@Setter
@Builder(toBuilder = true)
public class BaseTemplate {
    /**
     * 是否启用。
     */
    @NotNull(message = "启用状态不能为空")
    private boolean enable;
    /**
     * 日志前缀
     */
    @NotBlank(message = "日志前缀不能为空")
    private String logPrefix;
    /**
     * 文件输出目录
     */
    @NotBlank(message = "输出目录不能为空")
    private String outputDir;
    /**
     * 文件输出名称，不含后缀
     */
    @NotBlank(message = "输出名称不能为空")
    private String outputName;
    /**
     * 文件输出名称后缀
     */
    @NotBlank(message = "文件输出名称后缀不能为空")
    private String outputFileSuffix;
    /**
     * 如果文件存在是否覆盖。
     */
    private boolean overwriteExisting;
    /**
     * 文件生成的模板路径
     * <p>
     * 提供的路径方式：
     * 1. 模板文件的绝对路径
     * 2. 模板文件的相对路径：文件放置在 resources资源路径下，填写路径以 classpath: 开头
     * </p>
     */
    @NotBlank(message = "模板路径不能为空")
    private String templatePath;
    /**
     * 模板文件的参数
     */
    @NotNull(message = "模板参数不能为空")
    private Map<String, Object> paramMap;
}
