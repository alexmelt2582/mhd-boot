# 项目编码规范

Spring Boot 3 + Java 17 + Spring AI + MyBatis-Plus + MySQL8 + Vue3 项目。写代码时必须遵守以下规则。

---

## 一、项目结构

单模块 Gradle 项目，按功能分包：

```

```

---

## 二、后端技术栈约束

- 核心框架：Java 17 + Spring Boot 3 + MyBatis-Plus + MySQL8 + Redisson + Spring AI
- 构建工具：Maven
- 工具链：Lombok, MapStruct, SLF4J + @Slf4j

---

## 三、前端技术栈约束

- 核心框架：Vue3 + TypeScript + Vite + TailwindCSS 4（`front-end/` 目录）



## 命名与代码风格
- 严格遵循《阿里巴巴 Java 开发手册》。
- 类名：大驼峰（PascalCase），如 `UserService`。
- 方法/变量：小驼峰（camelCase），动词+名词，如 `getUserById`。
- 常量：全大写下划线分隔（UPPER_SNAKE_CASE）。
- 禁止魔法值：所有固定参数必须定义在常量类或枚举中。
- 不可变数据载体优先用 `record`

## 3. 全局安全与异常红线
- 统一返回格式：所有接口必须返回 `Result<T>`，禁止直接返回裸实体、List 或 Map。
- 异常处理：统一由全局异常处理器（`@RestControllerAdvice`）捕获，业务异常使用 `BusinessException`，禁止在 Controller 层随意 try-catch 吞掉异常。
- 安全红线：禁止硬编码 SQL 或拼接字符串，必须使用参数化查询或 LambdaQueryWrapper，杜绝 SQL 注入。

## 4. 模块文档优先读取规则
- 在修改 `libs` 下公共模块或 `apps` 业务代码前，优先读取 `docs/ai-module-guides/` 中对应模块文档。
- 处理 `boot-common-security` 时优先读取 `docs/ai-module-guides/boot-common-security.md`。
- 处理 `boot-common-redis` 时优先读取 `docs/ai-module-guides/boot-common-redis.md`。
- 处理 `boot-common-mybatis` 时优先读取 `docs/ai-module-guides/boot-common-mybatis.md`。
- 处理 `boot-common-web` 时优先读取 `docs/ai-module-guides/boot-common-web.md`。
- 处理 `boot-common-sse` 时优先读取 `docs/ai-module-guides/boot-common-sse.md`。
- 处理 `boot-common-sftp` 时优先读取 `docs/ai-module-guides/boot-common-sftp.md`。
- 处理 `boot-common-idempotent` 时优先读取 `docs/ai-module-guides/boot-common-idempotent.md`。
- 处理 `boot-common-operatelog` 时优先读取 `docs/ai-module-guides/boot-common-operatelog.md`。
- 处理 `boot-common-job` 时优先读取 `docs/ai-module-guides/boot-common-job.md`。
- 处理 `boot-common-doc` 时优先读取 `docs/ai-module-guides/boot-common-doc.md`。
- 处理基础公共能力时优先读取 `docs/ai-module-guides/boot-common.md`。
- 若文档与源码不一致，以源码为准，并在完成代码后同步更新文档。
