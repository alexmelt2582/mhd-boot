package com.mhd.generator.config;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.mhd.generator.constant.GeneratorConstant;
import com.mhd.generator.handler.CustomMysqlTypeConvert;
import com.mhd.generator.util.GeneratorUtils;
import com.mhd.generator.util.StringUtils;
import com.mhd.generator.util.ValidationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Java 后端代码生成配置（自包含：构建 → 校验 → 生成）。
 * <p>
 * 承载数据源、包名、全局配置以及各文件的生成规格。调用 {@link #generate()} 后：
 * <ol>
 *   <li>校验配置；</li>
 *   <li>通过 MyBatis-Plus 提取表元数据；</li>
 *   <li>按需用 MP 生成 Entity / XML；</li>
 *   <li>逐表构建模板上下文并渲染 Mapper/Service/ServiceImpl/Controller/QueryReqDTO/SaveReqDTO/Vo。</li>
 * </ol>
 *
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
@EqualsAndHashCode
@ToString
@Data
public class BackendConfig {
    private static final Logger log = LoggerFactory.getLogger(BackendConfig.class + GeneratorConstant.LOG_PREFIX);


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
     * 用户通过 xxxBuilder 设置的 paramMap 快照，避免逐表渲染时被覆盖丢失。
     */
    //private final Map<BaseTemplate, Map<String, Object>> paramSnapshots = new IdentityHashMap<>();
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

    /**
     * 校验配置并生成后端代码。
     */
    public void generate() {
        ValidationUtils.validate(packageConfig);
        //captureParamSnapshots();

        List<TableInfo> tableInfos = extractTableMetadata();
        if (entityTemplate.isEnable()) {
            generateMybatisOfEntity();
        }
        if (xmlTemplate.isEnable()) {
            generateMybatisOfXml();
        }
        for (TableInfo tableInfo : tableInfos) {
            Map<String, Object> base = buildContext(tableInfo);
            String entity = tableInfo.getEntityName();
            render(mapperTemplate, base, entity, "Mapper", packageConfig.getMapper());
            render(serviceTemplate, base, entity, "Service", packageConfig.getService());
            render(serviceImplTemplate, base, entity, "ServiceImpl", packageConfig.getServiceImpl());
            render(controllerTemplate, base, entity, "Controller", packageConfig.getController());
            render(queryReqDTOTemplate, base, entity, "QueryReqDTO", packageConfig.getQueryReqDTO());
            render(saveReqDTOTemplate, base, entity, "SaveReqDTO", packageConfig.getSaveReqDTO());
            render(voTemplate, base, entity, "Vo", packageConfig.getVo());
        }
    }

    //private void captureParamSnapshots() {
    //    paramSnapshots.clear();
    //    snapshotParam(controllerTemplate);
    //    snapshotParam(serviceTemplate);
    //    snapshotParam(serviceImplTemplate);
    //    snapshotParam(entityTemplate);
    //    snapshotParam(xmlTemplate);
    //    snapshotParam(mapperTemplate);
    //    snapshotParam(queryReqDTOTemplate);
    //    snapshotParam(saveReqDTOTemplate);
    //    snapshotParam(voTemplate);
    //}

    //private void snapshotParam(BaseTemplate spec) {
    //    paramSnapshots.put(spec, spec.getParamMap() == null ? new HashMap<>() : new HashMap<>(spec.getParamMap()));
    //}

    /**
     * 用 MP 查询数据库元数据，不生成文件，只取 TableInfo。
     */
    private List<TableInfo> extractTableMetadata() {
        DataSourceConfig dsc = new DataSourceConfig.Builder(
                dbConfig.getDbUrl(), dbConfig.getDbUsername(), dbConfig.getDbPassword()
        ).build();

        StrategyConfig strategy = new StrategyConfig.Builder()
                .addInclude(dbConfig.getDbTableList())
                .addTablePrefix(dbConfig.getDbTablePrefix())
                .entityBuilder()
                .naming(NamingStrategy.underline_to_camel)
                .columnNaming(NamingStrategy.underline_to_camel)
                .enableLombok()
                .idType(IdType.AUTO)
                .enableTableFieldAnnotation()
                .build();

        com.baomidou.mybatisplus.generator.config.PackageConfig pkg =
                new com.baomidou.mybatisplus.generator.config.PackageConfig.Builder()
                        .parent(packageConfig.getParent())
                        .entity(packageConfig.getEntity())
                        .mapper(packageConfig.getMapper())
                        .service(packageConfig.getService())
                        .serviceImpl(packageConfig.getServiceImpl())
                        .controller(packageConfig.getController())
                        .build();

        com.baomidou.mybatisplus.generator.config.GlobalConfig global =
                new com.baomidou.mybatisplus.generator.config.GlobalConfig.Builder()
                        .author(globalConfig.getAuthor())
                        .build();

        ConfigBuilder configBuilder = new ConfigBuilder(pkg, dsc, strategy, null, global, null);
        return configBuilder.getTableInfoList();
    }

    /**
     * 基于 MP 的 TableInfo 构建模板上下文
     */
    private Map<String, Object> buildContext(TableInfo tableInfo) {
        Map<String, Object> ctx = new HashMap<>();

        PackageConfig p = packageConfig;
        String parent = p.getParent();
        // ========== 1. 基础信息 ==========

        String author = StrUtil.blankToDefault(globalConfig.getAuthor(), "zhao-hao-dong");
        String date = DateTimeFormatter.ofPattern(
                StrUtil.blankToDefault(globalConfig.getDatePattern(), "yyyy-MM-dd")
        ).format(LocalDateTimeUtil.now());
        ctx.put("author", author);
        ctx.put("date", date);

        // ========== 2. 表元数据 ==========
        ctx.put("tableName", tableInfo.getName());           // sys_user
        ctx.put("tableComment", tableInfo.getComment());     // 用户表

        // ---------- 3. naming（命名相关） ----------
        Map<String, Object> naming = new HashMap<>();
        String entityName = tableInfo.getEntityName();  // User
        naming.put("entityName", entityName);
        naming.put("entityNameLower", StringUtils.toFirstLower(entityName));
        String controllerName = tableInfo.getControllerName();
        naming.put("controllerName", controllerName);
        naming.put("controllerNameLower", StringUtils.toFirstLower(controllerName));
        String serviceName = tableInfo.getServiceName();
        naming.put("serviceName", serviceName);
        naming.put("serviceNameLower", StringUtils.toFirstLower(serviceName));
        String serviceImplName = tableInfo.getServiceImplName();
        naming.put("serviceImplName", serviceImplName);
        naming.put("serviceImplNameLower", StringUtils.toFirstLower(serviceImplName));
        String mapperName = tableInfo.getMapperName();
        naming.put("mapperName", mapperName);
        naming.put("mapperNameLower", StringUtils.toFirstLower(mapperName));

        String saveDTOName = entityName + "SaveDTO";
        naming.put("saveDTOName", saveDTOName);
        naming.put("saveDTONameLower", StringUtils.toFirstLower(saveDTOName));
        String queryDTOName = entityName + "QueryDTO";
        naming.put("queryDTOName", queryDTOName);
        naming.put("queryDTONameLower", StringUtils.toFirstLower(queryDTOName));
        String voName = entityName + "Vo";
        naming.put("voName", voName);
        naming.put("voNameLower", StringUtils.toFirstLower(voName));
        ctx.put("naming", naming);

        // ---------- 4. packages（包路径） ----------
        Map<String, Object> packages = new HashMap<>();

        packages.put("parent", parent);
        packages.put("entityPackage", resolvePackage(parent, p.getEntity()));
        packages.put("mapperPackage", resolvePackage(parent, p.getMapper()));
        packages.put("servicePackage", resolvePackage(parent, p.getService()));
        packages.put("serviceImplPackage", resolvePackage(parent, p.getServiceImpl()));
        packages.put("controllerPackage", resolvePackage(parent, p.getController()));

        packages.put("queryDTOPackage", resolvePackage(parent, p.getQueryReqDTO()));
        packages.put("saveDTOPackage", resolvePackage(parent, p.getSaveReqDTO()));
        packages.put("voPackage", resolvePackage(parent, p.getVo()));
        ctx.put("packages", packages);

        handleFields(ctx, tableInfo.getFields());
        return ctx;
    }


    /**
     * 简单类型 → 全限定名映射，用于生成 DTO/VO 的 import 语句。java.lang 与基本类型无需导入。
     */
    private static final Map<String, String> TYPE_IMPORTS = Map.of(
            "BigDecimal", "java.math.BigDecimal",
            "BigInteger", "java.math.BigInteger",
            "LocalDateTime", "java.time.LocalDateTime",
            "LocalDate", "java.time.LocalDate",
            "LocalTime", "java.time.LocalTime",
            "Date", "java.util.Date",
            "Timestamp", "java.sql.Timestamp"
    );

    /**
     * 从 TableInfo 提取字段列表与 import 集合，替代原有基于反射的方式（无需 Entity 已编译）。
     * <ul>
     *   <li>{@code fields}：query_dto / save_dto 模板使用（含 type、name）</li>
     *   <li>{@code normalFields}：vo 模板使用（含 simpleType、comment、name）</li>
     *   <li>{@code imports}：全限定类型名集合</li>
     * </ul>
     */
    private void handleFields(Map<String, Object> map, List<TableField> fields) {
        List<Map<String, String>> fieldList = new ArrayList<>();
        List<Map<String, String>> normalFieldList = new ArrayList<>();
        Set<String> imports = new LinkedHashSet<>();
        if (fields != null) {
            for (TableField f : fields) {
                String name = f.getPropertyName();
                if ("serialVersionUID".equals(name)) {
                    continue;
                }
                String simpleType = StrUtil.blankToDefault(f.getPropertyType(), "Object");
                Map<String, String> fm = new HashMap<>();
                fm.put("name", name);
                fm.put("type", simpleType);
                fieldList.add(fm);

                Map<String, String> nf = new HashMap<>();
                nf.put("name", name);
                nf.put("simpleType", simpleType);
                nf.put("comment", StrUtil.blankToDefault(f.getComment(), ""));
                normalFieldList.add(nf);

                String fullType = TYPE_IMPORTS.get(simpleType);
                if (fullType != null) {
                    imports.add(fullType);
                }
            }
        }
        map.put("fields", fieldList);
        map.put("normalFields", normalFieldList);
        map.put("imports", imports);
    }

    /**
     * 渲染单个文件规格：合并上下文与用户覆盖参数，计算输出目录/文件名后生成。
     */
    private void render(BaseTemplate spec, Map<String, Object> base, String entity, String suffix, String rolePkg) {
        if (!spec.isEnable()) {
            return;
        }
        String rolePackage = resolvePackage(packageConfig.getParent(), rolePkg);
        spec.setOutputName(entity + suffix);
        spec.setOutputDir(globalConfig.getOutputDir() + "/" + rolePackage.replace(".", "/"));
        spec.setParamMap(base);
        GeneratorUtils.handleTemplateToFile(spec);
    }

    private String resolvePackage(String... parts) {
        return StringUtils.resolveClassPath(parts);
    }

    // region MyBatis-Plus 生成 Entity / XML
    private void generateMybatisOfEntity() {
        Consumer<StrategyConfig.Builder> consumer;
        if (entityTemplate.isOverwriteExisting()) {
            consumer = builder -> builder
                    .addInclude(dbConfig.getDbTableList())
                    .addTablePrefix(Arrays.asList(dbConfig.getDbTablePrefix()))
                    .entityBuilder()
                    .enableLombok()
                    .enableFileOverride()
                    .idType(IdType.AUTO)
                    .naming(NamingStrategy.underline_to_camel)
                    .columnNaming(NamingStrategy.underline_to_camel)
                    .enableTableFieldAnnotation()
                    .serviceBuilder().disable()
                    .controllerBuilder().disable()
                    .mapperBuilder().disable();
        } else {
            consumer = builder -> builder
                    .addInclude(dbConfig.getDbTableList())
                    .addTablePrefix(Arrays.asList(dbConfig.getDbTablePrefix()))
                    .entityBuilder()
                    .enableLombok()
                    .idType(IdType.AUTO)
                    .naming(NamingStrategy.underline_to_camel)
                    .columnNaming(NamingStrategy.underline_to_camel)
                    .enableTableFieldAnnotation()
                    .serviceBuilder().disable()
                    .controllerBuilder().disable()
                    .mapperBuilder().disable();
        }
        generateMybatis(consumer);
    }

    private void generateMybatisOfXml() {
        Consumer<StrategyConfig.Builder> consumer;
        if (xmlTemplate.isOverwriteExisting()) {
            consumer = builder -> builder
                    .addInclude(dbConfig.getDbTableList())
                    .addTablePrefix(Arrays.asList(dbConfig.getDbTablePrefix()))
                    .entityBuilder().disable()
                    .serviceBuilder().disable()
                    .controllerBuilder().disable()
                    .mapperBuilder()
                    .disableMapper()
                    .enableBaseResultMap()
                    .enableBaseColumnList()
                    .enableFileOverride();
        } else {
            consumer = builder -> builder
                    .addInclude(dbConfig.getDbTableList())
                    .addTablePrefix(Arrays.asList(dbConfig.getDbTablePrefix()))
                    .entityBuilder().disable()
                    .serviceBuilder().disable()
                    .controllerBuilder().disable()
                    .mapperBuilder()
                    .disableMapper()
                    .enableBaseResultMap()
                    .enableBaseColumnList();
        }
        generateMybatis(consumer);
    }

    private void generateMybatis(Consumer<StrategyConfig.Builder> consumer) {
        String filePath;
        if (packageConfig.getXml().startsWith(GeneratorConstant.DEFAULT_PREFIX)) {
            String newXmlPath = packageConfig.getXml().substring(GeneratorConstant.DEFAULT_PREFIX.length());
            filePath = "src/main/resources" + newXmlPath;
        } else {
            filePath = packageConfig.getXml();
        }
        DataSourceConfig.Builder dataSourceBuilder = new DataSourceConfig.Builder(
                dbConfig.getDbUrl(), dbConfig.getDbUsername(), dbConfig.getDbPassword()
        ).typeConvertHandler(new CustomMysqlTypeConvert());
        FastAutoGenerator.create(dataSourceBuilder)
                .globalConfig(builder -> builder
                        .author(globalConfig.getAuthor())
                        .outputDir(globalConfig.getOutputDir())
                        .disableOpenDir()
                        .commentDate(globalConfig.getDatePattern()))
                .packageConfig(builder -> builder
                        .parent(packageConfig.getParent())
                        .entity(packageConfig.getEntity())
                        .service(packageConfig.getService())
                        .serviceImpl(packageConfig.getServiceImpl())
                        .mapper(packageConfig.getMapper())
                        .controller(packageConfig.getController())
                        .pathInfo(Collections.singletonMap(OutputFile.xml, filePath)))
                .strategyConfig(consumer)
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    // region 内部配置类
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
        @NotEmpty(message = "输出目录不能为空")
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
         * xml 包名
         */
        @NotBlank(message = "xml 包名不能为空")
        private String xml;
        /**
         * mapper 包名
         */
        @NotBlank(message = "mapper 包名不能为空")
        private String mapper;
        /**
         * service 包名
         */
        @NotEmpty(message = "service 包名不能为空")
        private String service;
        /**
         * serviceImpl 包名
         */
        @NotEmpty(message = "serviceImpl 包名不能为空")
        private String serviceImpl;
        /**
         * controller 包名
         */
        @NotEmpty(message = "controller 包名不能为空")
        private String controller;
        /**
         * queryReqDTO 包名
         */
        @NotEmpty(message = "queryReqDTO 包名不能为空")
        private String queryReqDTO;
        /**
         * saveReqDTO 包名
         */
        @NotEmpty(message = "saveReqDTO 包名不能为空")
        private String saveReqDTO;
        /**
         * vo 包名
         */
        @NotEmpty(message = "vo 包名不能为空")
        private String vo;
    }

    @Setter
    @Getter
    @ToString
    @Builder(toBuilder = true)
    public static class DbConfig {
        /**
         * 数据库地址
         */
        @NotEmpty(message = "数据库地址不能为空")
        private String dbUrl;
        /**
         * 数据库用户名
         */
        @NotEmpty(message = "数据库用户名不能为空")
        private String dbUsername;
        /**
         * 数据库密码
         */
        @NotEmpty(message = "数据库密码不能为空")
        private String dbPassword;
        /**
         * 表名
         */
        @NotEmpty(message = "表名不能为空")
        @Size(min = 1, message = "表名不能为空")
        private String[] dbTableList;
        /**
         * 表前缀
         */
        private String[] dbTablePrefix;
    }
    // endregion
}
