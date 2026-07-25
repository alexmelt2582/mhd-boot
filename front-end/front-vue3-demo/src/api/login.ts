import type { BaseResponse } from '@/utils/service'
import service from '@/utils/service'
import type { SystemAuthLoginReqDTO, SystemAuthLoginVO } from '@/api/type.ts'

export async function login<T = BaseResponse<SystemAuthLoginVO>>(user: SystemAuthLoginReqDTO): Promise<T>  {
  return service({
    url: 'api/auth/login',
    method: 'post',
    data: user
  })
}

export async function logout<T = BaseResponse>(): Promise<T> {
  return service({
    url: 'api/auth/logout',
    method: 'delete'
  })
}

export async function register<T = BaseResponse>(data: any): Promise<T> {
  return service({
    url: 'api/auth/register',
    method: 'post',
    data
  })
}

export async function getLoginInfo<T = BaseResponse>(): Promise<T> {
  return service({
    url: 'api/auth/info',
    method: 'get'
  })
}
