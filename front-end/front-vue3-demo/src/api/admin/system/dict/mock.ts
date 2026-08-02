import type {BaseResponse, PageInfo} from '@/utils/service'
import {mockFail, mockSuccess, paginate} from "@/utils/mock.ts";
import {patchObject} from "@/utils/other.ts";
import type {
  DictItemDTO,
  DictItemQuery,
  DictItemVO,
  DictTypeDTO,
  DictTypeQuery,
  DictTypeVO
} from './type'

/* ================================================================
 * 模拟数据
 * ================================================================ */

const dictTypeList: DictTypeVO[] = [
  {
    dictId: 1,
    dictName: '通用状态',
    dictType: 'sys_normal_status',
    remark: '',
    createTime: '2023-01-01'
  },
  {
    dictId: 2,
    dictName: '显示状态',
    dictType: 'sys_show_status',
    remark: '',
    createTime: '2023-01-01'
  },
  {
    dictId: 3,
    dictName: '是否状态',
    dictType: 'sys_yes_no',
    remark: '',
    createTime: '2023-01-01'
  },
  {
    dictId: 4,
    dictName: '成功失败状态',
    dictType: 'sys_success_failure',
    remark: '',
    createTime: '2023-01-01'
  },
  {
    dictId: 5,
    dictName: '启用禁用状态',
    dictType: 'sys_enable_disable',
    remark: '',
    createTime: '2023-01-01'
  },

  {
    dictId: 11,
    dictName: '空间类型',
    dictType: 'space_type',
    remark: '空间类型列表',
    createTime: '2023-01-01'
  },
  {
    dictId: 12,
    dictName: '区域名称',
    dictType: 'area_name',
    remark: '区域名称列表',
    createTime: '2023-01-01'
  },
  {dictId: 13, dictName: '楼层', dictType: 'floor', remark: '楼层列表', createTime: '2023-01-01'},
  {
    dictId: 14,
    dictName: '设备类型',
    dictType: 'equipment_type',
    remark: '设备类型列表',
    createTime: '2023-01-01'
  },
  {
    dictId: 15,
    dictName: '违规类型',
    dictType: 'violation_type',
    remark: '违规类型列表',
    createTime: '2023-01-01'
  },
  {
    dictId: 16,
    dictName: '消息类型',
    dictType: 'message_type',
    remark: '消息类型列表',
    createTime: '2023-01-01'
  },
]

const dictItemList: DictItemVO[] = [
  {
    dictItemId: 100,
    dictType: 'sys_normal_status',
    dictLabel: '正常',
    dictValue: '0',
    dictSort: 1,
    cssClass: '',
    listClass: 'success',
    isDefault: 'Y',
    remark: '正常状态'
  },
  {
    dictItemId: 101,
    dictType: 'sys_normal_status',
    dictLabel: '停用',
    dictValue: '1',
    dictSort: 2,
    cssClass: '',
    listClass: 'danger',
    isDefault: 'N',
    remark: '停用状态'
  },
  {
    dictItemId: 102,
    dictType: 'sys_show_status',
    dictLabel: '显示',
    dictValue: '0',
    dictSort: 1,
    cssClass: '',
    listClass: 'success',
    isDefault: 'Y',
    remark: '显示'
  },
  {
    dictItemId: 103,
    dictType: 'sys_show_status',
    dictLabel: '隐藏',
    dictValue: '1',
    dictSort: 2,
    cssClass: '',
    listClass: 'info',
    isDefault: 'N',
    remark: '隐藏'
  },
  {
    dictItemId: 104,
    dictType: 'sys_yes_no',
    dictLabel: '是',
    dictValue: 'Y',
    dictSort: 1,
    cssClass: '',
    listClass: 'success',
    isDefault: 'Y',
    remark: '是'
  },
  {
    dictItemId: 105,
    dictType: 'sys_yes_no',
    dictLabel: '否',
    dictValue: 'N',
    dictSort: 2,
    cssClass: '',
    listClass: 'danger',
    isDefault: 'N',
    remark: '否'
  },
  {
    dictItemId: 106,
    dictType: 'sys_success_failure',
    dictLabel: '成功',
    dictValue: '0',
    dictSort: 1,
    cssClass: '',
    listClass: 'success',
    isDefault: 'Y',
    remark: '操作成功'
  },
  {
    dictItemId: 107,
    dictType: 'sys_success_failure',
    dictLabel: '失败',
    dictValue: '1',
    dictSort: 2,
    cssClass: '',
    listClass: 'danger',
    isDefault: 'N',
    remark: '操作失败'
  },
  {
    dictItemId: 108,
    dictType: 'sys_enable_disable',
    dictLabel: '启用',
    dictValue: '0',
    dictSort: 1,
    cssClass: '',
    listClass: 'success',
    isDefault: 'Y',
    remark: '启用状态'
  },
  {
    dictItemId: 109,
    dictType: 'sys_enable_disable',
    dictLabel: '禁用',
    dictValue: '1',
    dictSort: 2,
    cssClass: '',
    listClass: 'danger',
    isDefault: 'N',
    remark: '禁用状态'
  },
  {
    dictItemId: 1,
    dictType: 'space_type',
    dictLabel: '座位',
    dictValue: 'SEAT',
    dictSort: 1,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '自习座位'
  },
  {
    dictItemId: 2,
    dictType: 'space_type',
    dictLabel: '研讨室',
    dictValue: 'ROOM',
    dictSort: 2,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '小组研讨室'
  },
  {
    dictItemId: 3,
    dictType: 'area_name',
    dictLabel: '安静学习区',
    dictValue: 'QUIET_ZONE',
    dictSort: 1,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '需保持绝对安静'
  },
  {
    dictItemId: 4,
    dictType: 'area_name',
    dictLabel: '电子阅览区',
    dictValue: 'DIGITAL_ZONE',
    dictSort: 2,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '配备电脑设备'
  },
  {
    dictItemId: 5,
    dictType: 'area_name',
    dictLabel: '研讨区',
    dictValue: 'DISCUSSION_ZONE',
    dictSort: 3,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '可进行小组讨论'
  },
  {
    dictItemId: 6,
    dictType: 'area_name',
    dictLabel: '多媒体区',
    dictValue: 'MEDIA_ZONE',
    dictSort: 4,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '多媒体设备区'
  },
  {
    dictItemId: 7,
    dictType: 'area_name',
    dictLabel: '休闲阅读区',
    dictValue: 'READING_ZONE',
    dictSort: 5,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '休闲阅读空间'
  },
  {
    dictItemId: 8,
    dictType: 'floor',
    dictLabel: '1F',
    dictValue: '1F',
    dictSort: 1,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '一楼'
  },
  {
    dictItemId: 9,
    dictType: 'floor',
    dictLabel: '2F',
    dictValue: '2F',
    dictSort: 2,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '二楼'
  },
  {
    dictItemId: 10,
    dictType: 'floor',
    dictLabel: '3F',
    dictValue: '3F',
    dictSort: 3,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '三楼'
  },
  {
    dictItemId: 11,
    dictType: 'floor',
    dictLabel: '4F',
    dictValue: '4F',
    dictSort: 4,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '四楼'
  },
  {
    dictItemId: 12,
    dictType: 'equipment_type',
    dictLabel: '投影仪',
    dictValue: 'PROJECTOR',
    dictSort: 1,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '投影设备'
  },
  {
    dictItemId: 13,
    dictType: 'equipment_type',
    dictLabel: '电脑',
    dictValue: 'COMPUTER',
    dictSort: 2,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '计算机设备'
  },
  {
    dictItemId: 14,
    dictType: 'equipment_type',
    dictLabel: '白板',
    dictValue: 'WHITEBOARD',
    dictSort: 3,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '交互式电子白板'
  },
  {
    dictItemId: 15,
    dictType: 'equipment_type',
    dictLabel: '网络设备',
    dictValue: 'NETWORK',
    dictSort: 4,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '交换机/AP等网络设备'
  },
  {
    dictItemId: 16,
    dictType: 'violation_type',
    dictLabel: '低信用分限制',
    dictValue: 'LOW_CREDIT',
    dictSort: 1,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '信用分低于阈值自动限制'
  },
  {
    dictItemId: 17,
    dictType: 'violation_type',
    dictLabel: '管理员封禁',
    dictValue: 'ADMIN_BAN',
    dictSort: 2,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '管理员手动封禁'
  },
  {
    dictItemId: 18,
    dictType: 'violation_type',
    dictLabel: '违规行为',
    dictValue: 'VIOLATION',
    dictSort: 3,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '使用违规记录'
  },
  {
    dictItemId: 19,
    dictType: 'message_type',
    dictLabel: '系统通知',
    dictValue: 'SYSTEM',
    dictSort: 1,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '系统公告/维护通知'
  },
  {
    dictItemId: 20,
    dictType: 'message_type',
    dictLabel: '预约消息',
    dictValue: 'RESERVATION',
    dictSort: 2,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '预约成功/提醒/取消'
  },
  {
    dictItemId: 21,
    dictType: 'message_type',
    dictLabel: '信用变更',
    dictValue: 'CREDIT',
    dictSort: 3,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '信用分增减通知'
  },
  {
    dictItemId: 22,
    dictType: 'message_type',
    dictLabel: '违规提醒',
    dictValue: 'VIOLATION',
    dictSort: 4,
    cssClass: "", listClass: "default", isDefault: "N",
    remark: '违规警告/处理通知'
  },
]

/* ================================================================
 * Mock API 函数
 * ================================================================ */

/** 根据字典类型获取字典列表 */
export async function mockGetAllDicts(): Promise<BaseResponse<DictTypeVO[]>> {
  return mockSuccess(dictTypeList)
}

export async function mockPageDictTypes(query: DictTypeQuery): Promise<BaseResponse<PageInfo<DictTypeVO>>> {
  // 1. 过滤
  let filteredList = [...dictTypeList];
  if (query.dictName) {
    filteredList = filteredList.filter((item) => item.dictName.includes(query.dictName!));
  }
  if (query.dictType) {
    filteredList = filteredList.filter((item) => item.dictType.includes(query.dictType!));
  }
  // 2. 分页
  const pageInfo = paginate(filteredList, query.pageNo, query.pageSize);
  return mockSuccess(pageInfo);
}

export async function mockGetDictTypeDetail(dictId: number): Promise<BaseResponse<DictTypeVO>> {
  const item = dictTypeList.find(d => d.dictId === dictId)
  if (!item) return mockFail('字典类型不存在')
  return mockSuccess(item)
}


export async function mockAddDictType(data: DictTypeDTO): Promise<BaseResponse<null>> {
  const exists = dictTypeList.find(d => d.dictType === data.dictType)
  if (exists) return mockFail('字典类型已存在')
  const maxId = Math.max(...dictTypeList.map(d => d.dictId), 0)
  dictTypeList.push({
    dictId: maxId + 1,
    dictName: data.dictName,
    dictType: data.dictType,
    remark: data.remark || '',
    createTime: new Date().toISOString().split('T')[0] ?? ''
  })
  return mockSuccess(null, '新增成功')
}

export async function mockUpdateDictType(data: DictTypeDTO): Promise<BaseResponse<null>> {
  if (!data.dictId) return mockFail('缺少主键')
  const idx = dictTypeList.findIndex(d => d.dictId === data.dictId)
  if (idx === -1) return mockFail('字典类型不存在')
  patchObject(dictTypeList[idx]!, data)
  return mockSuccess(null, '更新成功')
}

export async function mockDeleteDictType(dictId: number): Promise<BaseResponse<null>> {
  const idx = dictTypeList.findIndex(d => d.dictId === dictId)
  if (idx === -1) return mockFail('字典类型不存在')
  dictTypeList.splice(idx, 1)
  return mockSuccess(null, '删除成功')
}

export async function mockRefreshDictCache(): Promise<BaseResponse<null>> {
  return mockSuccess(null, '缓存刷新成功')
}

// --- 字典数据接口 ---
export async function mockGetDictByType(dictType: string): Promise<BaseResponse<DictItemVO[]>> {
  const items = dictItemList.filter((d) => d.dictType === dictType).sort((a, b) => a.dictSort - b.dictSort)
  return mockSuccess(items)
}

export async function mockPageDictItems(query: DictItemQuery): Promise<BaseResponse<PageInfo<DictItemVO>>> {
  // 1. 过滤
  let filteredList = [...dictItemList];
  if (query.dictLabel) {
    filteredList = filteredList.filter((item) => item.dictLabel.includes(query.dictLabel!));
  }
  if (query.dictType) {
    filteredList = filteredList.filter((item) => item.dictType.includes(query.dictType!));
  }
  // 2. 分页
  const pageInfo = paginate(filteredList, query.pageNo, query.pageSize);
  return mockSuccess(pageInfo);
}

export async function mockGetDictItemDetail(dictItemId: number): Promise<BaseResponse<DictItemVO>> {
  const item = dictItemList.find(d => d.dictItemId === dictItemId)
  if (!item) return mockFail('字典数据不存在')
  return mockSuccess(item)
}

export async function mockAddDictItem(data: DictItemDTO): Promise<BaseResponse<null>> {
  const exists = dictItemList.find(d => d.dictType === data.dictType && d.dictValue === data.dictValue)
  if (exists) return mockFail('字典值已存在')
  const maxId = Math.max(...dictItemList.map(d => d.dictItemId), 0)
  dictItemList.push({
    dictItemId: maxId + 1,
    dictType: data.dictType,
    dictLabel: data.dictLabel,
    dictValue: data.dictValue,
    dictSort: data.dictSort ?? 99,
    cssClass: data.cssClass ?? '',
    listClass: data.listClass ?? '',
    isDefault: data.isDefault ?? 'N',
    remark: data.remark || '',
  })
  return mockSuccess(null, '新增成功')
}

export async function mockUpdateDictItem(data: DictItemDTO): Promise<BaseResponse<null>> {
  if (!data.dictItemId) return mockFail('缺少主键')
  const idx = dictItemList.findIndex(d => d.dictItemId === data.dictItemId)
  if (idx === -1) return mockFail('字典数据不存在')
  patchObject(dictItemList[idx]!, data)
  return mockSuccess(null, '更新成功')
}

export async function mockDeleteDictItem(dictItemId: number): Promise<BaseResponse<null>> {
  const idx = dictItemList.findIndex(d => d.dictItemId === dictItemId)
  if (idx === -1) return mockFail('字典数据不存在')
  dictItemList.splice(idx, 1)
  return mockSuccess(null, '删除成功')
}
