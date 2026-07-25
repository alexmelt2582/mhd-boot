import type { BaseResponse } from '@/utils/service'
import type { LoginReqDTO, LoginVO, RegisterReqDTO, UserVO } from './type'

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

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

export async function mockLogin(data: LoginReqDTO): Promise<BaseResponse<LoginVO>> {
  await delay()
  const user = mockUsers.find((u) => u.username === data.username && u.status === 1)
  if (!user) {
    return { code: 1001, msg: '用户名不存在', data: null as any }
  }
  if (data.password !== '123456') {
    return { code: 1002, msg: '密码错误', data: null as any }
  }
  const token = 'mock-token-' + user.id + '-' + Date.now()
  return { code: 0, msg: '登录成功', data: { token, user } }
}

export async function mockRegister(data: RegisterReqDTO): Promise<BaseResponse<null>> {
  await delay()
  return { code: 0, msg: '注册成功', data: null }
}

export async function mockGetUserInfo(): Promise<BaseResponse<UserVO>> {
  await delay()
  return { code: 0, msg: 'ok', data: mockUsers[1] }
}

export async function mockLogout(): Promise<BaseResponse<null>> {
  await delay()
  return { code: 0, msg: '已退出', data: null }
}

export async function mockUpdateProfile(data: Partial<UserVO>): Promise<BaseResponse<null>> {
  await delay()
  const user = mockUsers[1]
  Object.assign(user, data)
  return { code: 0, msg: '更新成功', data: null }
}

export async function mockChangePassword(data: {
  oldPassword: string
  newPassword: string
}): Promise<BaseResponse<null>> {
  await delay()
  if (data.oldPassword !== '123456') {
    return { code: 1003, msg: '原密码错误', data: null }
  }
  return { code: 0, msg: '密码修改成功', data: null }
}

export { mockUsers }
