import type { PageParam } from '@/api/common/type'

/** 设备状态：0-报修 1-正常 2-报废 */
export type EquipmentStatus = 0 | 1 | 2

/** 设备类型：PROJECTOR | COMPUTER | WHITEBOARD | NETWORK */
export type EquipmentType = 'PROJECTOR' | 'COMPUTER' | 'WHITEBOARD' | 'NETWORK'

/** 设备VO */
export interface EquipmentVO {
  /** 设备ID */
  id: number
  /** 设备名称 */
  equipmentName: string
  /** 设备型号 */
  equipmentModel: string
  /** 设备类型 */
  equipmentType: EquipmentType
  /** 所属空间ID（可为空，表示未分配） */
  spaceId: number | null
  /** 所属空间名称（联表查询） */
  spaceName: string
  /** 采购日期 */
  purchaseDate: string
  /** 最近维护日期 */
  lastMaintenanceDate: string
  /** 设备状态：0-报修 1-正常 2-报废 */
  status: EquipmentStatus
  /** 备注 */
  remark: string
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
}

/** 设备查询参数 */
export interface EquipmentQuery extends PageParam {
  /** 设备类型筛选 */
  equipmentType?: EquipmentType
  /** 所属空间ID筛选 */
  spaceId?: number
  /** 设备状态筛选 */
  status?: EquipmentStatus
  /** 关键字搜索（名称/型号） */
  keyword?: string
}

/** 设备创建/编辑 DTO */
export interface EquipmentDTO {
  id?: number
  /** 设备名称 */
  equipmentName: string
  /** 设备型号 */
  equipmentModel: string
  /** 设备类型 */
  equipmentType: EquipmentType
  /** 所属空间ID */
  spaceId?: number | null
  /** 采购日期 */
  purchaseDate?: string
  /** 最近维护日期 */
  lastMaintenanceDate?: string
  /** 备注 */
  remark?: string
}

/** 分配设备DTO */
export interface AssignEquipmentDTO {
  equipmentId: number
  spaceId: number
}
