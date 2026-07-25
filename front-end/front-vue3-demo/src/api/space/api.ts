import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { SpaceVO, SpaceQuery, SpaceDTO } from './type'
import {
  mockGetSpaceList,
  mockGetSpaceById,
  mockCreateSpace,
  mockUpdateSpace,
  mockDeleteSpace,
  mockUpdateSpaceStatus,
} from './mock'

export function getSpaceList(query: SpaceQuery): Promise<BaseResponse<PageResponse<SpaceVO>>> {
  if (USE_MOCK) return mockGetSpaceList(query)
  return service({ url: '/api/spaces', method: 'get', params: query }) as any
}

export function getSpaceById(id: number): Promise<BaseResponse<SpaceVO>> {
  if (USE_MOCK) return mockGetSpaceById(id)
  return service({ url: `/api/spaces/${id}`, method: 'get' }) as any
}

export function createSpace(data: SpaceDTO): Promise<BaseResponse<SpaceVO>> {
  if (USE_MOCK) return mockCreateSpace(data)
  return service({ url: '/api/spaces', method: 'post', data }) as any
}

export function updateSpace(data: SpaceDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUpdateSpace(data)
  return service({ url: `/api/spaces/${data.id}`, method: 'put', data }) as any
}

export function deleteSpace(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteSpace(id)
  return service({ url: `/api/spaces/${id}`, method: 'delete' }) as any
}

export function updateSpaceStatus(id: number, status: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUpdateSpaceStatus(id, status)
  return service({ url: `/api/spaces/${id}/status`, method: 'put', data: { status } }) as any
}
