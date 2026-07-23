# 类型定义最佳实践

本文件规定了 `type.ts` 的编写标准，它是 Mock 数据和真实接口的共同契约。

## 核心原则

*   **类型先行**：在编写任何逻辑代码前，必须先定义好 TypeScript 接口。
*   **精准定义**：避免使用 `any`，为每个字段提供准确的类型。
*   **结构清晰**：将请求参数、响应数据和实体模型分开定义。

## 代码示例

```typescript
// src/api/user/type.ts

// --- 1. 实体模型 (Entity) ---
// 定义单条数据的核心结构
export interface UserItem {
  id: string;
  username: string;
  nickname: string;
  email: string;
  status: number; // 对应 UserStatusEnum
  createTime: string;
}

// --- 2. 请求参数 (Request) ---
// 定义 API 调用时传入的参数结构
export interface UserListRequest {
  current?: number; // 当前页码
  size?: number;    // 每页大小
  nickname?: string; // 搜索条件：昵称
  status?: number;   // 搜索条件：状态
}

// --- 3. 响应数据 (Response) ---
// 定义 API 返回的数据结构

// 统一的分页响应结构
export interface PageResponse<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

// 获取用户列表的响应
export type UserListResponse = PageResponse<UserItem>;

// 获取用户详情的响应（直接返回实体）
export type UserDetailResponse = UserItem;
```