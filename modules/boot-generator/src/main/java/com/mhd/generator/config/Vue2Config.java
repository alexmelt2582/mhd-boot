package com.mhd.generator.config;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.mhd.generator.constant.GeneratorConstant;
import com.mhd.generator.util.GeneratorUtils;
import com.mhd.generator.util.StringUtils;
import com.mhd.generator.util.ValidationUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Vue2 前端代码生成配置（自包含：构建 → 校验 → 生成）。
 * <p>
 * 用户只需提供 {@code name}（如 user），对象内部自动派生模板所需变量：
 * <ul>
 *   <li>nameToUpper = User</li>
 *   <li>methodName  = User</li>
 *   <li>urlPath     = name（未显式设置时）</li>
 *   <li>primaryKey  = id（默认）</li>
 * </ul>
 * 调用 {@link #generate()} 触发校验与文件生成。
 *
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
@Getter
@ToString
public class Vue2Config {

    @NotBlank(message = "name 不能为空")
    private String name;
    private String primaryKey = "id";
    private String urlPath;
    @NotBlank(message = "输出目录不能为空")
    private String outputDir;
    private String outputName = "index";
    private boolean enableIndex = true;
    private boolean enableJs = true;
    private boolean overwriteExisting = false;
    private Map<String, Object> extraParams = MapUtil.newHashMap();

    private Vue2Config() {
    }

    public static Vue2Config createDefault() {
        Vue2Config config = new Vue2Config();
        config.outputDir = GeneratorConstant.DEFAULT_PREFIX + "/vue2";
        return config;
    }

    public Vue2Config name(String name) {
        this.name = name;
        return this;
    }

    public Vue2Config primaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public Vue2Config urlPath(String urlPath) {
        this.urlPath = urlPath;
        return this;
    }

    public Vue2Config outputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public Vue2Config outputName(String outputName) {
        this.outputName = outputName;
        return this;
    }

    public Vue2Config enableIndex(boolean enableIndex) {
        this.enableIndex = enableIndex;
        return this;
    }

    public Vue2Config enableJs(boolean enableJs) {
        this.enableJs = enableJs;
        return this;
    }

    public Vue2Config overwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
        return this;
    }

    public Vue2Config extraParam(String key, Object value) {
        this.extraParams.put(key, value);
        return this;
    }

    public Vue2Config extraParams(Map<String, Object> params) {
        if (MapUtil.isNotEmpty(params)) {
            this.extraParams.putAll(params);
        }
        return this;
    }

    /**
     * 校验配置并生成 Vue2 文件。
     */
    public void generate() {
        ValidationUtils.validate(this);
        Map<String, Object> params = buildParamMap();
        if (enableIndex) {
            render("Vue2 Index", GeneratorConstant.DEFAULT_VUE2_INDEX_TEMPLATE, ".vue", params);
        }
        if (enableJs) {
            render("Vue2 JS", GeneratorConstant.DEFAULT_VUE2_JS_TEMPLATE, ".js", params);
        }
    }

    /**
     * 由 name 派生模板所需全部变量。
     */
    private Map<String, Object> buildParamMap() {
        String nameUpper = StringUtils.toUpperCamelCase(name);
        String methodName = nameUpper;
        String url = StrUtil.blankToDefault(urlPath, name);
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("nameToUpper", nameUpper);
        params.put("methodName", methodName);
        params.put("urlPath", url);
        params.put("primaryKey", primaryKey);
        if (MapUtil.isNotEmpty(extraParams)) {
            params.putAll(extraParams);
        }
        return params;
    }

    private void render(String logPrefix, String templatePath, String suffix, Map<String, Object> params) {
        BaseTemplate spec = BaseTemplate.builder()
                .enable(true)
                .logPrefix(logPrefix)
                .outputDir(outputDir)
                .outputName(outputName)
                .outputFileSuffix(suffix)
                .overwriteExisting(overwriteExisting)
                .templatePath(templatePath)
                .paramMap(new HashMap<>(params))
                .build();
        GeneratorUtils.handleTemplateToFile(spec);
    }
}
