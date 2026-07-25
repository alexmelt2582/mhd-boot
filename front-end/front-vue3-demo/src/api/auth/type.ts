/** 登录请求 */
export interface LoginReqDTO {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginVO {
  token: string
  user: UserVO
}

/** 注册请求 */
export interface RegisterReqDTO {
  username: string
  password: string
  realName: string
  phone?: string
  email?: string
  college?: string
}

/** 用户信息 */
export interface UserVO {
  id: number
  username: string
  realName: string
  userType: string
  role: string
  phone: string
  email: string
  avatar: string
  college: string
  creditScore: number
  loginFailCount?: number
  lockTime?: string
  lastLoginTime: string
  status: number
  createTime: string
  updateTime: string
}
