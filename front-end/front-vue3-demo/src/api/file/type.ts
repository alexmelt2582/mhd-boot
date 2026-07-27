import type { PageParam } from '@/api/common/type'

/** 文件管理 VO */
export interface FileVO {
  id: number
  fileName: string
  fileKey: string
  fileUrl: string
  fileSize: number
  fileType: string
  mimeType: string
  uploaderId: number
  uploaderName?: string
  createTime: string
}

/** 文件查询参数 */
export interface FileQuery extends PageParam {
  fileType?: string
}
