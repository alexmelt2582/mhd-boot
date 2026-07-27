import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { UserManageVO, UserQuery, CreateUserDTO, UpdateUserDTO, UpdateUserStatusDTO, AssignRoleDTO } from './type'
import {
  mockGetUserList,
  mockGetUserById,
  mockCreateUser,
  mockUpdateUser,
  mockDeleteUser,
  mockUpdateUserStatus,
  mockAssignRole,
  mockBatchImportUsers,
} from './mock'

/** 获取用户列表（分页 + 筛选） */
export function getUserList(query: UserQuery): Promise<BaseResponse<PageResponse<UserManageVO>>> {
  if (USE_MOCK) return mockGetUserList(query)
  return service({ url: '/api/admin/users', method: 'get', params: query }) as any
}

/** 根据ID获取用户详情 */
export function getUserById(id: number): Promise<BaseResponse<UserManageVO>> {
  if (USE_MOCK) return mockGetUserById(id)
  return service({ url: `/api/admin/users/${id}`, method: 'get' }) as any
}

/** 创建用户 */
export function createUser(data: CreateUserDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockCreateUser(data)
  return service({ url: '/api/admin/users', method: 'post', data }) as any
}

/** 更新用户 */
export function updateUser(data: UpdateUserDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUpdateUser(data)
  return service({ url: `/api/admin/users/${data.id}`, method: 'put', data }) as any
}

/** 删除用户 */
export function deleteUser(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteUser(id)
  return service({ url: `/api/admin/users/${id}`, method: 'delete' }) as any
}

/** 更新用户状态（启用/禁用/锁定） */
export function updateUserStatus(data: UpdateUserStatusDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUpdateUserStatus(data)
  return service({ url: `/api/admin/users/${data.userId}/status`, method: 'put', data: { status: data.status } }) as any
}

/** 分配角色 */
export function assignRole(data: AssignRoleDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockAssignRole(data)
  return service({ url: `/api/admin/users/${data.userId}/role`, method: 'put', data: { role: data.role } }) as any
}

/** 批量导入用户 */
export function batchImportUsers(data: { users: CreateUserDTO[] }): Promise<BaseResponse<{ success: number; fail: number }>> {
  if (USE_MOCK) return mockBatchImportUsers(data)
  return service({ url: '/api/admin/users/batch-import', method: 'post', data }) as any
}
