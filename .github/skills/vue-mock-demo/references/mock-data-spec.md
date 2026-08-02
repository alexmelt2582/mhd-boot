# Mock 数据编写规范

本文件定义了 `mock.ts` 中模拟数据的编写标准，确保数据真实、可靠，能有效支撑前端开发。

## 核心原则

1.  **签名一致**：Mock 函数的参数和返回值类型必须与 `api.ts` 中定义的真实接口完全一致。
2.  **数据逼真**：数据应尽可能模拟真实业务场景，避免使用 `test1`, `abc` 等无意义占位符。
3.  **覆盖全面**：Mock 数据应覆盖列表、详情、分页、搜索等所有相关场景。

## 代码示例

```typescript
// src/api/<module>/mock.ts

import type { XxxItem, XxxListResponse, YyyyRequest } from './type';
import { XxxStatusEnum } from './enums'; // 使用枚举，保持一致性

// --- 1. 静态 Mock 数据源 ---
// 模拟一个包含多条数据的列表，用于测试分页和滚动
const MOCK_XXX_LIST: XxxItem[] = [
    {
        id: '1',
        name: '示例数据 A',
        status: 1,
        createTime: '2025-01-01 10:00:00',
    },
    {
        id: '2',
        name: '示例数据 B',
        status: 2,
        createTime: '2025-01-02 11:30:00',
    },
];

// --- 2. 工具函数 ---
// 模拟网络延迟，让加载状态可见
const delay = (ms: number = 300) => new Promise((resolve) => setTimeout(resolve, ms));

// --- 3. Mock 实现函数 ---

/**
 * 模拟获取用户列表（含分页、搜索）
 */
export async function mockGetUserList(params: YyyyRequest): Promise<XxxListResponse> {
  await delay(500); // 模拟 500ms 网络延迟

  let filteredList = [...MOCK_USER_LIST];

  // 模拟搜索过滤
  if (params.nickname) {
    filteredList = filteredList.filter((u) => u.nickname.includes(params.nickname!));
  }
  if (params.status !== undefined) {
    filteredList = filteredList.filter((u) => u.status === params.status);
  }

  // 模拟分页
  const { current = 1, size = 10 } = params;
  const start = (current - 1) * size;
  const records = filteredList.slice(start, start + size);

  return {
    records,
    total: filteredList.length,
    current,
    size,
  };
}

/**
 * 模拟获取用户详情
 */
export async function mockGetUserDetail(id: string): Promise<XxxItem> {
  await delay(200);
  const user = MOCK_USER_LIST.find((u) => u.id === id);
  if (!user) {
    return Promise.reject(new Error('用户不存在'));
  }
  return user;
}
```