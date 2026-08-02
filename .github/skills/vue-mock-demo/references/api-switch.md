# API 层开关控制模式

本文件详细说明了如何通过 `USE_MOCK` 常量在 `api.ts` 中实现 Mock 数据与真实接口的无缝切换。

## 核心思想

`api.ts` 是视图层与数据层之间的唯一入口。它通过一个简单的布尔常量 `USE_MOCK` 来决定是调用本地的 `mock.ts` 函数还是发送真实的网络请求。

## 代码示例

```typescript
// src/api/user/api.ts

// 1. 引入真实请求客户端
import { requestClient } from '#/api/request';

// 2. 引入 Mock 函数
import { mockGetUserList, mockGetUserDetail } from './mock';

// 3. 引入类型定义
import type { UserItem, UserListResponse, UserListRequest } from './type';

// ✅ 核心开关：只需修改此处即可切换数据源
const USE_MOCK = true;

// --- API 函数定义 ---

/**
 * 获取用户列表
 * 页面组件只调用此函数，不关心数据来源
 */
export async function getUserList(params: UserListRequest): Promise<UserListResponse> {
  if (USE_MOCK) {
    // Mock 模式：调用本地 mock 函数
    return mockGetUserList(params);
  }
  // 真实模式：发送 HTTP 请求
  // ⚠️ TODO [后端对接]: 确认接口路径和请求方式
  return requestClient.post<UserListResponse>('/api/user/list', params);
}

/**
 * 获取用户详情
 */
export async function getUserDetail(id: string): Promise<UserItem> {
  if (USE_MOCK) {
    return mockGetUserDetail(id);
  }
  // ⚠️ TODO [后端对接]: 确认接口路径
  return requestClient.get<UserItem>(`/api/user/${id}`);
}

/**
 * 保存用户（新增/编辑）
 */
export async function saveUser(data: UserItem): Promise<void> {
  if (USE_MOCK) {
    // 可以提供一个空的 mock 实现，或模拟成功/失败
    return Promise.resolve();
  }
  // ⚠️ TODO [后端对接]: 确认接口路径和请求方式
  return requestClient.post<void>('/api/user/save', data);
}
```

## 对接流程

1. 开发阶段，保持 const USE_MOCK = true;。
2. 后端接口就绪后，将 USE_MOCK 改为 false。
3. 根据 // ⚠️ TODO [后端对接] 注释，逐一核对并完善真实的 API 路径、请求方式（GET/POST）和参数。