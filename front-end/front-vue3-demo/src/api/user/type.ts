import type { UserVO } from '@/api/auth/type'
import type { PageParam } from '@/api/common/type'

/** 用户管理VO —— 扩展UserVO，增加管理字段 */
export interface UserManageVO extends UserVO {
  /** 登录失败次数 */
  loginFailCount: number
  /** 账号锁定时间 */
  lockTime: string
}

/** 用户查询参数 */
export interface UserQuery extends PageParam {
  /** 用户名模糊搜索 */
  username?: string
  /** 真实姓名模糊搜索 */
  realName?: string
  /** 角色：SYS_ADMIN | LIB_ADMIN | TEACHER | STUDENT | OTHER */
  role?: string
  /** 用户类型：STUDENT | TEACHER | OTHER */
  userType?: string
  /** 状态：0-禁用 1-启用 2-锁定 */
  status?: number
}

/** 创建用户DTO */
export interface CreateUserDTO {
  /** 用户名 / 学工号 */
  username: string
  /** 密码 */
  password: string
  /** 真实姓名 */
  realName: string
  /** 角色 */
  role: string
  /** 用户类型 */
  userType: string
  /** 手机号 */
  phone?: string
  /** 邮箱 */
  email?: string
  /** 学院 */
  college?: string
}

/** 更新用户DTO */
export interface UpdateUserDTO {
  id: number
  realName?: string
  role?: string
  userType?: string
  phone?: string
  email?: string
  college?: string
  creditScore?: number
}

/** 更新用户状态DTO */
export interface UpdateUserStatusDTO {
  userId: number
  status: number
}

/** 分配角色DTO */
export interface AssignRoleDTO {
  userId: number
  role: string
}

/** 批量导入用户DTO */
export interface BatchImportDTO {
  users: CreateUserDTO[]
}
