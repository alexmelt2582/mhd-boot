# idempotent 幂等

## 执行原理

基于 AOP 切面拦截 `@Idempotent` 注解，通过存储幂等键（Key）来防止重复请求，执行流程如下：

```
请求到达
    ↓
拦截 @Idempotent 注解
    ↓
根据 keyGenerator 获取 Key 生成器 ←——【可扩展点 ①：自定义 Key 生成逻辑】
    ↓
生成幂等 Key（如：order:12345）
    ↓
尝试存储 Key 到存储介质 ←——【可扩展点 ②：自定义存储实现】
    ↓
    ├—→ 存储失败（Key已存在）——→ 抛出 IdempotentException 异常 ——→ 返回提示信息
    ↓
存储成功（Key不存在）
    ↓
执行业务逻辑
    ↓
    ├—→ 业务正常完成 ——→ 根据 removeKeyWhenFinished 决定是否删除 Key
    │
    └—→ 业务执行异常 ——→ 根据 removeKeyWhenError 决定是否删除 Key
```

- 应用启动时，根据配置文件中 `mhd.idempotent.key-store-type` 选择存储方式：
    - **memory**：基于内存的存储，使用 Hutool 的 TimedCache 实现，适合单机部署
    - **redis**：基于 Redis 的分布式存储，适合集群部署（注意：当前版本未添加分布式锁）
    - **自定义存储**：支持扩展实现 `IdempotentKeyStore` 接口
- **默认生成器**：`DefaultIdempotentKeyGenerator`，通过 Spring Bean 方式自动注册
- **Key 格式规则**：
    - 当 `uniqueExpression` 存在时：`{prefix}:{uniqueExpression计算值}`
        - 示例：`@Idempotent(prefix = "order", uniqueExpression = "#orderId")` → `order:12345`
    - 当 `uniqueExpression` 为空时：`{prefix}`
        - 示例：`@Idempotent(prefix = "submit")` → `submit`（适用于全局幂等场景）
- **SpEL 表达式支持**：可从方法参数、Spring 上下文中提取动态值
- **自定义生成器**：支持实现 `IdempotentKeyGenerator` 接口自定义 Key 生成逻辑（如基于 IP、用户ID等）

⚠️ **存储方式选择建议**：

- **单机应用**：使用 `memory` 存储即可，性能更好
- **分布式/集群应用**：必须使用 `redis` 存储，确保跨节点幂等生效

⚠️ **时长配置注意**：

- `duration` 必须设置为大于业务实际执行时间，否则可能在业务执行中幂等键过期，导致重复请求通过
- 建议设置为业务耗时的 2-3 倍作为安全边界

⚠️ **Redis 模式说明**：

- 当前版本未实现分布式锁机制，高并发场景下极小概率出现竞态条件
- 如需严格的分布式锁，建议后续版本引入 Redisson 等方案

## 基本使用

- 在 pom.xml 中引入依赖

```xml
<dependency>
    <groupId>com.mhd.plugin</groupId>
    <artifactId>mhd-idempotent-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

- 修改 application.yml

```yml
mhd:
  idempotent:
    # 存储Key的方式，redis、memory
    key-store-type: redis
```

- 若使用 redis，还需额外添加 redis 连接池的相关配置

```yml
# redis相关，使用原生spring中redis
spring:
  data:
    redis:
      host: localhost
      port: 6379
      ...
```

- 在需要幂等处理的 Controller 上添加 @Idempotent 注解即可。

该注解具有以下基本属性:

| 注解值                   | 默认值                                                | 说明                                                                                                                       |
|-----------------------|----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| prefix                | idem                                               | 业务标识。作为幂等标识的前缀，可用于区分服务和业务，防止 key 冲突。完整的幂等标识 = {prefix}:{uniqueExpression.value}                                          |
| uniqueExpression      | -                                                  | 幂等的唯一性标识。值为 SpEL 表达式，从上下文中提取幂等的唯一性标识                                                                                     |
| keyGenerator          | DefaultIdempotentKeyGenerator.class(默认解析 SpEL 表达式) | Key生成器类型，用户通过该属性实现自定义 Key 生成器                                                                                            |
| duration              | 1                                                  | 幂等的控制时长。必须大于业务的处理耗时，其值为幂等 key 的标记时长，超过标记时间，则幂等 key 可再次使用，此时间需自行评估，保证过期时间大于业务执行时间                                         |
| timeUnit              | TimeUnit.SECONDS                                   | 控制时长单位。默认为 SECONDS 秒                                                                                                     |
| info                  | 重复请求，请稍后重试                                         | 正在执行中的提示信息                                                                                                               |
| removeKeyWhenFinished | false(不处理)                                         | 是否在业务完成后立刻清除幂等 key。建议保持默认配置，即使业务执行完，也不删除 key，强制锁 expireTime 的时间。预防出现第一个业务请求还在执行时，若前端未做遮罩，或者用户跳转页面后再回来做重复请求等短时间内重复发起请求的情况 |
| removeKeyWhenError    | false(不处理)                                         | 是否在业务执行异常时立刻清除幂等 key                                                                                                     |

使用示例：

```java

@GetMapping("/get")
@Idempotent(uniqueExpression = "#key", duration = 3, info = "请勿重复查询")
public String get(String key) throws Exception {
    Thread.sleep(2000L);
    return "success";
}
```

## 触发异常

触发幂等拦截时会抛出 IdempotentException,如果要进行友好提示的话通过 Spring 全局异常处理器拦截该异常即可

## Key生成器扩展

### 默认生成器说明

默认使用 `DefaultIdempotentKeyGenerator`（`generator.key.core.com.mhd.common.idempotent.DefaultIdempotentKeyGenerator`）：

- 当 `uniqueExpression` 存在时，Key格式为：`{prefix}:{uniqueExpression计算值}`
- 当 `uniqueExpression` 为空时，Key格式为：`{prefix}`

### 自定义生成器

#### 实现步骤

1. 创建自定义类实现 `IdempotentKeyGenerator` 接口
2. 使用 `@Component` 注解将其注册为 Spring Bean
3. 在 `@Idempotent` 注解中通过 `keyGenerator` 属性指定使用该生成器

**⚠️ 重要提示**：`generate()` 方法返回值不能为 `null`，否则会导致幂等逻辑失败

#### 使用场景示例

##### 场景1：基于 IP 地址的幂等控制

适用于限制同一 IP 的重复请求（如防刷接口）

```java

@Component
public class IPKeyGenerator implements IdempotentKeyGenerator {

    @Override
    @NonNull
    public String generate(JoinPoint joinPoint, Idempotent idempotentAnnotation) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String clientIP = getClientIP(request);
            // 生成格式：{prefix}:IP:{ip地址}
            return idempotentAnnotation.prefix() + ":IP:" + clientIP;
        }
        throw new IllegalStateException("无法获取请求上下文");
    }

    private String getClientIP(HttpServletRequest request) {
        // 获取真实IP的逻辑（处理代理、负载均衡等情况）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

使用示例：

```java

@GetMapping("/submit")
@Idempotent(keyGenerator = IPKeyGenerator.class, duration = 60, info = "同一IP每分钟只能提交一次")
public String submit() {
    // 业务逻辑
    return "success";
}
```

## Key存储扩展

### 内置存储方式

目前内置了以下两种存储方式：

- **memory**：基于 Hutool 的 `TimedCache` 实现，适合单机部署
- **redis**：基于 Spring Data Redis 实现，适合分布式/集群部署

### 自定义存储

#### 实现步骤

1. 创建自定义类实现 `IdempotentKeyStore` 接口（`store.key.core.com.mhd.common.idempotent.IdempotentKeyStore`）
2. 使用 `@Component`注解将其注册为 Spring Bean

**⚠️ 重要提示**：自定义实现类必须被 Spring 容器管理，否则无法生效
