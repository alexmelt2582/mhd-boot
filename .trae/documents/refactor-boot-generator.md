# 重构 boot-generator 模块：简化设计、完全分离

## Context（为什么做这件事）

`modules/boot-generator` 当前的设计过度抽象且处于半重构、部分功能断裂的状态。用户希望简化为：**Vue2 和 Java 各自成为自包含的生成器**——配置对象自带校验、自带生成逻辑，互不耦合（"完全分离"）。

诊断到的核心问题：

1. **抽象层过度**：`ITemplate`（空类）、`TemplateHandler` 接口、`TemplateHandlerManager`（单例注册表 + 按 Class 反射查找 handler）、`CodeGenerator`（单例分发器）——这套"模板→注册表→分发"机制对一个只有 2 种生成器的场景是杀鸡用牛刀。
2. **Java 后端流程当前是断的**：`BackendTemplateHandler.handleTemplateConfig` 里 `buildContext()` 产出的 key（`naming`/`packages` 嵌套 map）与模板实际用的 key（`package`、`entity`、`controller`、`packageMapper`…）对不上，且 `baseParamMap` 根本没传给模板。真正能跑的逻辑在被注释掉的死方法 `generateBaseParamMap` + `generateMeOf*` 里。
3. **模板路径配错**：常量 `classpath:/template/controller_template.ftl`，但文件在 `template/java/controller_template.ftl`；Vue2 同理（少了 `vue2/`）。加载模板会找不到文件。
4. **重复/垃圾代码**：`GlobalConstant` 与 `GeneratorConstants` 内容完全重复；`StringUtils` 与 `GeneratorUtils` 各有一份不同的 `findParams`；`StringUtils` 有带硬编码路径的 `main` 调试方法；`BackendTemplateHandler` 中 7 个 `generateMeOf*` 方法是死代码；`DefaultBackendTemplate.entityList` 字段及其 `entityListBuilder` 从未被读取。

外部依赖核查：除 `modules/pom.xml` 将其声明为子模块外，**项目内无任何其他模块引用 boot-generator**，可自由调整公开 API。

## 目标架构

```
com.mhd.generator
├── JavaGenerator.java          入口 main：构建 BackendConfig → generate()
├── VueGenerator.java           入口 main：构建 Vue2Config → generate()
├── config/
│   ├── BaseTemplate.java       单文件输出规格 DTO（原 core/BaseTemplate，保留 jakarta 校验注解）
│   ├── BackendConfig.java      Java 公共对象（原 DefaultBackendTemplate + BackendTemplateHandler 逻辑合并）
│   └── Vue2Config.java         Vue2 公共对象（原 DefaultVue2Template + Vue2TemplateHandler 逻辑合并，字段由 name 派生）
├── handler/
│   └── CustomMysqlTypeConvert.java   保留（MP 类型转换）
├── util/
│   ├── FileUtils.java          保留
│   ├── GeneratorUtils.java     保留 handleTemplateToFile + findParams；删除对已删类的引用
│   ├── StringUtils.java        删除 main、删除重复 findParams，保留 toUpperCamelCase/toFirstLower/resolveClassPath
│   └── ValidationUtils.java    保留
├── constant/
│   └── GlobalConstant.java     保留，修正模板路径常量
├── exception/
│   └── CodeGeneratorException.java   保留
└── validator/
    └── GroupMybatis.java       保留
```

**删除的类**：`core/ITemplate`、`core/TemplateHandler`、`core/TemplateHandlerManager`、`core/CodeGenerator`、`template/DefaultBackendTemplate`、`template/DefaultVue2Template`、`handler/BackendTemplateHandler`、`handler/Vue2TemplateHandler`、`constant/GeneratorConstants`。整个 `core/` 和 `template/` 包清空后删除。

两个决策（已与用户确认）：
- 彻底移除分发抽象层，Vue2/Java 各自独立，仅共享底层工具（freemarker 渲染、文件IO、校验工具）。
- Vue2 字段由 `name` 自动派生。

## 文件改动清单

### 删除
- `core/ITemplate.java`
- `core/TemplateHandler.java`
- `core/TemplateHandlerManager.java`
- `core/CodeGenerator.java`
- `template/DefaultBackendTemplate.java`（逻辑迁入 `config/BackendConfig.java`）
- `template/DefaultVue2Template.java`（逻辑迁入 `config/Vue2Config.java`）
- `handler/BackendTemplateHandler.java`（逻辑迁入 `config/BackendConfig.java`）
- `handler/Vue2TemplateHandler.java`（逻辑迁入 `config/Vue2Config.java`）
- `constant/GeneratorConstants.java`（与 GlobalConstant 重复）

### 新建
- `config/BaseTemplate.java`：从 `core/BaseTemplate.java` 移入，内容基本不变（`@Getter @Setter @Builder(toBuilder=true)` + jakarta 字段校验）。仅改 package。
- `config/BackendConfig.java`：见下方"BackendConfig 设计"。
- `config/Vue2Config.java`：见下方"Vue2Config 设计"。

### 修改
- `constant/GlobalConstant.java`：修正模板路径常量，java 模板加 `/java/`、vue2 模板加 `/vue2/`。
- `util/GeneratorUtils.java`：`handleTemplateToFile` 保留；移除对 `ITemplate` 等已删类的任何引用（当前未引用，确认即可）；`findParams` 保留（这是过滤 `${r'...'}` 的正确版本）。
- `util/StringUtils.java`：删除 `main` 方法、删除重复的 `findParams` 方法；保留 `resolveClassPath`、`toUpperCamelCase`、`toFirstLower`、`toLowerCamelCase`、`convertPackageToPath`。
- `JavaGenerator.java`：改为 `BackendConfig.createDefault()...generate()`，移除 `entityListBuilder` 调用。
- `VueGenerator.java`：改为 `Vue2Config.createDefault().name("test")...generate()`。

## 关键实现细节

### 1. 修正模板路径常量（GlobalConstant.java）
```java
String DEFAULT_CONTROLLER_TEMPLATE   = DEFAULT_PREFIX + "/template/java/controller_template.ftl";
String DEFAULT_SERVICE_TEMPLATE      = DEFAULT_PREFIX + "/template/java/service_template.ftl";
String DEFAULT_SERVICE_IMPL_TEMPLATE = DEFAULT_PREFIX + "/template/java/service_impl_template.ftl";
String DEFAULT_MAPPER_TEMPLATE       = DEFAULT_PREFIX + "/template/java/mapper_template.ftl";
String DEFAULT_VO_TEMPLATE           = DEFAULT_PREFIX + "/template/java/vo_template.ftl";
String DEFAULT_QUERY_REQ_DTO_TEMPLATE= DEFAULT_PREFIX + "/template/java/query_dto_template.ftl";
String DEFAULT_SAVE_REQ_DTO_TEMPLATE = DEFAULT_PREFIX + "/template/java/save_dto_template.ftl";
String DEFAULT_VUE2_INDEX_TEMPLATE   = DEFAULT_PREFIX + "/template/vue2/default_vue2_index_template.ftl";
String DEFAULT_VUE2_JS_TEMPLATE      = DEFAULT_PREFIX + "/template/vue2/default_vue2_js_template.ftl";
```

### 2. BackendConfig 设计（Java 公共对象）

承载原 `DefaultBackendTemplate` 的配置（GlobalConfig/PackageConfig/DbConfig + 9 个 BaseTemplate 文件规格）+ 原 `BackendTemplateHandler` 的生成逻辑。保留 `createDefault()` 与链式 `xxxBuilder(customizer)` 方法（用户已习惯此 API）。

```java
public class BackendConfig {
    @Valid private GlobalConfig globalConfig;
    @Valid private PackageConfig packageConfig;
    @Valid private DbConfig dbConfig;
    @Valid private BaseTemplate controllerTemplate, serviceTemplate, serviceImplTemplate,
            entityTemplate, xmlTemplate, mapperTemplate,
            queryReqDTOTemplate, saveReqDTOTemplate, voTemplate;
    // 保留内部类 GlobalConfig / PackageConfig / DbConfig（含 jakarta 校验注解）
    // 保留 createDefault() 与 xxxBuilder 链式方法

    public void generate() {
        ValidationUtils.validate(this);                         // 默认组校验
        ValidationUtils.validateByGroup(this, GroupMybatis.class); // DB 相关校验
        List<TableInfo> tableInfos = extractTableMetadata();    // MP 查 DB 元数据
        if (entityTemplate.isEnable()) generateMybatisOfEntity();  // MP 生成 Entity（循环外，一次）
        if (xmlTemplate.isEnable())   generateMybatisOfXml();      // MP 生成 XML（移到循环外，修正原循环内重复生成的 bug）
        for (TableInfo ti : tableInfos) {
            Map<String,Object> base = buildParamMap(ti);        // 扁平 key，匹配模板
            render(mapperTemplate,       base, ti.getEntityName(), "Mapper",       packageConfig.getMapper());
            render(serviceTemplate,      base, ti.getEntityName(), "Service",      packageConfig.getService());
            render(serviceImplTemplate,  base, ti.getEntityName(), "ServiceImpl",  packageConfig.getServiceImpl());
            render(controllerTemplate,   base, ti.getEntityName(), "Controller",   packageConfig.getController());
            render(queryReqDTOTemplate,  base, ti.getEntityName(), "QueryReqDTO",  packageConfig.getQueryReqDTO());
            render(saveReqDTOTemplate,   base, ti.getEntityName(), "SaveReqDTO",   packageConfig.getSaveReqDTO());
            render(voTemplate,           base, ti.getEntityName(), "VO",           packageConfig.getVo());
        }
    }
}
```

**`buildParamMap(TableInfo)`**：恢复死方法 `generateBaseParamMap` 的扁平 key 设计（与 `controller_template.ftl` 等模板一致）：`author`、`date`、`entity`、`entityToLower`、`controller`、`service`、`serviceImpl`、`mapper`、`queryReqDTO`、`saveReqDTO`、`vo` 及对应 `*ToLower`、`packageMapper`/`packageEntity`/.../`packageVO`、`package`（各角色父包）。entityName 等命名取自 `TableInfo`（`getEntityName()`/`getControllerName()` 等）。

**DTO/VO 字段提取改为基于 TableInfo（不再 Class.forName 反射）**：新增 `handleFields(paramMap, List<TableField> fields)`，从 `tableInfo.getFields()` 取属性名与 Java 类型，生成 `fields` 列表与 `imports` 集合，替代原 `generateMeOfQueryReqDTO/SaveReqDTO/VO` 里 `Class.forName(packageEntity)` 的反射方式。这样无需 Entity 已编译即可生成 DTO/VO。

**`render(spec, base, entity, suffix, pkg)`**：
```java
if (!spec.isEnable()) return;
Map<String,Object> params = new HashMap<>(base);
if (MapUtil.isNotEmpty(spec.getParamMap())) params.putAll(spec.getParamMap());
spec.setParamMap(params);
spec.setOutputName(entity + suffix);                         // 如 UserController
spec.setOutputDir(globalConfig.getOutputDir() + "/" + resolvePackage(packageConfig.getParent(), pkg).replace(".", "/"));
GeneratorUtils.handleTemplateToFile(spec);
```
（修复原死代码从未设置 outputDir/outputName 导致 `@NotBlank` 校验必失败的 bug。）

**`generateMybatisOfEntity` / `generateMybatisOfXml` / `generateMybatis`**：从 `BackendTemplateHandler` 原样迁入（已验证可用），仅删除 `generateMybatisOfXml` 返回的未被使用的 `List<String>` 与循环内重复调用。`CustomMysqlTypeConvert` 继续在 `generateMybatis` 中使用。

**移除 `entityList` 字段与 `entityListBuilder`**：从未被读取，删除。

### 3. Vue2Config 设计（Vue2 公共对象）

```java
public class Vue2Config {
    @NotBlank private String name;            // 用户输入，如 "user"
    private String primaryKey = "id";         // 默认 id
    private String urlPath;                   // 为空则由 name 派生
    @NotBlank private String outputDir;       // 输出根目录
    private boolean enableIndex = true;
    private boolean enableJs = true;
    private boolean overwriteExisting = false;
    private Map<String,Object> extraParams = MapUtil.newHashMap(); // 可选覆盖

    public static Vue2Config createDefault() { ... }   // 默认 outputDir = classpath:/vue2 等
    // 链式 setter：name/primaryKey/urlPath/outputDir/enableIndex/enableJs/overwriteExisting/extraParam

    public void generate() {
        ValidationUtils.validate(this);
        // 派生
        String nameUpper = StringUtils.toUpperCamelCase(name);  // user -> User
        String methodName = nameUpper;                          // User
        String url = StrUtil.blankToDefault(urlPath, name);
        Map<String,Object> params = new HashMap<>();
        params.put("name", name);
        params.put("nameToUpper", nameUpper);
        params.put("methodName", methodName);
        params.put("urlPath", url);
        params.put("primaryKey", primaryKey);
        params.putAll(extraParams);
        if (enableIndex) renderIndex(params);
        if (enableJs)    renderJs(params);
    }
}
```
`renderIndex`/`renderJs` 构造 `BaseTemplate`（templatePath 用 `GlobalConstant.DEFAULT_VUE2_*`，outputName="index"，suffix=".vue"/".js"），调用 `GeneratorUtils.handleTemplateToFile`。

修正现状 `VueGenerator` 里 `urlPath: 18`（数字）、`nameToUpper:"test"`（未大写）等错误。

### 4. 入口改造

`JavaGenerator.main`：`BackendConfig.createDefault().globalConfigBuilder(...).packageConfigBuilder(...).dbConfigBuilder(...).controllerBuilder(b->b.enable(true))...generate();`（移除 `entityListBuilder` 与 `CodeGenerator.create().execute(...)`）。

`VueGenerator.main`：`Vue2Config.createDefault().name("test").outputDir(...).generate();`。

## Verification（验证）

1. **编译**：在仓库根执行 `mvn -pl modules/boot-generator -am compile -q`，确认无编译错误、无对已删类的残留引用。
2. **Vue2 端到端**：运行 `VueGenerator.main()`（无需数据库）。校验 `src/main/resources/vue2/index.vue` 与 `.../js/index.js` 生成，且文件内 `nameToUpper`="Test"、`methodName`="Test"、`urlPath`="test"、`primaryKey`="id" 正确替换（无残留 `${...}`、无 `urlPath: 18`）。
3. **Java 端到端**（需本地 MySQL，表 `sys_oper_log`）：运行 `JavaGenerator.main()`。校验：
   - Entity 与 XML 由 MP 生成到 `outputDir` 与 resources/mapper；
   - Mapper/Service/ServiceImpl/Controller/QueryReqDTO/SaveReqDTO/Vo 由 freemarker 生成，类名、包路径、`fields`/`imports` 正确；
   - 模板参数校验通过（不再报"模板参数不匹配/缺少参数"）。
4. **回归确认**：`grep` 全模块确认无 `ITemplate`、`TemplateHandler`、`CodeGenerator`、`GeneratorConstants`、`entityListBuilder` 残留引用。

## 不在本次范围
- 不改动 freemarker 模板内容（`.ftl`）。
- 不调整 `pom.xml` 依赖。
- 不引入新框架；保留 jakarta validation + MP + freemarker 现有技术栈。
