import type {BaseResponse, PageInfo} from '@/utils/service'
import service, {USE_MOCK} from '@/utils/service'
import type {
  DictItemDTO,
  DictItemQuery,
  DictItemVO,
  DictTypeDTO,
  DictTypeQuery,
  DictTypeVO
} from './type'
import {
  mockAddDictItem,
  mockAddDictType,
  mockDeleteDictItem,
  mockDeleteDictType,
  mockGetAllDicts,
  mockGetDictByType,
  mockGetDictItemDetail,
  mockGetDictTypeDetail,
  mockPageDictItems,
  mockPageDictTypes,
  mockRefreshDictCache,
  mockUpdateDictItem,
  mockUpdateDictType
} from './mock'

/* ================= 字典类型接口 ================= */

/** 获取全部字典类型列表 */
export function getAllDicts(): Promise<BaseResponse<DictTypeVO[]>> {
  if (USE_MOCK) return mockGetAllDicts()
  return service({url: '/system/dict/type', method: 'get'}) as any
}

/** 分页查询字典类型列表 */
export function pageDictTypes(query: DictTypeQuery): Promise<BaseResponse<PageInfo<DictTypeVO>>> {
  if (USE_MOCK) return mockPageDictTypes(query)
  return service({url: '/system/dict/type/page', method: 'get', params: query}) as any
}

/** 获取字典类型详情 */
export function getDictTypeDetail(dictId: number): Promise<BaseResponse<DictTypeVO>> {
  if (USE_MOCK) return mockGetDictTypeDetail(dictId)
  return service({url: `/system/dict/type/${dictId}`, method: 'get'}) as any
}

/** 新增字典类型 */
export function addDictType(data: DictTypeDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockAddDictType(data)
  return service({url: '/system/dict/type', method: 'post', data}) as any
}

/** 修改字典类型 */
export function updateDictType(data: DictTypeDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUpdateDictType(data)
  return service({url: '/system/dict/type', method: 'put', data}) as any
}

/** 删除字典类型 */
export function deleteDictType(dictId: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteDictType(dictId)
  return service({url: `/system/dict/type/${dictId}`, method: 'delete'}) as any
}

/** 刷新字典缓存 */
export function refreshDictCache(): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockRefreshDictCache()
  return service({url: '/system/dict/type/refreshCache', method: 'delete'}) as any
}

/* ================= 字典数据接口 ================= */

/** 根据字典类型获取字典数据列表 */
export function getDictByType(dictType: string): Promise<BaseResponse<DictItemVO[]>> {
  if (USE_MOCK) return mockGetDictByType(dictType)
  return service({url: `/system/dict/item/type/${dictType}`, method: 'get'}) as any
}

/** 分页查询字典数据列表 */
export function pageDictItems(query: DictItemQuery): Promise<BaseResponse<PageInfo<DictItemVO>>> {
  if (USE_MOCK) return mockPageDictItems(query)
  return service({url: '/system/dict/item/page', method: 'get', params: query}) as any
}

/** 获取字典数据详情 */
export function getDictItemDetail(dictItemId: number): Promise<BaseResponse<DictItemVO>> {
  if (USE_MOCK) return mockGetDictItemDetail(dictItemId)
  return service({url: `/system/dict/item/${dictItemId}`, method: 'get'}) as any
}

/** 新增字典数据 */
export function addDictItem(data: DictItemDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockAddDictItem(data)
  return service({url: '/system/dict/item', method: 'post', data}) as any
}

/** 修改字典数据 */
export function updateDictItem(data: DictItemDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUpdateDictItem(data)
  return service({url: '/system/dict/item', method: 'put', data}) as any
}

/** 删除字典数据项 */
export function deleteDictItem(dictItemId: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteDictItem(dictItemId)
  return service({url: `/system/dict/item/${dictItemId}`, method: 'delete'}) as any
}
