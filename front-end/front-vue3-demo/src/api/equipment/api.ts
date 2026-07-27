import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { EquipmentVO, EquipmentQuery, EquipmentDTO, AssignEquipmentDTO } from './type'
import {
  mockGetEquipmentList,
  mockCreateEquipment,
  mockUpdateEquipment,
  mockDeleteEquipment,
  mockAssignEquipment,
  mockUnassignEquipment,
} from './mock'

/** 获取设备列表（分页 + 筛选） */
export function getEquipmentList(query: EquipmentQuery): Promise<BaseResponse<PageResponse<EquipmentVO>>> {
  if (USE_MOCK) return mockGetEquipmentList(query)
  return service({ url: '/api/admin/equipments', method: 'get', params: query }) as any
}

/** 创建设备 */
export function createEquipment(data: EquipmentDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockCreateEquipment(data)
  return service({ url: '/api/admin/equipments', method: 'post', data }) as any
}

/** 更新设备 */
export function updateEquipment(data: EquipmentDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUpdateEquipment(data)
  return service({ url: `/api/admin/equipments/${data.id}`, method: 'put', data }) as any
}

/** 删除设备 */
export function deleteEquipment(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteEquipment(id)
  return service({ url: `/api/admin/equipments/${id}`, method: 'delete' }) as any
}

/** 分配设备到空间 */
export function assignEquipment(data: AssignEquipmentDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockAssignEquipment(data)
  return service({ url: `/api/admin/equipments/${data.equipmentId}/assign`, method: 'put', data: { spaceId: data.spaceId } }) as any
}

/** 取消分配设备 */
export function unassignEquipment(equipmentId: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUnassignEquipment(equipmentId)
  return service({ url: `/api/admin/equipments/${equipmentId}/unassign`, method: 'put' }) as any
}
