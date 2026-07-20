# 公共模块 AI 开发指南（排除 boot-ai）

本目录用于给 AI 提供模块级知识索引。目标不是写给人看的简短摘要，而是让 AI 在处理对应模块需求时，先理解模块目的、边界、运行方式和典型用法，再开始改代码。

## 适用范围
- 覆盖 `libs` 下非 AI 公共模块
- 已排除 `libs/boot-ai`

## 文档列表
- [boot-common.md](boot-common.md)
- [boot-common-web.md](boot-common-web.md)
- [boot-common-mybatis.md](boot-common-mybatis.md)
- [boot-common-redis.md](boot-common-redis.md)
- [boot-common-security.md](boot-common-security.md)
- [boot-common-idempotent.md](boot-common-idempotent.md)
- [boot-common-operatelog.md](boot-common-operatelog.md)
- [boot-common-job.md](boot-common-job.md)
- [boot-common-sftp.md](boot-common-sftp.md)
- [boot-common-sse.md](boot-common-sse.md)
- [boot-common-doc.md](boot-common-doc.md)

## 跨模块优先级建议
1. Web API 基础组合：`boot-common + boot-common-web + boot-common-mybatis`
2. 缓存/分布式能力：`boot-common-redis`
3. 认证鉴权：`boot-common-security`
4. 防重复提交：`boot-common-idempotent`
5. 操作审计：`boot-common-operatelog`
6. 定时任务：`boot-common-job`
7. 实时推送：`boot-common-sse`
8. 文件传输：`boot-common-sftp`
9. 接口文档：`boot-common-doc`

## 写作原则
- 以模块用途、边界、流程、异常、示例为主，不写成空泛简介
- 示例要足够完整，能让 AI 直接据此生成业务代码
- 测试信息只用于帮助理解模块，不要求 AI 先回看测试类
- 若源码更新，文档必须同步更新
