import type { EquipmentQuery } from '@/api/equipment/type'

export const EquipmentTypeOptions = [
  { label: '全部', value: '' },
  { label: '投影仪', value: 'PROJECTOR' },
  { label: '电脑', value: 'COMPUTER' },
  { label: '白板', value: 'WHITEBOARD' },
  { label: '网络设备', value: 'NETWORK' },
]

export const EquipmentStatusOptions = [
  { label: '全部', value: '' },
  { label: '正常', value: 1 },
  { label: '故障', value: 0 },
  { label: '报废', value: 2 },
]

export const EquipmentTypeMap: Record<string, string> = {
  PROJECTOR: '投影仪',
  COMPUTER: '电脑',
  WHITEBOARD: '白板',
  NETWORK: '网络设备',
}

export const tableColumns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'equipmentName', label: '设备名称', minWidth: 130 },
  { prop: 'equipmentType', label: '设备类型', width: 100 },
  { prop: 'spaceName', label: '所属空间', minWidth: 120 },
  { prop: 'equipmentModel', label: '设备型号', width: 130 },
  { prop: 'status', label: '状态', width: 80 },
  { prop: 'createTime', label: '创建时间', width: 170 },
]

export const defaultQuery: EquipmentQuery = {
  page: 1,
  pageSize: 10,
  keyword: '',
  equipmentType: undefined,
  status: undefined,
}

export const defaultForm = {
  equipmentName: '',
  equipmentType: '' as string,
  spaceId: null as number | null,
  equipmentModel: '',
  purchaseDate: '',
  lastMaintenanceDate: '',
  status: 1,
  remark: '',
}
