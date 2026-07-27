import type { FileQuery } from '@/api/file/type'

/** 默认查询参数 */
export const defaultFileQuery: FileQuery = {
  page: 1,
  pageSize: 12,
  fileType: '',
}

/** 文件类型过滤 Tab */
export const fileTypeTabs = [
  { label: '全部', value: '' },
  { label: '图片', value: 'IMAGE' },
  { label: '文档', value: 'DOCUMENT' },
  { label: '其他', value: 'OTHER' },
]
