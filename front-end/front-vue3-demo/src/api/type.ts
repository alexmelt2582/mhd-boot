
/* ==================== 用户相关 ==================== */

/** 系统用户 */
export interface SystemUserVO {
  id: number
  username: string
  remark?: string
  email: string
  mobile: string
  status: number
  loginIp?: string
  loginTime?: string
  creator?: number
  createTime: string
  updater?: number
  updateTime: string
  roles: Set<number>
}

/** 登录返回信息 */
export interface SystemAuthLoginVO {
  token: string
  user: SystemUserVO
}

/** 登录请求 */
export interface SystemAuthLoginReqDTO {
  username: string
  password: string
}

