import type { BaseResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { LoginReqDTO, LoginVO, RegisterReqDTO, UserVO } from './type'
import {
  mockLogin,
  mockRegister,
  mockGetUserInfo,
  mockLogout,
  mockUpdateProfile,
  mockChangePassword,
} from './mock'

export async function login(data: LoginReqDTO): Promise<BaseResponse<LoginVO>> {
  if (USE_MOCK) return mockLogin(data)
  return service({ url: '/api/auth/login', method: 'post', data }) as any
}

export async function register(data: RegisterReqDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockRegister(data)
  return service({ url: '/api/auth/register', method: 'post', data }) as any
}

export async function getUserInfo(): Promise<BaseResponse<UserVO>> {
  if (USE_MOCK) return mockGetUserInfo()
  return service({ url: '/api/auth/info', method: 'get' }) as any
}

export async function logout(): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockLogout()
  return service({ url: '/api/auth/logout', method: 'delete' }) as any
}

export async function updateProfile(data: Partial<UserVO>): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUpdateProfile(data)
  return service({ url: '/api/auth/profile', method: 'put', data }) as any
}

export async function changePassword(data: {
  oldPassword: string
  newPassword: string
}): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockChangePassword(data)
  return service({ url: '/api/auth/password', method: 'put', data }) as any
}
