import type {BaseResponse, PageInfo} from '@/utils/service'
import {filterList, mockFail, mockSuccess, paginate} from "@/utils/mock.ts";
import {patchObject} from "@/utils/other.ts";
import type {DictItemDTO, DictItemVO, DictTypeDTO, DictTypeQuery, DictTypeVO} from './type'

/* ================================================================
 * 模拟数据
 * ================================================================ */

const dictTypeList: DictTypeVO[] = [
  {
    dictId: 1,
    dictName: '空间类型',
    dictType: 'space_type',
    remark: '空间类型列表',
    createTime: '2023-01-01'
  },
  {
    dictId: 2,
    dictName: '区域名称',
    dictType: 'area_name',
    remark: '区域名称列表',
    createTime: '2023-01-01'
  },
  {dictId: 3, dictName: '楼层', dictType: 'floor', remark: '楼层列表', createTime: '2023-01-01'},
  {
    dictId: 4,
    dictName: '设备类型',
    dictType: 'equipment_type',
    remark: '设备类型列表',
    createTime: '2023-01-01'
  },
  {
    dictId: 5,
    dictName: '违规类型',
    dictType: 'violation_type',
    remark: '违规类型列表',
    createTime: '2023-01-01'
  },
  {
    dictId: 6,
    dictName: '消息类型',
    dictType: 'message_type',
    remark: '消息类型列表',
    createTime: '2023-01-01'
  },
]

const dictItemList: DictItemVO[] = [
  {
    dictItemId: 1,
    dictType: 'space_type',
    dictLabel: '座位',
    dictValue: 'SEAT',
    dictSort: 1,
    status: 1,
    remark: '自习座位'
  },
  {
    dictItemId: 2,
    dictType: 'space_type',
    dictLabel: '研讨室',
    dictValue: 'ROOM',
    dictSort: 2,
    status: 1,
    remark: '小组研讨室'
  },
  {
    dictItemId: 3,
    dictType: 'area_name',
    dictLabel: '安静学习区',
    dictValue: 'QUIET_ZONE',
    dictSort: 1,
    status: 1,
    remark: '需保持绝对安静'
  },
  {
    dictItemId: 4,
    dictType: 'area_name',
    dictLabel: '电子阅览区',
    dictValue: 'DIGITAL_ZONE',
    dictSort: 2,
    status: 1,
    remark: '配备电脑设备'
  },
  {
    dictItemId: 5,
    dictType: 'area_name',
    dictLabel: '研讨区',
    dictValue: 'DISCUSSION_ZONE',
    dictSort: 3,
    status: 1,
    remark: '可进行小组讨论'
  },
  {
    dictItemId: 6,
    dictType: 'area_name',
    dictLabel: '多媒体区',
    dictValue: 'MEDIA_ZONE',
    dictSort: 4,
    status: 1,
    remark: '多媒体设备区'
  },
  {
    dictItemId: 7,
    dictType: 'area_name',
    dictLabel: '休闲阅读区',
    dictValue: 'READING_ZONE',
    dictSort: 5,
    status: 1,
    remark: '休闲阅读空间'
  },
  {
    dictItemId: 8,
    dictType: 'floor',
    dictLabel: '1F',
    dictValue: '1F',
    dictSort: 1,
    status: 1,
    remark: '一楼'
  },
  {
    dictItemId: 9,
    dictType: 'floor',
    dictLabel: '2F',
    dictValue: '2F',
    dictSort: 2,
    status: 1,
    remark: '二楼'
  },
  {
    dictItemId: 10,
    dictType: 'floor',
    dictLabel: '3F',
    dictValue: '3F',
    dictSort: 3,
    status: 1,
    remark: '三楼'
  },
  {
    dictItemId: 11,
    dictType: 'floor',
    dictLabel: '4F',
    dictValue: '4F',
    dictSort: 4,
    status: 1,
    remark: '四楼'
  },
  {
    dictItemId: 12,
    dictType: 'equipment_type',
    dictLabel: '投影仪',
    dictValue: 'PROJECTOR',
    dictSort: 1,
    status: 1,
    remark: '投影设备'
  },
  {
    dictItemId: 13,
    dictType: 'equipment_type',
    dictLabel: '电脑',
    dictValue: 'COMPUTER',
    dictSort: 2,
    status: 1,
    remark: '计算机设备'
  },
  {
    dictItemId: 14,
    dictType: 'equipment_type',
    dictLabel: '白板',
    dictValue: 'WHITEBOARD',
    dictSort: 3,
    status: 1,
    remark: '交互式电子白板'
  },
  {
    dictItemId: 15,
    dictType: 'equipment_type',
    dictLabel: '网络设备',
    dictValue: 'NETWORK',
    dictSort: 4,
    status: 1,
    remark: '交换机/AP等网络设备'
  },
  {
    dictItemId: 16,
    dictType: 'violation_type',
    dictLabel: '低信用分限制',
    dictValue: 'LOW_CREDIT',
    dictSort: 1,
    status: 1,
    remark: '信用分低于阈值自动限制'
  },
  {
    dictItemId: 17,
    dictType: 'violation_type',
    dictLabel: '管理员封禁',
    dictValue: 'ADMIN_BAN',
    dictSort: 2,
    status: 1,
    remark: '管理员手动封禁'
  },
  {
    dictItemId: 18,
    dictType: 'violation_type',
    dictLabel: '违规行为',
    dictValue: 'VIOLATION',
    dictSort: 3,
    status: 1,
    remark: '使用违规记录'
  },
  {
    dictItemId: 19,
    dictType: 'message_type',
    dictLabel: '系统通知',
    dictValue: 'SYSTEM',
    dictSort: 1,
    status: 1,
    remark: '系统公告/维护通知'
  },
  {
    dictItemId: 20,
    dictType: 'message_type',
    dictLabel: '预约消息',
    dictValue: 'RESERVATION',
    dictSort: 2,
    status: 1,
    remark: '预约成功/提醒/取消'
  },
  {
    dictItemId: 21,
    dictType: 'message_type',
    dictLabel: '信用变更',
    dictValue: 'CREDIT',
    dictSort: 3,
    status: 1,
    remark: '信用分增减通知'
  },
  {
    dictItemId: 22,
    dictType: 'message_type',
    dictLabel: '违规提醒',
    dictValue: 'VIOLATION',
    dictSort: 4,
    status: 1,
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
  // 定义查询参数和数据字段的映射关系
  const fieldMap = {
    dictName: 'dictName',
    dictType: 'dictType',
  };
  // 先过滤，再分页
  const filteredList = filterList(dictTypeList, query, fieldMap);
  const pageInfo = paginate(filteredList, query.pageNum, query.pageSize);
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

export async function mockPageDictItems(query: any): Promise<BaseResponse<PageInfo<DictItemVO>>> {
  const fieldMap = {
    dictType: 'dictType',
    dictLabel: 'dictLabel',
    status: 'status', // 数字类型会进行严格相等匹配
  };
  const filteredList = filterList(dictItemList, query, fieldMap);
  const pageInfo = paginate(filteredList, query.pageNum, query.pageSize);
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
    status: data.status ?? 1,
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
