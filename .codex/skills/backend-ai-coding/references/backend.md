# 后端约定

## 优先参考的代码来源

- `modules/boot-generator/src/main/resources/fm/java/*.ftl`
- `modules/boot-system/...`
- `libs/boot-common-mybatis/...`

## 决策顺序

写代码时按下面顺序取样：

1. 当前业务模块下最近似实现。
2. 当前仓库公共能力模块中的统一约定。
3. generator 模板。
4. 通用 Spring / MyBatis-Plus 默认习惯。

如果规则冲突，优先相信当前仓库真实代码。

## 分层结构

标准 CRUD 代码应优先遵循下面这套结构：

- `entity/Entity.java`
- `model/dto/EntityQueryDTO.java`
- `model/dto/EntitySaveDTO.java`
- `model/vo/EntityVo.java`
- `mapper/EntityMapper.java`
- `service/EntityService.java`
- `service/impl/EntityServiceImpl.java`
- `controller/EntityController.java`

## Entity 规则

- 除非所在模块明显另有约定，否则实体类继承 `com.mhd.boot.common.mybatis.core.domain.BaseEntity`。
- 使用 Lombok `@Data` 和 `@EqualsAndHashCode(callSuper = true)`。
- 使用 `@TableName("table_name")`。
- 主键使用 `@TableId`。
- 存在 `delFlag` 时保留 `@TableLogic`，存在乐观锁字段时保留 `@Version`。
- 如果附近实体已经使用 `@OrderBy` 等额外注解，应继续保持。

## DTO 规则

- 实现 `Serializable`。
- 如果是 SaveDTO 添加 `@AutoMapper(target = Entity.class, reverseConvertGenerate = false)`。
- 在生成器或附近代码已有分组校验时，继续使用：`AddGroup`、`EditGroup`。
- `@Xss`、`@Email`、`@Size`、`@NotBlank`、`@NotNull` 要按真实业务语义添加，不要一股脑全套上。

## VO 规则

- 实现 `Serializable`。
- 添加 `@AutoMapper(target = Entity.class)`。
- 生成器风格的导出对象通常带 `@ExcelIgnoreUnannotated`。
- `@ExcelProperty`、`@ExcelDictFormat`、`ExcelDictConvert`、`@ExcelRequired`、`@ExcelNotation`、`@DateTimeFormat` 只在导入导出场景下使用。

## Mapper 规则

- 默认形式是 `interface XxxMapper extends BaseMapper<Xxx>`。

### Mapper 建议结构

标准 mapper 一般按这个顺序组织：

1. 接口声明
2. 默认查询方法
3. 自定义分页或列表方法
4. 特殊数据权限重写
5. 辅助构造方法

### 什么时候需要 XML

- 复杂联表 SQL 无法仅靠 wrapper 清晰表达时。
- 需要手写查询列和结果映射时。
- 项目当前模块已经大量使用 XML 时。

如果 `BaseMapper + wrapper` 已足够，优先不要补 XML。

## Service 规则

- 类声明通常是 `@RequiredArgsConstructor`、`@Service`，按需补 `@Slf4j`。
- 手写 mapper 注入字段使用具体业务短名；代码生成器模板按类名首字母小写命名。
- 命名时去掉清晰的模块/系统前缀后使用 lowerCamel + `Mapper`，例如 `SysRoleMapper` -> `roleMapper`、`SysDictDataMapper` -> `dictDataMapper`。
- 如果去掉前缀会产生歧义或命名冲突，保留必要前缀。
- 读操作通常返回 `Vo`、`List<Vo>` 或 `BaseResponse<PageInfo<Vo>>`。
- SaveDTO 转实体用 `MapstructUtils.convert(bo, Entity.class)`。
- 查询条件优先返回 `LambdaQueryWrapperX`
- 字符串和空值条件优先用 `likeIfPresent`、`eqIfPresent`
- 分页查询优先采用：
  `Page<XXX> page = MybatisPlusUtils.buildPage(pageParam, null);`
  `LambdaQueryWrapperX<XXX> wrapperX = buildQueryWrapper(queryDTO);`
  `IPage<XXXVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, XXXVo.class);`
  `return PageResultUtils.build(voPage);`
- 生成器风格模块保留 `validSaveDTO(...)` 这种扩展点。
- 多表写操作使用 `@Transactional(rollbackFor = Exception.class)`。
- 明确的业务失败，尤其是权限、数据完整性、删除校验，使用 `BusinessException`。
- 不要绕过模块现有的数据权限、角色校验、删除前校验。

### Service 建议结构

标准 service impl 一般按下面顺序组织：

1. 分页查询
2. 列表查询
3. 构建查询条件
4. 查询单条
5. 新增
6. 修改
7. 删除
8. 新增或修改前校验
9. 其他扩展业务方法

### 查询逻辑建议

- 单表查询优先返回 `LambdaQueryWrapperX`
- 条件判断直接放在 wrapper 链式条件上，不要额外写大量 if 套壳。

### 写入逻辑建议

- SaveDTO 转实体统一走 `MapstructUtils.convert`。
- 批量关系维护时优先拆成私有方法，例如角色、岗位、用户关联。
- 修改前优先保留已有防误删、防越权、防并发覆盖逻辑。

## Controller 规则

- 继承 `BaseController`。
- 类上通常带 `@Validated`、`@RestController`、`@RequiredArgsConstructor`、`@RequestMapping`。
- 返回值使用 `BaseResponse<T>` 或 `BaseResponse<Void>` ，如果是分页则返回 `BaseResponse<PageInfo<T>>`。
- 标准 CRUD 接口通常是：`GET /page`、`POST /export`、`GET /{id}`、`POST`、`PUT`、`DELETE /{ids}`。
- 树表接口通常不分页，`list` 返回 `BaseResponse<List<Vo>>`；
- 写操作、导入导出接口通常加 `@OperateLog(module = "...", type = OperateTypeEnum.X)`。
- 附近接口已有防重时，写接口继续使用 `@RepeatSubmit`。
- 适合分组校验时，使用 `@Validated(AddGroup.class)` 和 `@Validated(EditGroup.class)`。
- 特殊接口直接复用模块内现成做法，例如导入导出、写入前唯一性校验。

### Controller 建议结构

标准 controller 一般按下面顺序组织：

1. 分页列表
2. 导出
3. 详情
4. 新增
5. 修改
6. 删除
7. 特殊接口

### Controller 边界

- controller 负责接参、校验、权限、日志、返回值转换。
- 重业务逻辑尽量放 service，不要在 controller 里堆长逻辑。
- 但前置权限检查、唯一性提示、显式业务失败提示可以留在 controller，前提是同模块已有这种习惯。

## 查询与工具规则

- 分页统一使用 `PageParam` 和 `PageInfo`，不要无故引入新的分页 DTO。
- 优先使用项目工具类：`MapstructUtils`、`StringUtils`、`ValidatorUtils`、`SpringUtils`、`RedisUtils`。
- 数组转列表按附近代码习惯使用 `List.of(ids)` 或 `Arrays.asList(ids)`。
- 构建查询优先识别 `LambdaQueryWrapperX`不要退回临时手写 SQL。

## 缓存与异步/监听规则

- 已有 service 使用 `@Cacheable`、`@CachePut`、`@CacheEvict`、`@Caching` 或 `CacheUtils.evict/clear` 时，新增写操作要同步考虑缓存失效。
- 部门、字典、OSS 配置等模块已有缓存初始化或失效逻辑，不要只改数据库不处理缓存；字典这类模块常同时维护 `CacheNames.SYS_DICT` 与 `CacheNames.SYS_DICT_TYPE`。
- Excel 导入监听器实现 `ExcelListener` 时，保留 `getExcelResult()` 的回执语义和错误聚合方式。
- 定时任务、MQTT、SSE、异步回调等框架方法一般按接口覆写语义实现，除非业务不直观，不要添加冗长注释。

## JavaDoc 注释规则

- 公共 API、接口、VO/BO/Entity 字段、Mapper 默认方法、Service/Controller 方法应有简洁 JavaDoc。
- 注释描述“做什么”和关键参数语义，不复述显而易见的实现细节。
- `void` 方法不要写 `@return`；返回布尔值时说明 `true/false` 含义。
- 私有方法只有在业务规则、算法、映射关系不直观时补注释。
- 框架覆写方法如果只是标准回调，可不重复注释；但当前文件已有统一注释风格时保持一致。
- 只改注释时，不重排 import、不格式化全文件、不修改代码行为。

## 前后端联动规则

- 新增后端接口时，路径和权限前缀尽量保持 generator 约定，方便前端目录和 API 命名同步。
- 导出接口通常保持 `POST /export` 风格，便于前端直接复用现有下载逻辑。
- 批量删除接口通常使用 `DELETE /{ids}`，便于前端直接传数组或逗号串。

## 生成器优先模式

从零新增 CRUD 时，优先对齐生成器默认方法集合：

- `selectPageXXXList`
- `selectXXXList`
- `selectXXXeById`
- `insertXXX`
- `updateXXX`
- `deleteXXXByIds`

然后再叠加模块内已有增强，例如：

- 唯一性校验
- 缓存注解
- Excel 导入导出监听器
- 关联表维护逻辑

## 什么时候优先看 generator

- 新增一个标准单表 CRUD 时。
- 只有表结构和基本接口需求，没有现成业务模块可参考时。
- 需要快速补齐整套骨架代码时。

## 什么时候优先看现有模块

- 目标模块已经有类似业务。
- 涉及数据权限、联表、缓存、角色岗位关系、导入导出、工作流扩展时。
- 任务是“修改已有模块”而不是“新建模块”时。

## 避免事项

- 不要在 controller 里直接暴露 entity 代替 DTO/VO。
- 不要给新的管理接口漏掉权限注解。
- 没有明确必要时，不要从 `BaseMapper` 风格退回手工映射。

## 交付前自检

交付前至少检查这些点：

- CRUD 主链路是否完整。
- DTO / VO / Entity 职责是否清晰。
- 分页、查询、删除校验是否与前端对得上。
- 权限、日志、防重、事务是否遗漏。
- 是否只是 generator 裸产物，如果是，需要继续补齐同模块已有增强。
