import type {BaseResponse} from '@/utils/service'
import type {LoginReqDTO, LoginVO, RegisterReqDTO, UserVO} from './type'
import {mockFail, mockSuccess} from "@/utils/mock.ts";
import {RESPONSE_CODE} from "@/constants/status.ts";

const mockUsers: UserVO[] = [
  {
    id: 1,
    username: 'admin',
    realName: '系统管理员',
    userType: 'TEACHER',
    role: 'SYS_ADMIN',
    phone: '13800000000',
    email: 'admin@library.com',
    avatar: '',
    college: '信息中心',
    creditScore: 100,
    lastLoginTime: '2026-07-25 08:00:00',
    status: 1,
    createTime: '2026-01-01 00:00:00',
    updateTime: '2026-07-25 08:00:00',
  },
  {
    id: 2,
    username: '2021001001',
    realName: '张三',
    userType: 'STUDENT',
    role: 'STUDENT',
    phone: '13900001111',
    email: 'zhangsan@campus.edu',
    avatar: '',
    college: '计算机学院',
    creditScore: 85,
    lastLoginTime: '2026-07-25 09:30:00',
    status: 1,
    createTime: '2026-03-01 00:00:00',
    updateTime: '2026-07-25 09:30:00',
  },
  {
    id: 3,
    username: 'libadmin',
    realName: '图书管理员',
    userType: 'TEACHER',
    role: 'LIB_ADMIN',
    phone: '13800000001',
    email: 'libadmin@library.com',
    avatar: '',
    college: '图书馆',
    creditScore: 100,
    lastLoginTime: '2026-07-25 07:00:00',
    status: 1,
    createTime: '2026-01-15 00:00:00',
    updateTime: '2026-07-25 07:00:00',
  },
]

function getCurrentUser(): UserVO | null {
  // 1. 从 sessionStorage 读取 token
  const token = localStorage.getItem('library_token')
  if (!token) return null

  // 2. 解析 token (格式: mock-token-1-123456)
  // 我们约定 token 的第3部分是 userId
  const parts = token.split('-')
  if (parts.length >= 3) {
    const userId = parseInt(parts[2]!)
    // 3. 根据 userId 查找用户
    return mockUsers.find(u => u.id === userId) || null
  }
  return null
}

export async function mockLogin(data: LoginReqDTO): Promise<BaseResponse<LoginVO>> {
  const user = mockUsers.find((u) => u.username === data.username && u.status === 1)
  if (!user) return mockFail('用户名不存在')
  if (data.password !== '123456') return mockFail('密码错误')
  const token = 'mock-token-' + user.id + '-' + Date.now()
  return mockSuccess({token, user})
}

export async function mockRegister(data: RegisterReqDTO): Promise<BaseResponse<null>> {
  const exists = mockUsers.find(u => u.username === data.username)
  if (exists) return mockFail('用户名已存在')
  const maxId = Math.max(...mockUsers.map(d => d.id), 0)

  const newUser: UserVO = {
    id: maxId + 1,
    username: data.username,
    realName: data.realName || '新用户',
    userType: 'STUDENT',
    role: 'STUDENT',
    phone: '',
    email: '',
    avatar: '',
    college: '',
    creditScore: 100,
    status: 1,
    createTime: new Date().toLocaleString(),
    updateTime: new Date().toLocaleString(),
    lastLoginTime: ''
  }
  mockUsers.push(newUser)
  return mockSuccess(null)
}

export async function mockGetUserInfo(): Promise<BaseResponse<UserVO>> {
  const user = getCurrentUser()
  if (!user) return mockFail('未登录或登录已过期', RESPONSE_CODE.UNAUTHORIZED)
  return mockSuccess(user)
}

export async function mockLogout(): Promise<BaseResponse<null>> {
  return mockSuccess(null)
}

export async function mockUpdateProfile(data: Partial<UserVO>): Promise<BaseResponse<null>> {
  const user = getCurrentUser()
  if (!user) return mockFail('未登录或登录已过期', RESPONSE_CODE.UNAUTHORIZED)

  Object.assign(user, data)
  user.updateTime = new Date().toLocaleString()
  return mockSuccess(null)
}

export async function mockChangePassword(data: {
  oldPassword: string
  newPassword: string
}): Promise<BaseResponse<null>> {
  const user = getCurrentUser()
  if (!user) return mockFail('未登录或登录已过期', RESPONSE_CODE.UNAUTHORIZED)

  // 简单校验逻辑：假设默认密码是 123456
  if (data.oldPassword !== '123456') return mockFail('原密码错误')

  return mockSuccess(null)
}

export {mockUsers}
