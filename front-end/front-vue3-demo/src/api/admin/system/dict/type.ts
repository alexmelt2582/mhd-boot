import type {PageParam} from "@/utils/service.ts";

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
export interface DictTypeQuery extends PageParam{
  dictName?: string
  dictType?: string
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
  /** 样式属性（其他样式扩展） */
  cssClass: string
  /** 回显样式 */
  listClass: string
  /** 是否默认（Y是 N否） */
  isDefault: string
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
  cssClass?: string
  listClass?: string
  isDefault?: string
  remark?: string
}

/** 字典数据查询参数 */
export interface DictItemQuery extends PageParam{
  dictType?: string
  dictLabel?: string
}
