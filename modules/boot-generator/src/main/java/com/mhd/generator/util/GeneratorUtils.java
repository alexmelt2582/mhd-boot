package com.mhd.generator.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.mhd.generator.constant.GlobalConstant;
import com.mhd.generator.core.BaseTemplate;
import com.mhd.generator.exception.CodeGeneratorException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 生成器工具类
 *
 * @author zhao-hao-dong
 * @since 2025-04-18
 **/
public class GeneratorUtils {
    private static final Logger log = LoggerFactory.getLogger(GeneratorUtils.class + GlobalConstant.LOG_PREFIX);
    private static final Set<String> customFilterParamList = new HashSet<>();

    private GeneratorUtils() {
    }

    static {
        // 添加自定义过滤参数
        customFilterParamList.add("import");
    }

    /**
     * 添加自定义过滤参数
     *
     * @param param 自定义过滤参数
     */
    public static void addCustomFilterParam(String param) {
        if (StrUtil.isNotBlank(param)) {
            customFilterParamList.add(param);
        }
    }

    public static void getCustomFilterParamList() {
        log.info("自定义过滤参数列表: {}", customFilterParamList);
    }

    public static BaseTemplate applyCustomization(BaseTemplate source, Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        return customizer.apply(source.toBuilder()).build();
    }

    /**
     * 从文本内容中查找${...}中的内容，忽略${r'${...}'}和${r"${...}"}
     *
     * @param content 文本内容
     * @return 参数列表
     */
    public static Set<String> findParams(String content) {
        if (StrUtil.isBlank(content)) {
            return new HashSet<>();
        }
        Set<String> result = new HashSet<>();
        // 定义正则表达式匹配${...}，但排除${r'${...}'}和${r"${...}"}
        Pattern pattern = Pattern.compile("\\$\\{(?!r['\"])(\\w+)\\}");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            // 提取${...}中的内容
            String match = matcher.group();
            // 去掉${和}，得到nameToUpper
            String tmpParam = match.substring(2, match.length() - 1);
            if (CollUtil.isNotEmpty(customFilterParamList)) {
                if (customFilterParamList.contains(tmpParam)) {
                    continue;
                }
            }
            result.add(tmpParam);
        }
        return result;
    }

    public static void handleTemplateToFile(BaseTemplate templateConfig) {
        log.info("");
        String logPrefix = templateConfig.getLogPrefix();
        // 1. 判断是否启用
        if (!templateConfig.isEnable()) {
            log.warn("[{}] 未启用，不生成文件 !!!", logPrefix);
            return;
        } else {
            log.info("[{}] 启用，开始生成文件 ...", logPrefix);
        }
        // 2. 校验参数是否合法，若不合法则抛出异常
        ValidationUtils.validate(templateConfig);
        // 3. 加载模板
        String templatePath = templateConfig.getTemplatePath();
        log.info("[{}] 模板路径: {}", logPrefix, templatePath);
        // 4. 获取配置类
        Configuration configuration = getConfiguration();
        String filePath;
        String name;
        // 5. 判断模板类型，是相对路径还是绝对路径
        if (templatePath.startsWith(GlobalConstant.DEFAULT_PREFIX)) {
            String newTemplatePath = templatePath.substring(GlobalConstant.DEFAULT_PREFIX.length());
            String[] filePathAndName = FileUtils.extractFilePathAndName(newTemplatePath);
            filePath = filePathAndName[0];
            name = filePathAndName[1];
            configuration.setClassForTemplateLoading(FileUtils.class, filePath);
        } else {
            String[] filePathAndName = FileUtils.extractFilePathAndName(templatePath);
            filePath = filePathAndName[0];
            name = filePathAndName[1];
            try {
                configuration.setDirectoryForTemplateLoading(new File(filePath));
            } catch (IOException e) {
                log.error("[{}] 加载模板失败 !!!", logPrefix, e);
                throw new CodeGeneratorException(e);
            }
        }
        Template template;
        try {
            // 6. 加载模板
            template = configuration.getTemplate(name);
        } catch (IOException e) {
            log.error("[{}] 加载模板失败 !!!", logPrefix, e);
            throw new CodeGeneratorException(e);
        }
        // 7. 处理参数，判断参数是否符合模板要求
        Set<String> fileParams;
        if (templatePath.startsWith(GlobalConstant.DEFAULT_PREFIX)) {
            fileParams = findParams(FileUtils.readResourceAsString(templatePath.replace(GlobalConstant.DEFAULT_PREFIX, "")));
        } else {
            fileParams = findParams(FileUtils.readAbsolutePathAsString(templatePath));
        }
        Set<String> templateParams = new HashSet<>(templateConfig.getParamMap().keySet());
        Set<String> tmpParams = new HashSet<>();
        for (String filParam : fileParams) {
            if (!templateParams.contains(filParam)) {
                tmpParams.add(filParam);
            }
        }
        if (!tmpParams.isEmpty()) {
            log.error("[{}] 模板参数不匹配 !!!，缺少参数: {}", logPrefix, tmpParams);
            throw new CodeGeneratorException("模板参数不匹配");
        }
        // 8. 生成文件
        try {
            // 创建输出路径
            Path outputDirPath;
            if (templateConfig.getOutputDir().startsWith(GlobalConstant.DEFAULT_PREFIX)) {
                // 获取当前项目的根目录路径
                String projectRoot = System.getProperty("user.dir");
                String newOutputDir = templateConfig.getOutputDir().substring(GlobalConstant.DEFAULT_PREFIX.length());
                outputDirPath = Paths.get(projectRoot + "/src/main/resources/" + newOutputDir);
            } else {
                outputDirPath = Paths.get(templateConfig.getOutputDir());
            }
            Files.createDirectories(outputDirPath);
            Path outputFilePath = outputDirPath.resolve(templateConfig.getOutputName() + templateConfig.getOutputFileSuffix());
            // 将生成的内容写入文件
            boolean flag = false;
            if (FileUtil.exist(outputFilePath.toString())) {
                if (templateConfig.isOverwriteExisting()) {
                    FileUtil.del(outputFilePath.toString());
                    log.warn("[{}] {} 文件存在,已删除", logPrefix, templateConfig.getOutputName() + templateConfig.getOutputFileSuffix());
                    flag = true;
                } else {
                    log.warn("[{}] {} 文件存在,忽略生成 !!!", logPrefix, templateConfig.getOutputName() + templateConfig.getOutputFileSuffix());
                    log.warn("[{}] {} 文件路径：{}", logPrefix, templateConfig.getOutputName() + templateConfig.getOutputFileSuffix(), outputFilePath);
                    log.warn("[{}] 如果需要覆盖，请设置overwriteExisting为true", logPrefix);
                }
            } else {
                flag = true;
            }
            if (flag) {
                try (FileWriter writer = new FileWriter(outputFilePath.toFile())) {
                    template.process(templateConfig.getParamMap(), writer);
                }
                log.info("[{}] 文件路径：{}", logPrefix, outputFilePath);
            }
        } catch (Exception e) {
            log.error("[{}] 生成文件失败 !!!", logPrefix, e);
            throw new CodeGeneratorException(e);
        }
    }

    private static Configuration getConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setDefaultEncoding(String.valueOf(StandardCharsets.UTF_8));
        return configuration;
    }
}
