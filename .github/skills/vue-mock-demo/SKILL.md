---
name: vue-mock-demo
description: Vue3 前端 Mock 数据演示层构建专家。用于在无后端情况下开发或展示 Vue3 页面 demo，实现 Mock 与真实接口的“一键切换”。
---

# Vue Mock Demo 构建指南

构建与真实 API 层保持完全相同接口签名的 Mock 数据层，实现前端开发零依赖后端，并支持无缝切换。
不感知数据来源。等后端就绪，只需要在 API 层把 mock 数据替换为真实 HTTP 请求，Vue 页面零改动。

## 何时使用

- 用户要求“先做前端 demo”或“后端还没好”。
- 需要用 Mock 数据、假数据、静态数据展示页面效果。
- 已有页面需要将 API 调用改为 Mock 模式进行调试。
- 需要将页面中散落的枚举/字典值提取到独立文件以便后续替换。
- 目标是实现 Mock 和真实接口共用同一套 API 调用层，后续对接后端时只需打开真实请求。

## 何时不适用

- 后端接口已就绪，可直接进行真实数据联调。
- 纯静态展示页面，不涉及任何 API 数据交互。
- 不需要考虑后续与后端接口对接的临时性原型。

## 快速开始

1.  **创建 API 模块**：在 `src/api/` 下建立对应的模块文件夹，包含 `api.ts`, `type.ts`, `mock.ts`。
2.  **创建视图模块**：在 `src/views/` 下建立对应的模块文件夹，包含 `index.vue`, `data.ts` 等。
3.  **定义类型契约**：优先编写 `src/api/.../type.ts`，定义请求和响应的数据结构。
4.  **编写 Mock 数据**：在 `src/api/.../mock.ts` 中基于类型定义填充模拟数据。
5.  **配置 API 开关**：在 `src/api/.../api.ts` 中通过 `USE_MOCK` 常量控制数据来源。
6.  **配置视图数据**：在 `src/views/.../data.ts` 中定义表格列和查询表单 Schema。
7.  **组装页面**：在 `src/views/.../index.vue` 中调用 API 并渲染视图。

## 核心架构与规则

### 目录结构规范

对于每个功能模块，在 `src/api/` 下建立如下结构：多级模块采用 `src/views/<module>/<sub-module>/` 形式。

| 文件 | 描述 |
| :--- | :--- |
| `api.ts` | 对外暴露的 API 函数（唯一入口，页面只引这里） |
| `type.ts` | TypeScript 类型定义（请求/响应体） |
| `mock.ts` | Mock 数据及实现函数 |

#### 视图层结构

对于每个功能模块，在 `src/views/` 下建立如下结构。多级模块采用 `src/views/<module>/<sub-module>/` 形式。

| 文件 | 描述 |
| :--- | :--- |
| `index.vue` | 列表主页（含搜索表单 + 表格 + 操作按钮） |
| `data.ts` | 表格列定义 + 查询表单 schema |
| `enums.ts` | 枚举/字典常量（可选） |
| `modal.vue` | 新增/编辑弹窗（可选） |
| `import-modal.vue` | 导入弹窗（可选，有导入功能时才创建） |
对于每个功能模块，在 `src/api/` 下建立如下结构：

### 核心开发原则

- **接口签名一致**：Mock 函数的入参和返回类型必须和真实 API 函数完全相同。
- **类型先行**：一定要先写 `type.ts`，再写 Mock 数据。类型是 Mock 和真实接口的契约。
- **一键切换**：页面代码永远只调用 `api.ts` 导出的函数。后端就绪后，只需在 `api.ts` 中将 `USE_MOCK` 改为 `false`，页面代码零改动。
- **配置与视图分离**：将 `index.vue` 中的表格列定义和查询表单 schema 抽离到 `data.ts` 中。
- **枚举提取**：将 `index.vue` 中的状态值、类型值、字典值提取到 `enums.ts`，方便后续替换为后端字典表接口。
- **数据逼真**：Mock 数据要“像真的”，日期用真实格式，数字字段用合理范围，列表数据量足够测试分页。

## 参考文件

| 文件名称 | 描述与用途 |
| :--- | :--- |
| `references/api-switch.md` | API 层开关控制模式，详解 `USE_MOCK` 常量的使用方法与真实接口 URL 的注释规范。 |
| `references/mock-data-spec.md` | Mock 数据编写规范，包含数据结构一致性、模拟网络延迟及分页逻辑的实现细节。 |
| `references/type-definition.md` | 类型定义最佳实践，涵盖请求参数、单条数据及列表响应的统一分页结构接口设计。 |
| `references/view-data-config.md` | 视图层 `data.ts` 编写规范，指导如何抽离表格列定义和查询表单 schema。 |
| `references/enum-extraction.md` | 枚举提取规范，指导如何将页面中的魔法数字、状态映射及下拉选项提取到独立文件。 |
| `references/demo-rules.md` | Demo 实战补充规则，包含页面风格对齐、组件风格统一及路由菜单一致性检查的强制要求。 |

## 数据流模式

| 层级 | 责任 | 示例 |
| :--- | :--- | :--- |
| **View (index.vue)** | 调用 API 层函数，渲染视图 | `import { getXxxList } from '@/api/xxx/api'` |
| **View Data (data.ts)** | 定义表格列和查询表单配置 | `export const columns = [...]` |
| **API (api.ts)** | 控制数据来源开关 | `if (USE_MOCK) return mockGetXxx(params)` |
| **Mock (mock.ts)** | 返回模拟数据 | `return Promise.resolve({ records: [], total: 0 })` |
| **Real (requestClient)** | 发送真实 HTTP 请求 | `requestClient.get('/api/xxx', { params })` |

## 脚本与工具

| 脚本/工具 | 目的 |
| :--- | :--- |
| `USE_MOCK` 常量 | 在 `api.ts` 中控制使用 Mock 数据还是真实接口。 |
| `delay` 函数 | 在 `mock.ts` 中模拟网络延迟，测试页面加载状态。 |
| `getXxxStatusOptions` | 在 `enums.ts` 中生成下拉选项，后续可替换为字典表 API。 |

## 后端对接清单

当后端接口就绪时，按以下步骤切换，Vue 页面无需改动：

- [ ] 1. 打开 `api.ts`，将 `USE_MOCK = true` 改为 `USE_MOCK = false`。
- [ ] 2. 核对每个函数里注释的真实接口 URL 是否正确。
- [ ] 3. 核对请求方式（GET/POST）和参数位置（params/body）。
- [ ] 4. 对比 `type.ts` 中的响应类型与后端实际返回字段名，调整字段映射。
- [ ] 5. 将 `enums.ts` 中标注 `TODO [后端对接]` 的 options 函数替换为字典表 API。
- [ ] 6. 删除 `mock.ts` 文件（可选，也可保留用于测试）。

## 最佳实践

1. **页面风格对齐**：开始写页面前，先找 1~2 个同业务域的已上线页面作为视觉和交互基线。
2. **组件风格统一**：同一模块内禁止混用多套主表格范式（如一部分 `a-table`、一部分 `vxe-grid`）。
3. **Mock 与表结构强一致**：`type.ts` 的实体字段必须与用户给定表结构逐项一致，`mock.ts` 要覆盖所有字段。
4. **路由与菜单一致性**：新增页面后检查路由路径、组件路径与菜单来源是否对齐。
5. **最低验证清单**：确保页面可访问、可展示（表格有数据、分页可用）、可操作（增删改查验证）、可切换（`USE_MOCK` 开关有效）。