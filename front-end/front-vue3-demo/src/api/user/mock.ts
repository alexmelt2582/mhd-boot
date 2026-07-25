import type { BaseResponse, PageResponse } from '@/utils/service'
import type { UserManageVO, UserQuery, CreateUserDTO, UpdateUserDTO, UpdateUserStatusDTO, AssignRoleDTO } from './type'

/* ================================================================
 * 学生数据：50 人，ID 范围 10001-10050，分布在 7 个学院
 * ================================================================ */
interface StudentCfg {
  id: number
  username: string
  realName: string
  college: string
}

const studentConfigs: StudentCfg[] = [
  // ---------- 计算机学院 (15人) ----------
  { id: 10001, username: '2021001001', realName: '张伟', college: '计算机学院' },
  { id: 10002, username: '2021001002', realName: '李娜', college: '计算机学院' },
  { id: 10003, username: '2021001003', realName: '王磊', college: '计算机学院' },
  { id: 10004, username: '2021001004', realName: '刘洋', college: '计算机学院' },
  { id: 10005, username: '2021001005', realName: '陈静', college: '计算机学院' },
  { id: 10006, username: '2021001006', realName: '杨帆', college: '计算机学院' },
  { id: 10007, username: '2021001007', realName: '赵敏', college: '计算机学院' },
  { id: 10008, username: '2021001008', realName: '黄强', college: '计算机学院' },
  { id: 10009, username: '2021001009', realName: '周婷', college: '计算机学院' },
  { id: 10010, username: '2021001010', realName: '吴鹏', college: '计算机学院' },
  { id: 10011, username: '2021001011', realName: '徐璐', college: '计算机学院' },
  { id: 10012, username: '2021001012', realName: '孙浩', college: '计算机学院' },
  { id: 10013, username: '2021001013', realName: '马丽', college: '计算机学院' },
  { id: 10014, username: '2021001014', realName: '朱峰', college: '计算机学院' },
  { id: 10015, username: '2021001015', realName: '胡悦', college: '计算机学院' },
  // ---------- 外国语学院 (8人) ----------
  { id: 10016, username: '2021002001', realName: '林晓', college: '外国语学院' },
  { id: 10017, username: '2021002002', realName: '何雨', college: '外国语学院' },
  { id: 10018, username: '2021002003', realName: '郭鑫', college: '外国语学院' },
  { id: 10019, username: '2021002004', realName: '沈洁', college: '外国语学院' },
  { id: 10020, username: '2021002005', realName: '韩冰', college: '外国语学院' },
  { id: 10021, username: '2021002006', realName: '唐宁', college: '外国语学院' },
  { id: 10022, username: '2021002007', realName: '冯雪', college: '外国语学院' },
  { id: 10023, username: '2021002008', realName: '曹亮', college: '外国语学院' },
  // ---------- 经管学院 (8人) ----------
  { id: 10024, username: '2021003001', realName: '邓超', college: '经管学院' },
  { id: 10025, username: '2021003002', realName: '彭芳', college: '经管学院' },
  { id: 10026, username: '2021003003', realName: '肖俊', college: '经管学院' },
  { id: 10027, username: '2021003004', realName: '田甜', college: '经管学院' },
  { id: 10028, username: '2021003005', realName: '袁博', college: '经管学院' },
  { id: 10029, username: '2021003006', realName: '段琳', college: '经管学院' },
  { id: 10030, username: '2021003007', realName: '雷刚', college: '经管学院' },
  { id: 10031, username: '2021003008', realName: '贺蓉', college: '经管学院' },
  // ---------- 理学院 (6人) ----------
  { id: 10032, username: '2021004001', realName: '罗晨', college: '理学院' },
  { id: 10033, username: '2021004002', realName: '梁慧', college: '理学院' },
  { id: 10034, username: '2021004003', realName: '宋扬', college: '理学院' },
  { id: 10035, username: '2021004004', realName: '郑洁', college: '理学院' },
  { id: 10036, username: '2021004005', realName: '谢飞', college: '理学院' },
  { id: 10037, username: '2021004006', realName: '韩月', college: '理学院' },
  // ---------- 工学院 (6人) ----------
  { id: 10038, username: '2021005001', realName: '唐明', college: '工学院' },
  { id: 10039, username: '2021005002', realName: '于洋', college: '工学院' },
  { id: 10040, username: '2021005003', realName: '董华', college: '工学院' },
  { id: 10041, username: '2021005004', realName: '苏婷', college: '工学院' },
  { id: 10042, username: '2021005005', realName: '魏东', college: '工学院' },
  { id: 10043, username: '2021005006', realName: '蒋琦', college: '工学院' },
  // ---------- 医学院 (4人) ----------
  { id: 10044, username: '2021006001', realName: '蔡文', college: '医学院' },
  { id: 10045, username: '2021006002', realName: '潘虹', college: '医学院' },
  { id: 10046, username: '2021006003', realName: '丁健', college: '医学院' },
  { id: 10047, username: '2021006004', realName: '余菲', college: '医学院' },
  // ---------- 法学院 (3人) ----------
  { id: 10048, username: '2021007001', realName: '任强', college: '法学院' },
  { id: 10049, username: '2021007002', realName: '姜琳', college: '法学院' },
  { id: 10050, username: '2021007003', realName: '廖辉', college: '法学院' },
]

/* ================================================================
 * 教师数据：5 人，ID 范围 20001-20005
 * ================================================================ */
interface TeacherCfg {
  id: number
  username: string
  realName: string
  college: string
}

const teacherConfigs: TeacherCfg[] = [
  { id: 20001, username: 't001', realName: '陈建国', college: '计算机学院' },
  { id: 20002, username: 't002', realName: '刘慧芳', college: '外国语学院' },
  { id: 20003, username: 't003', realName: '周志强', college: '工学院' },
  { id: 20004, username: 't004', realName: '孙丽华', college: '计算机学院' },
  { id: 20005, username: 't005', realName: '赵明远', college: '图书馆' },
]

/* ================================================================
 * 组装完整 mock 用户列表
 * ================================================================ */
function buildUserManageVO(
  id: number,
  username: string,
  realName: string,
  userType: string,
  role: string,
  college: string,
  status: number,
  creditScore: number,
  phone: string,
  email: string,
  loginFailCount: number,
  lockTime: string,
): UserManageVO {
  const day = String(Math.floor(Math.random() * 28) + 1).padStart(2, '0')
  const hour = String(Math.floor(Math.random() * 24)).padStart(2, '0')
  return {
    id,
    username,
    realName,
    userType,
    role,
    phone,
    email,
    avatar: '',
    college,
    creditScore,
    loginFailCount,
    lockTime,
    lastLoginTime: `2026-07-${day} ${hour}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}:00`,
    status,
    createTime: '2025-09-01 00:00:00',
    updateTime: `2026-07-${day} ${hour}:00:00`,
  }
}

const phonePrefixes = ['138', '139', '137', '136', '158', '159', '188', '187']

function genPhone(idx: number): string {
  const pre = phonePrefixes[idx % phonePrefixes.length]
  return pre + String(Math.floor(Math.random() * 100000000)).padStart(8, '0')
}

const userList: UserManageVO[] = []

// 50 students
studentConfigs.forEach((s, idx) => {
  const creditScores = [100, 95, 88, 82, 76, 70, 65, 60, 55, 100, 98, 90, 85, 92, 80]
  const statuses = [
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 0, 0, 1, 1, 2, 1, 1, 1, 1,
  ]
  const st = statuses[idx] ?? 1
  userList.push(
    buildUserManageVO(
      s.id,
      s.username,
      s.realName,
      'STUDENT',
      'STUDENT',
      s.college,
      st,
      creditScores[idx % creditScores.length],
      genPhone(idx),
      s.username + '@campus.edu',
      st === 2 ? 5 : st === 0 ? 0 : Math.floor(Math.random() * 3),
      st === 2 ? '2026-07-20 10:00:00' : '',
    ),
  )
})

// 5 teachers
teacherConfigs.forEach((t, idx) => {
  userList.push(
    buildUserManageVO(
      t.id,
      t.username,
      t.realName,
      'TEACHER',
      'TEACHER',
      t.college,
      1,
      100,
      genPhone(idx + 50),
      t.username + '@campus.edu',
      0,
      '',
    ),
  )
})

// 2 lib_admins
userList.push(
  buildUserManageVO(30001, 'libadmin01', '王馆长', 'TEACHER', 'LIB_ADMIN', '图书馆', 1, 100, '13800000011', 'libadmin01@library.com', 0, ''),
  buildUserManageVO(30002, 'libadmin02', '李采编', 'TEACHER', 'LIB_ADMIN', '图书馆', 1, 100, '13800000012', 'libadmin02@library.com', 0, ''),
)

// 1 sys_admin
userList.push(
  buildUserManageVO(1, 'admin', '系统管理员', 'TEACHER', 'SYS_ADMIN', '信息中心', 1, 100, '13800000000', 'admin@library.com', 0, ''),
)

// 2 others
userList.push(
  buildUserManageVO(40001, 'guest001', '校外访客A', 'OTHER', 'OTHER', '校外', 1, 80, '13800000013', 'guest001@test.com', 0, ''),
  buildUserManageVO(40002, 'guest002', '校外访客B', 'OTHER', 'OTHER', '校外', 0, 60, '13800000014', 'guest002@test.com', 0, ''),
)

export { userList }

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

/* ================================================================
 * Mock API 函数
 * ================================================================ */

/** 获取用户列表（分页 + 筛选） */
export async function mockGetUserList(query: UserQuery): Promise<BaseResponse<PageResponse<UserManageVO>>> {
  await delay()
  let filtered = [...userList]

  if (query.username) {
    const kw = query.username.toLowerCase()
    filtered = filtered.filter((u) => u.username.toLowerCase().includes(kw))
  }
  if (query.realName) {
    filtered = filtered.filter((u) => u.realName.includes(query.realName!))
  }
  if (query.role) {
    filtered = filtered.filter((u) => u.role === query.role)
  }
  if (query.userType) {
    filtered = filtered.filter((u) => u.userType === query.userType)
  }
  if (query.status !== undefined && query.status !== null) {
    filtered = filtered.filter((u) => u.status === query.status)
  }

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return { code: 0, msg: 'ok', data: { total: filtered.length, list } }
}

/** 根据ID获取用户详情 */
export async function mockGetUserById(id: number): Promise<BaseResponse<UserManageVO>> {
  await delay()
  const user = userList.find((u) => u.id === id)
  if (!user) return { code: 404, msg: '用户不存在', data: null as any }
  return { code: 0, msg: 'ok', data: { ...user } }
}

/** 创建用户 */
export async function mockCreateUser(data: CreateUserDTO): Promise<BaseResponse<null>> {
  await delay()
  const exists = userList.find((u) => u.username === data.username)
  if (exists) return { code: 1001, msg: '用户名已存在', data: null }
  const maxId = Math.max(...userList.map((u) => u.id), 10050)
  const newUser: UserManageVO = {
    id: maxId + 1,
    username: data.username,
    realName: data.realName,
    userType: data.userType,
    role: data.role,
    phone: data.phone || '',
    email: data.email || '',
    avatar: '',
    college: data.college || '',
    creditScore: 100,
    loginFailCount: 0,
    lockTime: '',
    lastLoginTime: '',
    status: 1,
    createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
    updateTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
  }
  userList.unshift(newUser)
  return { code: 0, msg: '创建成功', data: null }
}

/** 更新用户 */
export async function mockUpdateUser(data: UpdateUserDTO): Promise<BaseResponse<null>> {
  await delay()
  const idx = userList.findIndex((u) => u.id === data.id)
  if (idx === -1) return { code: 404, msg: '用户不存在', data: null }
  Object.assign(userList[idx], data, {
    updateTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
  })
  return { code: 0, msg: '更新成功', data: null }
}

/** 删除用户 */
export async function mockDeleteUser(id: number): Promise<BaseResponse<null>> {
  await delay()
  const idx = userList.findIndex((u) => u.id === id)
  if (idx === -1) return { code: 404, msg: '用户不存在', data: null }
  userList.splice(idx, 1)
  return { code: 0, msg: '删除成功', data: null }
}

/** 更新用户状态 */
export async function mockUpdateUserStatus(data: UpdateUserStatusDTO): Promise<BaseResponse<null>> {
  await delay()
  const user = userList.find((u) => u.id === data.userId)
  if (!user) return { code: 404, msg: '用户不存在', data: null }
  user.status = data.status
  if (data.status === 2) {
    user.lockTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
  } else {
    user.lockTime = ''
  }
  return { code: 0, msg: '状态更新成功', data: null }
}

/** 分配角色 */
export async function mockAssignRole(data: AssignRoleDTO): Promise<BaseResponse<null>> {
  await delay()
  const user = userList.find((u) => u.id === data.userId)
  if (!user) return { code: 404, msg: '用户不存在', data: null }
  user.role = data.role
  user.updateTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
  return { code: 0, msg: '角色分配成功', data: null }
}

/** 批量导入用户 */
export async function mockBatchImportUsers(data: { users: CreateUserDTO[] }): Promise<BaseResponse<{ success: number; fail: number }>> {
  await delay()
  let success = 0
  let fail = 0
  let maxId = Math.max(...userList.map((u) => u.id), 10050)
  for (const dto of data.users) {
    const exists = userList.find((u) => u.username === dto.username)
    if (exists) {
      fail++
      continue
    }
    maxId++
    userList.unshift({
      id: maxId,
      username: dto.username,
      realName: dto.realName,
      userType: dto.userType,
      role: dto.role,
      phone: dto.phone || '',
      email: dto.email || '',
      avatar: '',
      college: dto.college || '',
      creditScore: 100,
      loginFailCount: 0,
      lockTime: '',
      lastLoginTime: '',
      status: 1,
      createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
      updateTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
    })
    success++
  }
  return { code: 0, msg: `成功导入${success}条，失败${fail}条`, data: { success, fail } }
}
