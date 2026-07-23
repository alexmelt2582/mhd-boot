# 视图层 data.ts 编写规范

本文件指导如何将 `index.vue` 中的表格列定义和查询表单配置抽离到独立的 `data.ts` 文件中，以保持组件的整洁。

## 核心原则

*   **配置与逻辑分离**：将纯配置项（如表格列、表单字段）从组件的 `<script>` 逻辑中移出。
*   **单一职责**：`index.vue` 负责数据获取和事件处理，`data.ts` 负责定义 UI 结构。
*   **易于维护**：修改表格列或搜索项时，只需关注 `data.ts` 文件。

## 代码示例

```typescript
// src/views/system/user/data.ts

import { UserStatusEnum, USER_STATUS_MAP } from '@/api/user/enums';
import type { VxeTableColumn } from 'vxe-table'; // 假设使用 vxe-table
import type { FormSchema } from '#/adapter/form'; // 假设使用 vben 的表单类型

// --- 1. 表格列定义 ---
// 将表格的列配置抽离出来
export const columns: VxeTableColumn[] = [
  { type: 'seq', width: 60, title: '序号' },
  { field: 'username', title: '用户名', minWidth: 150 },
  { field: 'nickname', title: '昵称', minWidth: 150 },
  { field: 'email', title: '邮箱', minWidth: 200 },
  {
    field: 'status',
    title: '状态',
    width: 100,
    slots: {
      // 使用插槽渲染，配合 enums.ts 中的映射
      default: ({ row }) => {
        const status = USER_STATUS_MAP[row.status];
        return [<a-tag color={status.color}>{status.label}</a-tag>];
      },
    },
  },
  { field: 'createTime', title: '创建时间', width: 180 },
  { title: '操作', width: 150, fixed: 'right', slots: { default: 'action' } },
];

// --- 2. 查询表单 Schema ---
// 将搜索表单的字段配置抽离出来
export const searchFormSchema: FormSchema[] = [
  {
    field: 'nickname',
    label: '昵称',
    component: 'Input',
    componentProps: {
      placeholder: '请输入昵称',
      allowClear: true,
    },
  },
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    componentProps: {
      placeholder: '请选择状态',
      allowClear: true,
      options: Object.entries(USER_STATUS_MAP).map(([value, { label }]) => ({
        label,
        value: Number(value),
      })),
    },
  },
];