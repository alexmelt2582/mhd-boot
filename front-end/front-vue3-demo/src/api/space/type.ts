import type { PageParam, PageResult } from '@/api/common/type'

/** 设备配置 */
export interface EquipmentConfig {
  projector?: boolean
  whiteboard?: boolean
  computer?: string
  network?: string
  power?: boolean
  sofa?: boolean
  videoConf?: boolean
  light?: string
  audio?: boolean
  screen?: string
}

/** 空间VO */
export interface SpaceVO {
  id: number
  spaceName: string
  spaceType: string
  areaName: string
  floor: string
  capacity: number
  equipmentConfig: EquipmentConfig
  qrCode: string
  imageUrl: string
  description: string
  useRules: string
  sortOrder: number
  status: number
  createTime: string
  updateTime: string
}

/** 空间查询参数 */
export interface SpaceQuery extends PageParam {
  spaceType?: string
  areaName?: string
  floor?: string
  status?: number
  keyword?: string
}

/** 空间创建/编辑 DTO */
export interface SpaceDTO {
  id?: number
  spaceName: string
  spaceType: string
  areaName: string
  floor: string
  capacity: number
  equipmentConfig?: EquipmentConfig
  description?: string
  useRules?: string
  sortOrder?: number
  qrCode?: string
  imageUrl?: string
}
