import type {UserQuery} from '@/api/user/type'
import type {SearchConfigItem} from '@/components/SearcherBar/type.ts'

export const RoleOptions = [
  {label: '全部', value: ''},
  {label: '学生', value: 'STUDENT'},
  {label: '教师', value: 'TEACHER'},
  {label: '图书管理员', value: 'LIB_ADMIN'},
  {label: '系统管理员', value: 'SYS_ADMIN'},
  {label: '其他', value: 'OTHER'},
]

export const StatusOptions = [
  {label: '全部', value: ''},
  {label: '正常', value: 1},
  {label: '禁用', value: 0},
  {label: '锁定', value: 2},
]

export const userSearchConfig: SearchConfigItem[] = [
  {
    type: 'input', label: '用户名', field: 'username'
  },
  {
    type: 'input', label: '真实姓名', field: 'realName'
  },
  {
    type: 'select', label: '角色', field: 'role',
    options: RoleOptions,
    clearable: true, filterable: true
  },
  {
    type: 'select', label: '状态', field: 'status',
    options: StatusOptions,
    clearable: true, filterable: true
  },
]


export const UserTypeOptions = [
  {label: '全部', value: ''},
  {label: '学生', value: 'STUDENT'},
  {label: '教师', value: 'TEACHER'},
  {label: '其他', value: 'OTHER'},
]


export const CollegeOptions = [
  {label: '计算机学院', value: '计算机学院'},
  {label: '外国语学院', value: '外国语学院'},
  {label: '经管学院', value: '经管学院'},
  {label: '理学院', value: '理学院'},
  {label: '工学院', value: '工学院'},
  {label: '医学院', value: '医学院'},
  {label: '法学院', value: '法学院'},
  {label: '图书馆', value: '图书馆'},
  {label: '信息中心', value: '信息中心'},
]

export const RoleMap: Record<string, string> = {
  STUDENT: '学生',
  TEACHER: '教师',
  LIB_ADMIN: '图书管理员',
  SYS_ADMIN: '系统管理员',
  OTHER: '其他',
}

export const tableColumns = [
  {prop: 'id', label: 'ID', width: 70},
  {prop: 'username', label: '用户名', minWidth: 120},
  {prop: 'realName', label: '姓名', width: 90},
  {prop: 'role', label: '角色', width: 110},
  {prop: 'userType', label: '用户类型', width: 90},
  {prop: 'college', label: '学院', minWidth: 120},
  {prop: 'creditScore', label: '信用分', width: 80},
  {prop: 'status', label: '状态', width: 80},
]

export const defaultQuery: UserQuery = {
  page: 1,
  pageSize: 10,
  username: '',
  realName: '',
  role: '',
  userType: '',
  status: undefined,
}

export const defaultForm = {
  username: '',
  password: '',
  realName: '',
  role: 'STUDENT' as string,
  userType: 'STUDENT' as string,
  phone: '',
  email: '',
  college: '',
}
