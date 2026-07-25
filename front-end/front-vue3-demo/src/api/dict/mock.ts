import type { BaseResponse } from '@/utils/service'
import type { DictVO, DictDTO } from './type'

/* ================================================================
 * 字典数据 —— 与后端 sys_dict DDL INSERT 数据完全一致
 * ================================================================ */

const dictList: DictVO[] = [
  // ========== space_type ==========
  { id: 1, dictType: 'space_type', dictLabel: '座位', dictValue: 'SEAT', sortOrder: 1, status: 1, remark: '自习座位' },
  { id: 2, dictType: 'space_type', dictLabel: '研讨室', dictValue: 'ROOM', sortOrder: 2, status: 1, remark: '小组研讨室' },

  // ========== area_name ==========
  { id: 3, dictType: 'area_name', dictLabel: '安静学习区', dictValue: 'QUIET_ZONE', sortOrder: 1, status: 1, remark: '需保持绝对安静' },
  { id: 4, dictType: 'area_name', dictLabel: '电子阅览区', dictValue: 'DIGITAL_ZONE', sortOrder: 2, status: 1, remark: '配备电脑设备' },
  { id: 5, dictType: 'area_name', dictLabel: '研讨区', dictValue: 'DISCUSSION_ZONE', sortOrder: 3, status: 1, remark: '可进行小组讨论' },
  { id: 6, dictType: 'area_name', dictLabel: '多媒体区', dictValue: 'MEDIA_ZONE', sortOrder: 4, status: 1, remark: '多媒体设备区' },
  { id: 7, dictType: 'area_name', dictLabel: '休闲阅读区', dictValue: 'READING_ZONE', sortOrder: 5, status: 1, remark: '休闲阅读空间' },

  // ========== floor ==========
  { id: 8, dictType: 'floor', dictLabel: '1F', dictValue: '1F', sortOrder: 1, status: 1, remark: '一楼' },
  { id: 9, dictType: 'floor', dictLabel: '2F', dictValue: '2F', sortOrder: 2, status: 1, remark: '二楼' },
  { id: 10, dictType: 'floor', dictLabel: '3F', dictValue: '3F', sortOrder: 3, status: 1, remark: '三楼' },
  { id: 11, dictType: 'floor', dictLabel: '4F', dictValue: '4F', sortOrder: 4, status: 1, remark: '四楼' },

  // ========== equipment_type ==========
  { id: 12, dictType: 'equipment_type', dictLabel: '投影仪', dictValue: 'PROJECTOR', sortOrder: 1, status: 1, remark: '投影设备' },
  { id: 13, dictType: 'equipment_type', dictLabel: '电脑', dictValue: 'COMPUTER', sortOrder: 2, status: 1, remark: '计算机设备' },
  { id: 14, dictType: 'equipment_type', dictLabel: '白板', dictValue: 'WHITEBOARD', sortOrder: 3, status: 1, remark: '交互式电子白板' },
  { id: 15, dictType: 'equipment_type', dictLabel: '网络设备', dictValue: 'NETWORK', sortOrder: 4, status: 1, remark: '交换机/AP等网络设备' },

  // ========== violation_type ==========
  { id: 16, dictType: 'violation_type', dictLabel: '低信用分限制', dictValue: 'LOW_CREDIT', sortOrder: 1, status: 1, remark: '信用分低于阈值自动限制' },
  { id: 17, dictType: 'violation_type', dictLabel: '管理员封禁', dictValue: 'ADMIN_BAN', sortOrder: 2, status: 1, remark: '管理员手动封禁' },
  { id: 18, dictType: 'violation_type', dictLabel: '违规行为', dictValue: 'VIOLATION', sortOrder: 3, status: 1, remark: '使用违规记录' },

  // ========== message_type ==========
  { id: 19, dictType: 'message_type', dictLabel: '系统通知', dictValue: 'SYSTEM', sortOrder: 1, status: 1, remark: '系统公告/维护通知' },
  { id: 20, dictType: 'message_type', dictLabel: '预约消息', dictValue: 'RESERVATION', sortOrder: 2, status: 1, remark: '预约成功/提醒/取消' },
  { id: 21, dictType: 'message_type', dictLabel: '信用变更', dictValue: 'CREDIT', sortOrder: 3, status: 1, remark: '信用分增减通知' },
  { id: 22, dictType: 'message_type', dictLabel: '违规提醒', dictValue: 'VIOLATION', sortOrder: 4, status: 1, remark: '违规警告/处理通知' },
]

export { dictList }

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

/* ================================================================
 * Mock API 函数
 * ================================================================ */

/** 根据字典类型获取字典列表 */
export async function mockGetDictByType(dictType: string): Promise<BaseResponse<DictVO[]>> {
  await delay()
  const items = dictList.filter((d) => d.dictType === dictType).sort((a, b) => a.sortOrder - b.sortOrder)
  return { code: 0, msg: 'ok', data: items }
}

/** 获取全部字典（按类型分组） */
export async function mockGetAllDicts(): Promise<BaseResponse<{ dictType: string; items: DictVO[] }[]>> {
  await delay()
  const map = new Map<string, DictVO[]>()
  for (const d of dictList) {
    if (!map.has(d.dictType)) map.set(d.dictType, [])
    map.get(d.dictType)!.push(d)
  }
  const result = Array.from(map.entries()).map(([dictType, items]) => ({
    dictType,
    items: items.sort((a, b) => a.sortOrder - b.sortOrder),
  }))
  return { code: 0, msg: 'ok', data: result }
}

/** 保存字典（新增或更新） */
export async function mockSaveDict(data: DictDTO): Promise<BaseResponse<null>> {
  await delay()
  if (data.id) {
    // 更新
    const idx = dictList.findIndex((d) => d.id === data.id)
    if (idx === -1) return { code: 404, msg: '字典项不存在', data: null }
    Object.assign(dictList[idx], data)
    return { code: 0, msg: '更新成功', data: null }
  } else {
    // 新增
    const exists = dictList.find((d) => d.dictType === data.dictType && d.dictValue === data.dictValue)
    if (exists) return { code: 1001, msg: '字典值已存在', data: null }
    const maxId = Math.max(...dictList.map((d) => d.id), 22)
    dictList.push({
      id: maxId + 1,
      dictType: data.dictType,
      dictLabel: data.dictLabel,
      dictValue: data.dictValue,
      sortOrder: data.sortOrder ?? 99,
      status: data.status ?? 1,
      remark: data.remark || '',
    })
    return { code: 0, msg: '新增成功', data: null }
  }
}

/** 删除字典 */
export async function mockDeleteDict(id: number): Promise<BaseResponse<null>> {
  await delay()
  const idx = dictList.findIndex((d) => d.id === id)
  if (idx === -1) return { code: 404, msg: '字典项不存在', data: null }
  dictList.splice(idx, 1)
  return { code: 0, msg: '删除成功', data: null }
}
