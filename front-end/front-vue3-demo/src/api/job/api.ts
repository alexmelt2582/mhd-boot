import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { JobVO, JobQuery } from './type'
import { mockGetJobList, mockUpdateJob, mockTriggerJob, mockPauseJob, mockResumeJob } from './mock'

export function getJobList(query: JobQuery): Promise<BaseResponse<PageResponse<JobVO>>> {
  if (USE_MOCK) return mockGetJobList(query)
  return service({ url: '/api/jobs', method: 'get', params: query }) as any
}

export function updateJob(data: Partial<JobVO> & { id: number }): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockUpdateJob(data)
  return service({ url: `/api/jobs/${data.id}`, method: 'put', data }) as any
}

export function triggerJob(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockTriggerJob(id)
  return service({ url: `/api/jobs/${id}/trigger`, method: 'post' }) as any
}

export function pauseJob(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockPauseJob(id)
  return service({ url: `/api/jobs/${id}/pause`, method: 'post' }) as any
}

export function resumeJob(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockResumeJob(id)
  return service({ url: `/api/jobs/${id}/resume`, method: 'post' }) as any
}
