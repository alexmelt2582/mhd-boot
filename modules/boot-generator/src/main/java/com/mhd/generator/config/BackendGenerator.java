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
import com.baomidou.mybatisplus.generator.jdbc.DatabaseMetaDataWrapper;
import com.mhd.generator.constant.GeneratorConstant;
import com.mhd.generator.handler.CustomMysqlTypeConvert;
import com.mhd.generator.util.GeneratorUtils;
import com.mhd.generator.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Java 后端代码生成器（行为侧）。
 * <p>
 * 与 {@link BackendConfig}（数据侧）分离：配置对象只承载参数与校验，
 * 本类负责“根据配置 + 模板生成文件”的全部流程：
 * <ol>
 *   <li>校验配置；</li>
 *   <li>通过 MyBatis-Plus 提取表元数据；</li>
 *   <li>按需用 MP 生成 Entity / XML；</li>
 *   <li>逐表构建模板上下文并渲染 Mapper/Service/ServiceImpl/Controller/QueryDTO/SaveDTO/Vo。</li>
 * </ol>
 * 上下文构建完成后会合并 {@link BackendConfig#getExtraParams()}，
 * 用户传入的参数优先级高于系统自动构建的值（可用于覆盖 moduleName、author 等）。
 *
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
public class BackendGenerator {
    private static final Logger log = LoggerFactory.getLogger(BackendGenerator.class + GeneratorConstant.LOG_PREFIX);

    /** 简单类型 → 全限定名映射，用于生成 DTO/VO 的 import 语句。java.lang 与基本类型无需导入。 */
    private static final Map<String, String> TYPE_IMPORTS = Map.of(
            "BigDecimal", "java.math.BigDecimal",
            "BigInteger", "java.math.BigInteger",
            "LocalDateTime", "java.time.LocalDateTime",
            "LocalDate", "java.time.LocalDate",
            "LocalTime", "java.time.LocalTime",
            "Date", "java.util.Date",
            "Timestamp", "java.sql.Timestamp"
    );

    /** 代码生成配置（数据源、包路径、模板开关等） */
    private final BackendConfig config;

    /**
     * 构造后端生成器。
     *
     * @param config 代码生成配置对象，不可为 null
     */
    public BackendGenerator(BackendConfig config) {
        this.config = config;
    }

    /**
     * 校验配置并生成后端代码。
     */
    public void generate() {
        config.validate();

        List<TableInfo> tableInfos = extractTableMetadata();
        if (config.getEntityTemplate().isEnable()) {
            generateMybatisOfEntity();
        }
        if (config.getXmlTemplate().isEnable()) {
            generateMybatisOfXml();
        }
        BackendConfig.PackageConfig packageConfig = config.getPackageConfig();
        for (TableInfo tableInfo : tableInfos) {
            Map<String, Object> base = buildContext(tableInfo);
            Map<String, String> namingMap = (Map)base.get("naming");
            String entity = tableInfo.getEntityName();
            render(config.getMapperTemplate(), base, entity, namingMap.get("mapperName"), packageConfig.getMapper());
            render(config.getServiceTemplate(), base, entity, namingMap.get("serviceName"), packageConfig.getService());
            render(config.getServiceImplTemplate(), base, entity, namingMap.get("serviceImplName"), packageConfig.getServiceImpl());
            render(config.getControllerTemplate(), base, entity, namingMap.get("controllerName"), packageConfig.getController());
            render(config.getQueryReqDTOTemplate(), base, entity, namingMap.get("queryDTOName"), packageConfig.getQueryReqDTO());
            render(config.getSaveReqDTOTemplate(), base, entity, namingMap.get("saveDTOName"), packageConfig.getSaveReqDTO());
            render(config.getVoTemplate(), base, entity, namingMap.get("voName"), packageConfig.getVo());
        }
    }

    /**
     * 用 MP 查询数据库元数据，不生成文件，只取 TableInfo。
     *
     * @return 表元数据列表（含字段、命名等信息）
     */
    private List<TableInfo> extractTableMetadata() {
        BackendConfig.DbConfig dbConfig = config.getDbConfig();
        BackendConfig.PackageConfig packageConfig = config.getPackageConfig();
        BackendConfig.GlobalConfig globalConfig = config.getGlobalConfig();

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
     * 基于 MP 的 TableInfo 构建模板上下文（嵌套结构：naming / packages / fields / imports）。
     * <p>
     * 构建完成后合并 {@link BackendConfig#getExtraParams()}，用户参数优先（覆盖系统值）。
     * 例如 {@code moduleName} 默认不自动推导，由用户通过 extraParams 提供。
     *
     * @param tableInfo MP 表元数据对象，含字段列表与命名信息
     * @return 模板上下文 Map，包含 naming、packages、fields、imports 等层级结构
     */
    private Map<String, Object> buildContext(TableInfo tableInfo) {
        Map<String, Object> ctx = new HashMap<>();

        BackendConfig.PackageConfig p = config.getPackageConfig();
        BackendConfig.GlobalConfig g = config.getGlobalConfig();
        String parent = p.getParent();

        // ========== 1. 基础信息 ==========
        String author = StrUtil.blankToDefault(g.getAuthor(), "zhao-hao-dong");
        String date = DateTimeFormatter.ofPattern(
                StrUtil.blankToDefault(g.getDatePattern(), "yyyy-MM-dd")
        ).format(LocalDateTimeUtil.now());
        ctx.put("author", author);
        ctx.put("date", date);


        // ========== 2. 表元数据 ==========
        ctx.put("tableName", tableInfo.getName());
        ctx.put("tableComment", tableInfo.getComment());

        String primaryKey = tableInfo.getIndexList().stream()
                .filter(index -> "PRIMARY".equalsIgnoreCase(index.getName()))
                .findFirst()
                .map(DatabaseMetaDataWrapper.Index::getColumnName)
                .orElse("id"); // 如果流为空，则返回默认值
        ctx.put("primaryKey", StringUtils.toLowerCamelCase(primaryKey));

        // ---------- 3. naming（命名相关） ----------
        Map<String, Object> naming = new HashMap<>();
        String entityName = tableInfo.getEntityName();
        naming.put("entityName", entityName);
        naming.put("entityNameLower", StringUtils.toFirstLower(entityName));
        String controllerName = tableInfo.getControllerName();
        naming.put("controllerName", controllerName);
        naming.put("controllerNameLower", StringUtils.toFirstLower(controllerName));
        String serviceName = entityName + "Service";
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

        // ========== 5. 合并用户自定义参数 ==========
        Map<String, Object> extraParams = config.getExtraParams();
        if (MapUtil.isNotEmpty(extraParams)) {
            ctx.putAll(extraParams);
        }
        return ctx;
    }

    /**
     * 从 TableInfo 提取字段列表与 import 集合（基于 DB 元数据，无需 Entity 已编译）。
     * <ul>
     *   <li>{@code fields}：query_dto / save_dto 模板使用（含 type、name）</li>
     *   <li>{@code normalFields}：vo 模板使用（含 simpleType、comment、name）</li>
     *   <li>{@code imports}：全限定类型名集合</li>
     * </ul>
     *
     * @param map    模板上下文 Map，字段列表与 import 集合将写入此处
     * @param fields DB 元数据字段列表，来自 TableInfo#getFields()
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
     * 渲染单个文件规格：基于模板上下文补充当前角色包名，计算输出目录/文件名后生成。
     * <p>
     * 从 base 拷贝上下文并补充当前角色的 package 信息，避免多角色共享同一个 base map。
     *
     * @param spec      模板规格，含模板路径、开关等配置
     * @param base      模板基础上下文（含 naming、packages、fields 等）
     * @param outputName   输出文件名（如 {@code "XXXMapper"}、{@code "XXXService"}）
     * @param rolePkg   当前角色的子包名（如 {@code "mapper"}、{@code "service"}）
     */
    private void render(BaseTemplate spec, Map<String, Object> base, String entity, String outputName, String rolePkg) {
        if (!spec.isEnable()) {
            return;
        }
        String rolePackage = resolvePackage(config.getPackageConfig().getParent(), rolePkg);
        spec.setOutputName(outputName);
        spec.setOutputDir(config.getGlobalConfig().getOutputDir() + "/" + rolePackage.replace(".", "/"));
        spec.setParamMap(base);
        GeneratorUtils.handleTemplateToFile(spec);
    }

    /**
     * 确保目录存在，兼容 Windows 路径分隔符。
     *
     * @param dirPath 目录绝对路径
     */
    private void ensureDirectoryExists(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.debug("[{}] 创建输出目录: {}", "Generator", dirPath);
            }
        } catch (Exception e) {
            log.warn("[{}] 创建输出目录失败: {} - {}", "Generator", dirPath, e.getMessage());
        }
    }

    // region MyBatis-Plus 生成 Entity / XML

    /**
     * 使用 MyBatis-Plus 生成 Entity 实体类。
     * <p>
     * 根据 {@code entityTemplate.isOverwriteExisting()} 决定是否覆盖已存在的文件，
     * 策略中仅启用 Entity 生成，关闭 Service / Controller / Mapper。
     */
    private void generateMybatisOfEntity() {
        Consumer<StrategyConfig.Builder> consumer;
        if (config.getEntityTemplate().isOverwriteExisting()) {
            consumer = this::entityStrategyWithOverride;
        } else {
            consumer = this::entityStrategy;
        }
        generateMybatis(consumer);
    }

    /**
     * Entity 生成策略（不覆盖已存在文件）。
     *
     * @param builder MP 策略配置构建器
     */
    private void entityStrategy(StrategyConfig.Builder builder) {
        builder
                .addInclude(config.getDbConfig().getDbTableList())
                .addTablePrefix(Arrays.asList(config.getDbConfig().getDbTablePrefix()))
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

    /**
     * Entity 生成策略（覆盖已存在文件）。
     *
     * @param builder MP 策略配置构建器
     */
    private void entityStrategyWithOverride(StrategyConfig.Builder builder) {
        builder
                .addInclude(config.getDbConfig().getDbTableList())
                .addTablePrefix(Arrays.asList(config.getDbConfig().getDbTablePrefix()))
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
    }

    /**
     * 使用 MyBatis-Plus 生成 Mapper XML 映射文件。
     * <p>
     * 根据 {@code xmlTemplate.isOverwriteExisting()} 决定是否覆盖已存在的文件，
     * 策略中仅启用 XML 生成，关闭 Entity / Service / Controller。
     */
    private void generateMybatisOfXml() {
        Consumer<StrategyConfig.Builder> consumer;
        if (config.getXmlTemplate().isOverwriteExisting()) {
            consumer = this::xmlStrategyWithOverride;
        } else {
            consumer = this::xmlStrategy;
        }
        generateMybatis(consumer);
    }

    /**
     * XML 生成策略（不覆盖已存在文件）。
     *
     * @param builder MP 策略配置构建器
     */
    private void xmlStrategy(StrategyConfig.Builder builder) {
        builder
                .addInclude(config.getDbConfig().getDbTableList())
                .addTablePrefix(Arrays.asList(config.getDbConfig().getDbTablePrefix()))
                .entityBuilder().disable()
                .serviceBuilder().disable()
                .controllerBuilder().disable()
                .mapperBuilder()
                .disableMapper()
                .enableBaseResultMap()
                .enableBaseColumnList();
    }

    /**
     * XML 生成策略（覆盖已存在文件）。
     *
     * @param builder MP 策略配置构建器
     */
    private void xmlStrategyWithOverride(StrategyConfig.Builder builder) {
        builder
                .addInclude(config.getDbConfig().getDbTableList())
                .addTablePrefix(Arrays.asList(config.getDbConfig().getDbTablePrefix()))
                .entityBuilder().disable()
                .serviceBuilder().disable()
                .controllerBuilder().disable()
                .mapperBuilder()
                .disableMapper()
                .enableBaseResultMap()
                .enableBaseColumnList()
                .enableFileOverride();
    }

    /**
     * 统一的 MyBatis-Plus 生成入口。
     * <p>
     * 使用 {@link FastAutoGenerator} 构建完整的生成链路，包括全局配置、包配置、策略配置，
     * 最终通过 Freemarker 模板引擎生成文件。XML 路径会根据是否以默认前缀开头做适配。
     *
     * @param consumer 策略配置回调，由调用方决定启用 Entity 还是 XML 生成
     */
    private void generateMybatis(Consumer<StrategyConfig.Builder> consumer) {
        BackendConfig.PackageConfig packageConfig = config.getPackageConfig();
        BackendConfig.DbConfig dbConfig = config.getDbConfig();
        BackendConfig.GlobalConfig globalConfig = config.getGlobalConfig();

        // XML 路径适配：若以默认前缀开头（如 "classpath:"），则转为 src/main/resources 下的相对路径
        String filePath;
        if (packageConfig.getXml().startsWith(GeneratorConstant.DEFAULT_PREFIX)) {
            String newXmlPath = packageConfig.getXml().substring(GeneratorConstant.DEFAULT_PREFIX.length());
            filePath = "src/main/resources" + newXmlPath;
        } else {
            filePath = packageConfig.getXml();
        }
        ensureDirectoryExists(globalConfig.getOutputDir());

        // 数据源构建，注册自定义 MySQL 类型转换器以处理特殊类型映射
        DataSourceConfig.Builder dataSourceBuilder = new DataSourceConfig.Builder(
                dbConfig.getDbUrl(), dbConfig.getDbUsername(), dbConfig.getDbPassword()
        ).typeConvertHandler(new CustomMysqlTypeConvert());

        // 构建 MP 生成器管线：全局配置 → 包配置 → 策略配置 → 模板引擎 → 执行
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
    // endregion

    /**
     * 将包路径片段拼接为完整的 Java 包路径。
     *
     * @param parts 包路径片段，如 {@code ("com.mhd", "mapper")}
     * @return 拼接后的完整包路径，如 {@code "com.mhd.mapper"}
     */
    private String resolvePackage(String... parts) {
        return StringUtils.resolveClassPath(parts);
    }
}
