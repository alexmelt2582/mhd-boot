package com.mhd.generator.template;

import cn.hutool.core.map.MapUtil;
import com.mhd.generator.constant.GlobalConstant;
import com.mhd.generator.core.BaseTemplate;
import com.mhd.generator.core.ITemplate;
import com.mhd.generator.util.GeneratorUtils;
import com.mhd.generator.validator.GroupMybatis;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.function.Function;

/**
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class DefaultBackendTemplate extends ITemplate {
    @Valid
    private BaseTemplate controllerTemplate;
    @Valid
    private BaseTemplate serviceTemplate;
    @Valid
    private BaseTemplate serviceImplTemplate;
    @Valid
    private BaseTemplate entityTemplate;
    @Valid
    private BaseTemplate xmlTemplate;
    @Valid
    private BaseTemplate mapperTemplate;
    @Valid
    private BaseTemplate queryReqDTOTemplate;
    @Valid
    private BaseTemplate saveReqDTOTemplate;
    @Valid
    private BaseTemplate voTemplate;

    @Valid
    private GlobalConfig globalConfig;
    @Valid
    private PackageConfig packageConfig;
    @Valid
    private DbConfig dbConfig;
    private String[] entityList;

    private DefaultBackendTemplate() {
    }



    public static DefaultBackendTemplate createDefault() {
        DefaultBackendTemplate template = new DefaultBackendTemplate();
        // 初始化所有模板
        template.controllerTemplate = defaultControllerTemplate();
        template.serviceTemplate = defaultServiceTemplate();
        template.serviceImplTemplate = defaultServiceImplTemplate();
        template.entityTemplate = defaultEntityTemplate();
        template.xmlTemplate = defaultXmlTemplate();
        template.mapperTemplate = defaultMapperTemplate();
        template.queryReqDTOTemplate = defaultQueryReqDTOTemplate();
        template.saveReqDTOTemplate = defaultSaveReqDTOTemplate();
        template.voTemplate = defaultVoTemplate();

        // 初始化配置
        template.globalConfig = defaultGlobalConfig();
        template.packageConfig = defaultPackageConfig();
        template.dbConfig = defaultDbConfig();

        return template;
    }
    // endregion

    // region 模板默认配置（完整版）
    private static BaseTemplate defaultControllerTemplate() {
        return BaseTemplate.builder()
                .enable(false)
                .logPrefix("Controller")
                .outputDir(null)
                .outputName(null)
                .outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_CONTROLLER_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultServiceTemplate() {
        return BaseTemplate.builder()
                .enable(false)
                .logPrefix("Service")
                .outputDir(null)
                .outputName(null)
                .outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_SERVICE_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultServiceImplTemplate() {
        return BaseTemplate.builder()
                .enable(false)
                .logPrefix("ServiceImpl")
                .outputDir(null)
                .outputName(null)
                .outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_SERVICE_IMPL_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultEntityTemplate() {
        return BaseTemplate.builder()
                .enable(false)
                .logPrefix("Entity")
                .outputDir(null)
                .outputName(null)
                .outputFileSuffix(".java")
                .overwriteExisting(false)
                //.templatePath(GlobalConstant.DEFAULT_ENTITY_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();

    }

    private static BaseTemplate defaultXmlTemplate() {
        return BaseTemplate.builder()
                .enable(false)
                .logPrefix("XML")
                .outputDir(null)
                .outputName(null)
                .outputFileSuffix(".xml")
                .overwriteExisting(false)
                //.templatePath(GlobalConstant.DEFAULT_XML_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultMapperTemplate() {
        return BaseTemplate.builder()
                .enable(false)
                .logPrefix("Mapper")
                .outputDir(null)
                .outputName(null)
                .outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_MAPPER_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultQueryReqDTOTemplate() {
        return BaseTemplate.builder()
                .enable(false)
                .logPrefix("QueryReqDTO")
                .outputDir(null)
                .outputName(null)
                .outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_QUERY_REQ_DTO_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultSaveReqDTOTemplate() {
        return BaseTemplate.builder()
                .enable(false)
                .logPrefix("SaveReqDTO")
                .outputDir(null)
                .outputName(null)
                .outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_SAVE_REQ_DTO_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    private static BaseTemplate defaultVoTemplate() {
        return BaseTemplate.builder()
                .enable(false)
                .logPrefix("VO")
                .outputDir(null)
                .outputName(null)
                .outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_VO_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }
    // endregion

    // region 配置默认值
    private static GlobalConfig defaultGlobalConfig() {
        return GlobalConfig.builder()
                .author("alex melt")
                .outputDir("./generated-code")
                .datePattern("yyyy-MM-dd")
                .build();
    }

    private static PackageConfig defaultPackageConfig() {
        return PackageConfig.builder()
                .build();
    }

    private static DbConfig defaultDbConfig() {
        return DbConfig.builder()
                .build();
    }
    // endregion

    public DefaultBackendTemplate entityListBuilder(String[] entityList) {
        this.entityList = entityList;
        return this;
    }

    // region 链式定制方法（完整版）
    public DefaultBackendTemplate controllerBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.controllerTemplate = GeneratorUtils.applyCustomization(controllerTemplate, customizer);
        return this;
    }

    public DefaultBackendTemplate serviceBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.serviceTemplate = GeneratorUtils.applyCustomization(serviceTemplate, customizer);
        return this;
    }

    public DefaultBackendTemplate serviceImplBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.serviceImplTemplate = GeneratorUtils.applyCustomization(serviceImplTemplate, customizer);
        return this;
    }

    public DefaultBackendTemplate entityBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.entityTemplate = GeneratorUtils.applyCustomization(entityTemplate, customizer);
        return this;
    }

    public DefaultBackendTemplate xmlBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.xmlTemplate = GeneratorUtils.applyCustomization(xmlTemplate, customizer);
        return this;
    }

    public DefaultBackendTemplate mapperBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.mapperTemplate = GeneratorUtils.applyCustomization(mapperTemplate, customizer);
        return this;
    }

    public DefaultBackendTemplate queryReqDTOBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.queryReqDTOTemplate = GeneratorUtils.applyCustomization(queryReqDTOTemplate, customizer);
        return this;
    }

    public DefaultBackendTemplate saveReqDTOBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.saveReqDTOTemplate = GeneratorUtils.applyCustomization(saveReqDTOTemplate, customizer);
        return this;
    }

    public DefaultBackendTemplate voBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.voTemplate = GeneratorUtils.applyCustomization(voTemplate, customizer);
        return this;
    }

    public DefaultBackendTemplate globalConfigBuilder(Function<GlobalConfig.GlobalConfigBuilder, GlobalConfig.GlobalConfigBuilder> customizer) {
        this.globalConfig = customizer.apply(globalConfig.toBuilder()).build();
        return this;
    }

    public DefaultBackendTemplate packageConfigBuilder(Function<PackageConfig.PackageConfigBuilder, PackageConfig.PackageConfigBuilder> customizer) {
        this.packageConfig = customizer.apply(packageConfig.toBuilder()).build();
        return this;
    }

    public DefaultBackendTemplate dbConfigBuilder(Function<DbConfig.DbConfigBuilder, DbConfig.DbConfigBuilder> customizer) {
        this.dbConfig = customizer.apply(dbConfig.toBuilder()).build();
        return this;
    }

    @Setter
    @Getter
    @ToString
    @Builder(toBuilder = true)
    public static class GlobalConfig {
        /**
         * 作者
         */
        private String author;
        /**
         * 输出目录
         */
        @NotEmpty(message = "输出目录不能为空", groups = {GroupMybatis.class})
        private String outputDir;
        /**
         * 日期格式
         */
        private String datePattern;
    }

    @Getter
    @Setter
    @ToString
    @Builder(toBuilder = true)
    public static class PackageConfig {
        /**
         * 父包名
         */
        @NotBlank(message = "父包名不能为空")
        private String parent;
        /**
         * 实体类包名
         */
        @NotBlank(message = "实体类包名不能为空")
        private String entity;
        /**
         * xml包名
         */
        @NotBlank(message = "xml包名不能为空")
        private String xml;
        /**
         * mapper包名
         */
        @NotBlank(message = "mapper包名不能为空")
        private String mapper;
        /**
         * service包名
         */
        @NotEmpty(message = "service包名不能为空")
        private String service;
        /**
         * serviceImpl包名
         */
        @NotEmpty(message = "serviceImpl包名不能为空")
        private String serviceImpl;
        /**
         * controller包名
         */
        @NotEmpty(message = "controller包名不能为空")
        private String controller;
        /**
         * queryDTO包名
         */
        @NotEmpty(message = "queryReqDTO包名不能为空")
        private String queryReqDTO;
        /**
         * saveDTO包名
         */
        @NotEmpty(message = "saveReqDTO包名不能为空")
        private String saveReqDTO;
        /**
         * vo包名
         */
        @NotEmpty(message = "vo包名不能为空")
        private String vo;
    }

    @Setter
    @Getter
    @ToString
    @Builder(toBuilder = true)
    public static class DbConfig {
        /**
         * 数据库驱动
         */
        @NotEmpty(message = "数据库驱动不能为空", groups = {GroupMybatis.class})
        private String dbUrl;
        /**
         * 数据库地址
         */
        @NotEmpty(message = "数据库地址不能为空", groups = {GroupMybatis.class})
        private String dbUsername;
        /**
         * 数据库密码
         */
        @NotEmpty(message = "数据库密码不能为空", groups = {GroupMybatis.class})
        private String dbPassword;
        /**
         * 表名
         */
        @NotEmpty(message = "表名不能为空", groups = {GroupMybatis.class})
        @Size(min = 1, message = "表名不能为空", groups = {GroupMybatis.class})
        private String[] dbTableList;
        /**
         * 表前缀
         */
        private String[] dbTablePrefix;
    }
}
