import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { FileVO, FileQuery } from './type'
import { mockGetFiles, mockUploadFile, mockDeleteFile, mockDownloadFile } from './mock'

export function getFiles(query: FileQuery): Promise<BaseResponse<PageResponse<FileVO>>> {
  if (USE_MOCK) return mockGetFiles(query)
  return service({ url: '/api/files', method: 'get', params: query }) as any
}

export function uploadFile(data: FormData): Promise<BaseResponse<FileVO>> {
  if (USE_MOCK) {
    const file = data.get('file') as File
    return mockUploadFile({
      fileName: file?.name || 'unknown',
      fileType: file?.type?.startsWith('image') ? 'IMAGE' : file?.type?.startsWith('application/pdf') ? 'DOCUMENT' : 'OTHER',
      fileSize: file?.size || 0,
    })
  }
  return service({ url: '/api/files/upload', method: 'post', data }) as any
}

export function deleteFile(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteFile(id)
  return service({ url: `/api/files/${id}`, method: 'delete' }) as any
}

export function downloadFile(id: number): Promise<BaseResponse<{ url: string; fileName: string }>> {
  if (USE_MOCK) return mockDownloadFile(id)
  return service({ url: `/api/files/${id}/download`, method: 'get' }) as any
}
