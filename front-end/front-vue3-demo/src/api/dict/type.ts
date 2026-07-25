/** 字典VO */
export interface DictVO {
  /** 字典ID */
  id: number
  /** 字典类型，如 space_type / area_name / floor / equipment_type / violation_type / message_type */
  dictType: string
  /** 字典标签（中文展示） */
  dictLabel: string
  /** 字典值（存储值） */
  dictValue: string
  /** 排序 */
  sortOrder: number
  /** 状态：0-禁用 1-启用 */
  status: number
  /** 备注 */
  remark: string
}

/** 按类型分组的字典 */
export interface DictTypeGroup {
  /** 字典类型 */
  dictType: string
  /** 该类型下的字典项列表 */
  items: DictVO[]
}

/** 字典保存DTO */
export interface DictDTO {
  id?: number
  dictType: string
  dictLabel: string
  dictValue: string
  sortOrder?: number
  status?: number
  remark?: string
}
