import type { BaseResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { DictVO, DictTypeGroup, DictDTO } from './type'
import {
  mockGetDictByType,
  mockGetAllDicts,
  mockSaveDict,
  mockDeleteDict,
} from './mock'

/** 根据字典类型获取字典列表 */
export function getDictByType(dictType: string): Promise<BaseResponse<DictVO[]>> {
  if (USE_MOCK) return mockGetDictByType(dictType)
  return service({ url: `/api/admin/dicts/${dictType}`, method: 'get' }) as any
}

/** 获取全部字典（按类型分组） */
export function getAllDicts(): Promise<BaseResponse<DictTypeGroup[]>> {
  if (USE_MOCK) return mockGetAllDicts()
  return service({ url: '/api/admin/dicts', method: 'get' }) as any
}

/** 保存字典（新增或更新） */
export function saveDict(data: DictDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockSaveDict(data)
  return service({ url: '/api/admin/dicts', method: 'post', data }) as any
}

/** 删除字典 */
export function deleteDict(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteDict(id)
  return service({ url: `/api/admin/dicts/${id}`, method: 'delete' }) as any
}
