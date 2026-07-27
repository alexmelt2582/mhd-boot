/** 字典类型VO */
export interface DictTypeVO {
  /** 字典主键 */
  dictId: number
  /** 字典名称 */
  dictName: string
  /** 字典类型 */
  dictType: string
  /** 备注 */
  remark: string
  /** 创建时间 */
  createTime: string
}

/** 字典类型保存DTO */
export interface DictTypeDTO {
  dictId?: number
  dictName: string
  dictType: string
  remark?: string
}

/** 字典类型查询参数 */
export interface DictTypeQuery {
  dictName?: string
  dictType?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

/** 字典数据VO */
export interface DictItemVO {
  /** 字典数据ID */
  dictItemId: number
  /** 字典标签 */
  dictLabel: string
  /** 字典值 */
  dictValue: string
  /** 字典类型 */
  dictType: string
  /** 排序 */
  dictSort: number
  /** 状态 */
  status: number
  /** 备注 */
  remark: string
}

/** 字典数据保存DTO */
export interface DictItemDTO {
  dictItemId?: number
  dictLabel: string
  dictValue: string
  dictType: string
  dictSort?: number
  status?: number
  remark?: string
}

/** 字典数据查询参数 */
export interface DictItemQuery {
  dictType?: string
  dictLabel?: string
  status?: number
  pageNum?: number
  pageSize?: number
}
