# 枚举提取规范

本文件规定了如何将页面中散落的“魔法数字”和状态字典提取到 `enums.ts` 文件中，以便于统一管理和后续对接后端字典接口。

## 核心原则

*   **消除魔法数字**：禁止在模板或逻辑中直接使用 `row.status === 1` 这样的硬编码。
*   **集中管理**：所有与业务状态相关的常量、标签映射、下拉选项都应在 `enums.ts` 中定义。
*   **易于替换**：为未来从静态枚举切换到后端字典 API 做好准备。

## 代码示例

```typescript
// src/views/system/user/enums.ts
// 或者 src/api/user/enums.ts，根据你的项目结构决定

// --- 1. 状态枚举 (Enum) ---
// 使用 `as const` 确保值的类型被推断为字面量类型
export const UserStatusEnum = {
  ENABLED: 1,
  DISABLED: 0,
  PENDING: 2,
} as const;

// 从枚举对象中提取出值的联合类型: 1 | 0 | 2
export type UserStatus = (typeof UserStatusEnum)[keyof typeof UserStatusEnum];

// --- 2. 状态展示映射 (Map) ---
// 用于在表格、标签等地方根据状态值渲染对应的文本和颜色
export const USER_STATUS_MAP: Record<
  UserStatus,
  { label: string; color: string }
> = {
  [UserStatusEnum.ENABLED]: { label: '启用', color: 'success' },
  [UserStatusEnum.DISABLED]: { label: '禁用', color: 'error' },
  [UserStatusEnum.PENDING]: { label: '待审核', color: 'warning' },
};

// --- 3. 下拉选项生成器 (Options) ---
// 用于生成 Select、RadioGroup 等组件的 options 属性
// ⚠️ TODO [后端对接]: 后端字典接口就绪后，将此函数替换为 API 调用
// 例如: return getDictOptions('user_status')
export function getUserStatusOptions() {
  return Object.entries(USER_STATUS_MAP).map(([value, { label }]) => ({
    label,
    value: Number(value),
  }));
}
```

## 在 Vue 组件中使用

```vue

<script setup lang="ts">
  import {getUserStatusOptions} from './enums';
  // ...
  const statusOptions = getUserStatusOptions();
</script>

<template>
  <!-- ✅ 正确：使用枚举常量 -->
  <el-tag v-if="record.status === UserStatusEnum.ENABLED" color="success">
    启用
  </el-tag>

  <!-- ✅ 正确：使用映射对象 -->
  <el-tag :color="USER_STATUS_MAP[record.status].color">
    {{ USER_STATUS_MAP[record.status].label }}
  </el-tag>

  <!-- ✅ 正确：使用选项生成器 -->
  <el-select :options="statusOptions"/>
</template>
```