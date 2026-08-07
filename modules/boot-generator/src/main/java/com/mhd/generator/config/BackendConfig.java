package com.mhd.generator.config;

import cn.hutool.core.map.MapUtil;
import com.mhd.generator.constant.GeneratorConstant;
import com.mhd.generator.util.GeneratorUtils;
import com.mhd.generator.util.ValidationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.Map;
import java.util.function.Function;

/**
 * Java 后端代码生成配置（数据侧）。
 * <p>
 * 只承载数据源、包名、全局配置、各文件生成规格以及用户自定义上下文参数；
 * 不包含生成逻辑。生成流程由 {@link BackendGenerator} 负责：
 * <pre>{@code
 * new BackendGenerator(BackendConfig.createDefault()...).generate();
 * }</pre>
 * 用户可通过 {@link #extraParam(String, Object)} 注入/覆盖模板变量（如 moduleName），
 * 合并时用户参数优先于系统自动构建的值。
 *
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
@EqualsAndHashCode
@ToString
@Data
public class BackendConfig {

    @Valid
    private GlobalConfig globalConfig;
    @Valid
    private PackageConfig packageConfig;
    @Valid
    private DbConfig dbConfig;

    private BaseTemplate controllerTemplate;
    private BaseTemplate serviceTemplate;
    private BaseTemplate serviceImplTemplate;
    private BaseTemplate entityTemplate;
    private BaseTemplate xmlTemplate;
    private BaseTemplate mapperTemplate;
    private BaseTemplate queryReqDTOTemplate;
    private BaseTemplate saveReqDTOTemplate;
    private BaseTemplate voTemplate;

    /**
     * 用户自定义模板参数。生成时与系统构建的上下文合并，<b>用户优先（覆盖系统值）</b>。
     * 例如 {@code moduleName} 默认不自动推导，可由用户通过本 map 提供。
     */
    private Map<String, Object> extraParams = MapUtil.newHashMap();

    private BackendConfig() {
    }

    public static BackendConfig createDefault() {
        BackendConfig template = new BackendConfig();
        template.controllerTemplate = defaultControllerTemplate();
        template.serviceTemplate = defaultServiceTemplate();
        template.serviceImplTemplate = defaultServiceImplTemplate();
        template.entityTemplate = defaultEntityTemplate();
        template.xmlTemplate = defaultXmlTemplate();
        template.mapperTemplate = defaultMapperTemplate();
        template.queryReqDTOTemplate = defaultQueryReqDTOTemplate();
        template.saveReqDTOTemplate = defaultSaveReqDTOTemplate();
        template.voTemplate = defaultVoTemplate();
        template.globalConfig = defaultGlobalConfig();
        template.packageConfig = defaultPackageConfig();
        template.dbConfig = defaultDbConfig();
        return template;
    }

    /**
     * 校验配置对象（包名、数据源、全局配置）。
     */
    public void validate() {
        ValidationUtils.validate(packageConfig);
        ValidationUtils.validate(dbConfig);
        ValidationUtils.validate(globalConfig);
    }

    // region 用户自定义参数
    public BackendConfig extraParam(String key, Object value) {
        this.extraParams.put(key, value);
        return this;
    }

    public BackendConfig extraParams(Map<String, Object> params) {
        if (MapUtil.isNotEmpty(params)) {
            this.extraParams.putAll(params);
        }
        return this;
    }
    // endregion

    // region 默认文件规格
    private static BaseTemplate defaultControllerTemplate() {
        return BaseTemplate.builder()
                .enable(false).logPrefix("Controller")
                .outputDir(null).outputName(null).outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GeneratorConstant.DEFAULT_CONTROLLER_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultServiceTemplate() {
        return BaseTemplate.builder()
                .enable(false).logPrefix("Service")
                .outputDir(null).outputName(null).outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GeneratorConstant.DEFAULT_SERVICE_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultServiceImplTemplate() {
        return BaseTemplate.builder()
                .enable(false).logPrefix("ServiceImpl")
                .outputDir(null).outputName(null).outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GeneratorConstant.DEFAULT_SERVICE_IMPL_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultEntityTemplate() {
        return BaseTemplate.builder()
                .enable(false).logPrefix("Entity")
                .outputDir(null).outputName(null).outputFileSuffix(".java")
                .overwriteExisting(false)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultXmlTemplate() {
        return BaseTemplate.builder()
                .enable(false).logPrefix("XML")
                .outputDir(null).outputName(null).outputFileSuffix(".xml")
                .overwriteExisting(false)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultMapperTemplate() {
        return BaseTemplate.builder()
                .enable(false).logPrefix("Mapper")
                .outputDir(null).outputName(null).outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GeneratorConstant.DEFAULT_MAPPER_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultQueryReqDTOTemplate() {
        return BaseTemplate.builder()
                .enable(false).logPrefix("QueryReqDTO")
                .outputDir(null).outputName(null).outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GeneratorConstant.DEFAULT_QUERY_REQ_DTO_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultSaveReqDTOTemplate() {
        return BaseTemplate.builder()
                .enable(false).logPrefix("SaveReqDTO")
                .outputDir(null).outputName(null).outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GeneratorConstant.DEFAULT_SAVE_REQ_DTO_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultVoTemplate() {
        return BaseTemplate.builder()
                .enable(false).logPrefix("VO")
                .outputDir(null).outputName(null).outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GeneratorConstant.DEFAULT_VO_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }
    // endregion

    // region 默认配置
    private static GlobalConfig defaultGlobalConfig() {
        return GlobalConfig.builder()
                .author("zhao-hao-dong")
                .outputDir("./generated-code")
                .datePattern("yyyy-MM-dd")
                .build();
    }

    private static PackageConfig defaultPackageConfig() {
        return PackageConfig.builder().build();
    }

    private static DbConfig defaultDbConfig() {
        return DbConfig.builder().build();
    }
    // endregion

    // region 链式定制方法
    public BackendConfig controllerBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.controllerTemplate = GeneratorUtils.applyCustomization(controllerTemplate, customizer);
        return this;
    }

    public BackendConfig serviceBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.serviceTemplate = GeneratorUtils.applyCustomization(serviceTemplate, customizer);
        return this;
    }

    public BackendConfig serviceImplBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.serviceImplTemplate = GeneratorUtils.applyCustomization(serviceImplTemplate, customizer);
        return this;
    }

    public BackendConfig entityBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.entityTemplate = GeneratorUtils.applyCustomization(entityTemplate, customizer);
        return this;
    }

    public BackendConfig xmlBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.xmlTemplate = GeneratorUtils.applyCustomization(xmlTemplate, customizer);
        return this;
    }

    public BackendConfig mapperBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.mapperTemplate = GeneratorUtils.applyCustomization(mapperTemplate, customizer);
        return this;
    }

    public BackendConfig queryReqDTOBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.queryReqDTOTemplate = GeneratorUtils.applyCustomization(queryReqDTOTemplate, customizer);
        return this;
    }

    public BackendConfig saveReqDTOBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.saveReqDTOTemplate = GeneratorUtils.applyCustomization(saveReqDTOTemplate, customizer);
        return this;
    }

    public BackendConfig voBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.voTemplate = GeneratorUtils.applyCustomization(voTemplate, customizer);
        return this;
    }

    public BackendConfig globalConfigBuilder(Function<GlobalConfig.GlobalConfigBuilder, GlobalConfig.GlobalConfigBuilder> customizer) {
        this.globalConfig = customizer.apply(globalConfig.toBuilder()).build();
        return this;
    }

    public BackendConfig packageConfigBuilder(Function<PackageConfig.PackageConfigBuilder, PackageConfig.PackageConfigBuilder> customizer) {
        this.packageConfig = customizer.apply(packageConfig.toBuilder()).build();
        return this;
    }

    public BackendConfig dbConfigBuilder(Function<DbConfig.DbConfigBuilder, DbConfig.DbConfigBuilder> customizer) {
        this.dbConfig = customizer.apply(dbConfig.toBuilder()).build();
        return this;
    }
    // endregion

    // region 内部配置类
    @Setter
    @Getter
    @ToString
    @Builder(toBuilder = true)
    public static class GlobalConfig {
        /** 作者 */
        private String author;
        /** 输出目录 */
        @NotEmpty(message = "输出目录不能为空")
        private String outputDir;
        /** 日期格式 */
        private String datePattern;
    }

    @Getter
    @Setter
    @ToString
    @Builder(toBuilder = true)
    public static class PackageConfig {
        /** 父包名 */
        @NotBlank(message = "父包名不能为空")
        private String parent;
        /** 实体类包名 */
        @NotBlank(message = "实体类包名不能为空")
        private String entity;
        /** xml 包名 */
        @NotBlank(message = "xml 包名不能为空")
        private String xml;
        /** mapper 包名 */
        @NotBlank(message = "mapper 包名不能为空")
        private String mapper;
        /** service 包名 */
        @NotEmpty(message = "service 包名不能为空")
        private String service;
        /** serviceImpl 包名 */
        @NotEmpty(message = "serviceImpl 包名不能为空")
        private String serviceImpl;
        /** controller 包名 */
        @NotEmpty(message = "controller 包名不能为空")
        private String controller;
        /** queryReqDTO 包名 */
        @NotEmpty(message = "queryReqDTO 包名不能为空")
        private String queryReqDTO;
        /** saveReqDTO 包名 */
        @NotEmpty(message = "saveReqDTO 包名不能为空")
        private String saveReqDTO;
        /** vo 包名 */
        @NotEmpty(message = "vo 包名不能为空")
        private String vo;
    }

    @Setter
    @Getter
    @ToString
    @Builder(toBuilder = true)
    public static class DbConfig {
        /** 数据库地址 */
        @NotEmpty(message = "数据库地址不能为空")
        private String dbUrl;
        /** 数据库用户名 */
        @NotEmpty(message = "数据库用户名不能为空")
        private String dbUsername;
        /** 数据库密码 */
        @NotEmpty(message = "数据库密码不能为空")
        private String dbPassword;
        /** 表名 */
        @NotEmpty(message = "表名不能为空")
        @Size(min = 1, message = "表名不能为空")
        private String[] dbTableList;
        /** 表前缀 */
        private String[] dbTablePrefix;
    }
    // endregion
}
