import type { BaseResponse, PageResponse } from '@/utils/service'
import type { FileVO, FileQuery } from './type'

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

const baseUrl = 'https://oss.library-campus.edu/files/'

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const fileList: FileVO[] = [
  // === 15 images ===
  { id: 1,  fileName: 'avatar-admin-2026.png',       fileKey: 'images/avatar/admin_2026.png',       fileUrl: baseUrl + 'images/avatar/admin_2026.png',       fileSize: 245760,  fileType: 'IMAGE',    mimeType: 'image/png',  uploaderId: 1,  uploaderName: '系统管理员', createTime: '2026-07-25 10:00:00' },
  { id: 2,  fileName: 'avatar-zhangsan.png',         fileKey: 'images/avatar/zhangsan.png',         fileUrl: baseUrl + 'images/avatar/zhangsan.png',         fileSize: 184320,  fileType: 'IMAGE',    mimeType: 'image/png',  uploaderId: 2,  uploaderName: '张三',       createTime: '2026-07-24 15:30:00' },
  { id: 3,  fileName: 'avatar-lisi.jpg',             fileKey: 'images/avatar/lisi.jpg',             fileUrl: baseUrl + 'images/avatar/lisi.jpg',             fileSize: 153600,  fileType: 'IMAGE',    mimeType: 'image/jpeg', uploaderId: 4,  uploaderName: '李四',       createTime: '2026-07-24 14:20:00' },
  { id: 4,  fileName: 'avatar-wangwu.png',           fileKey: 'images/avatar/wangwu.png',           fileUrl: baseUrl + 'images/avatar/wangwu.png',           fileSize: 204800,  fileType: 'IMAGE',    mimeType: 'image/png',  uploaderId: 5,  uploaderName: '王五',       createTime: '2026-07-23 16:00:00' },
  { id: 5,  fileName: 'space-photo-quiet-A01.jpg',   fileKey: 'images/spaces/quiet_A01.jpg',        fileUrl: baseUrl + 'images/spaces/quiet_A01.jpg',        fileSize: 3145728, fileType: 'IMAGE',    mimeType: 'image/jpeg', uploaderId: 3,  uploaderName: '图书管理员', createTime: '2026-07-20 09:00:00' },
  { id: 6,  fileName: 'space-photo-quiet-A02.jpg',   fileKey: 'images/spaces/quiet_A02.jpg',        fileUrl: baseUrl + 'images/spaces/quiet_A02.jpg',        fileSize: 2097152, fileType: 'IMAGE',    mimeType: 'image/jpeg', uploaderId: 3,  uploaderName: '图书管理员', createTime: '2026-07-20 09:05:00' },
  { id: 7,  fileName: 'space-photo-digital-C05.jpg', fileKey: 'images/spaces/digital_C05.jpg',      fileUrl: baseUrl + 'images/spaces/digital_C05.jpg',      fileSize: 3670016, fileType: 'IMAGE',    mimeType: 'image/jpeg', uploaderId: 3,  uploaderName: '图书管理员', createTime: '2026-07-20 09:10:00' },
  { id: 8,  fileName: 'space-photo-room-301.jpg',    fileKey: 'images/spaces/room_301.jpg',          fileUrl: baseUrl + 'images/spaces/room_301.jpg',          fileSize: 4194304, fileType: 'IMAGE',    mimeType: 'image/jpeg', uploaderId: 3,  uploaderName: '图书管理员', createTime: '2026-07-20 09:15:00' },
  { id: 9,  fileName: 'space-photo-room-302.jpg',    fileKey: 'images/spaces/room_302.jpg',          fileUrl: baseUrl + 'images/spaces/room_302.jpg',          fileSize: 3932160, fileType: 'IMAGE',    mimeType: 'image/jpeg', uploaderId: 3,  uploaderName: '图书管理员', createTime: '2026-07-20 09:20:00' },
  { id: 10, fileName: 'space-photo-media-M01.jpg',   fileKey: 'images/spaces/media_M01.jpg',         fileUrl: baseUrl + 'images/spaces/media_M01.jpg',         fileSize: 4456448, fileType: 'IMAGE',    mimeType: 'image/jpeg', uploaderId: 3,  uploaderName: '图书管理员', createTime: '2026-07-20 09:25:00' },
  { id: 11, fileName: 'space-photo-leisure-L03.jpg', fileKey: 'images/spaces/leisure_L03.jpg',       fileUrl: baseUrl + 'images/spaces/leisure_L03.jpg',       fileSize: 2883584, fileType: 'IMAGE',    mimeType: 'image/jpeg', uploaderId: 3,  uploaderName: '图书管理员', createTime: '2026-07-20 09:30:00' },
  { id: 12, fileName: 'banner-notice-newyear.png',   fileKey: 'images/banner/notice_newyear.png',    fileUrl: baseUrl + 'images/banner/notice_newyear.png',    fileSize: 5242880, fileType: 'IMAGE',    mimeType: 'image/png',  uploaderId: 1,  uploaderName: '系统管理员', createTime: '2026-06-28 08:00:00' },
  { id: 13, fileName: 'equipment-projector-PRO01.jpg', fileKey: 'images/equipment/projector_PRO01.jpg', fileUrl: baseUrl + 'images/equipment/projector_PRO01.jpg', fileSize: 2621440, fileType: 'IMAGE', mimeType: 'image/jpeg', uploaderId: 3, uploaderName: '图书管理员', createTime: '2026-07-15 11:00:00' },
  { id: 14, fileName: 'equipment-whiteboard-WB01.jpg', fileKey: 'images/equipment/whiteboard_WB01.jpg', fileUrl: baseUrl + 'images/equipment/whiteboard_WB01.jpg', fileSize: 2359296, fileType: 'IMAGE', mimeType: 'image/jpeg', uploaderId: 3, uploaderName: '图书管理员', createTime: '2026-07-15 11:05:00' },
  { id: 15, fileName: 'qr-code-seat-B12.png',        fileKey: 'images/qrcode/seat_B12.png',          fileUrl: baseUrl + 'images/qrcode/seat_B12.png',          fileSize: 51200,   fileType: 'IMAGE',    mimeType: 'image/png',  uploaderId: 1,  uploaderName: '系统管理员', createTime: '2026-07-01 17:00:00' },

  // === 10 documents ===
  { id: 16, fileName: '空间导入模板.xlsx',             fileKey: 'documents/templates/space_import_template.xlsx',   fileUrl: baseUrl + 'documents/templates/space_import_template.xlsx',   fileSize: 15360,   fileType: 'DOCUMENT', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',  uploaderId: 1, uploaderName: '系统管理员', createTime: '2026-07-20 10:00:00' },
  { id: 17, fileName: '用户导入模板.xlsx',             fileKey: 'documents/templates/user_import_template.xlsx',    fileUrl: baseUrl + 'documents/templates/user_import_template.xlsx',    fileSize: 12288,   fileType: 'DOCUMENT', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',  uploaderId: 1, uploaderName: '系统管理员', createTime: '2026-07-20 10:05:00' },
  { id: 18, fileName: '预约导入模板.xlsx',             fileKey: 'documents/templates/reservation_import_template.xlsx', fileUrl: baseUrl + 'documents/templates/reservation_import_template.xlsx', fileSize: 13824, fileType: 'DOCUMENT', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', uploaderId: 1, uploaderName: '系统管理员', createTime: '2026-07-20 10:10:00' },
  { id: 19, fileName: '设备清单导入模板.xlsx',          fileKey: 'documents/templates/equipment_import_template.xlsx', fileUrl: baseUrl + 'documents/templates/equipment_import_template.xlsx', fileSize: 11776, fileType: 'DOCUMENT', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', uploaderId: 1, uploaderName: '系统管理员', createTime: '2026-07-20 10:15:00' },
  { id: 20, fileName: '空间使用率报表-20260724.xlsx',    fileKey: 'documents/reports/space_usage_20260724.xlsx',     fileUrl: baseUrl + 'documents/reports/space_usage_20260724.xlsx',     fileSize: 46080,   fileType: 'DOCUMENT', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',  uploaderId: 3, uploaderName: '图书管理员', createTime: '2026-07-25 01:30:00' },
  { id: 21, fileName: '签到率报表-20260724.xlsx',        fileKey: 'documents/reports/checkin_rate_20260724.xlsx',     fileUrl: baseUrl + 'documents/reports/checkin_rate_20260724.xlsx',     fileSize: 35840,   fileType: 'DOCUMENT', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',  uploaderId: 3, uploaderName: '图书管理员', createTime: '2026-07-25 01:35:00' },
  { id: 22, fileName: '信用分统计报表-20260724.xlsx',     fileKey: 'documents/reports/credit_stats_20260724.xlsx',     fileUrl: baseUrl + 'documents/reports/credit_stats_20260724.xlsx',     fileSize: 25600,   fileType: 'DOCUMENT', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',  uploaderId: 3, uploaderName: '图书管理员', createTime: '2026-07-25 01:40:00' },
  { id: 23, fileName: '图书馆使用指南.pdf',              fileKey: 'documents/guides/library_usage_guide.pdf',          fileUrl: baseUrl + 'documents/guides/library_usage_guide.pdf',          fileSize: 2621440,  fileType: 'DOCUMENT', mimeType: 'application/pdf', uploaderId: 1, uploaderName: '系统管理员', createTime: '2026-06-01 14:00:00' },
  { id: 24, fileName: '预约系统操作手册.pdf',            fileKey: 'documents/guides/reservation_manual.pdf',           fileUrl: baseUrl + 'documents/guides/reservation_manual.pdf',           fileSize: 3145728,  fileType: 'DOCUMENT', mimeType: 'application/pdf', uploaderId: 1, uploaderName: '系统管理员', createTime: '2026-06-15 09:00:00' },
  { id: 25, fileName: '设备使用说明书.pdf',              fileKey: 'documents/guides/equipment_manual.pdf',             fileUrl: baseUrl + 'documents/guides/equipment_manual.pdf',             fileSize: 1572864,  fileType: 'DOCUMENT', mimeType: 'application/pdf', uploaderId: 3, uploaderName: '图书管理员', createTime: '2026-07-10 16:00:00' },

  // === 5 other ===
  { id: 26, fileName: '系统日志-20260724.log',          fileKey: 'logs/system_20260724.log',              fileUrl: baseUrl + 'logs/system_20260724.log',              fileSize: 2097152,  fileType: 'OTHER', mimeType: 'text/plain',       uploaderId: 0, uploaderName: '系统自动',   createTime: '2026-07-25 00:00:00' },
  { id: 27, fileName: '数据库备份-20260724.sql.gz',      fileKey: 'backups/db_backup_20260724.sql.gz',     fileUrl: baseUrl + 'backups/db_backup_20260724.sql.gz',     fileSize: 52428800, fileType: 'OTHER', mimeType: 'application/gzip',  uploaderId: 0, uploaderName: '系统自动',   createTime: '2026-07-25 04:00:00' },
  { id: 28, fileName: '数据库备份-20260723.sql.gz',      fileKey: 'backups/db_backup_20260723.sql.gz',     fileUrl: baseUrl + 'backups/db_backup_20260723.sql.gz',     fileSize: 51380224, fileType: 'OTHER', mimeType: 'application/gzip',  uploaderId: 0, uploaderName: '系统自动',   createTime: '2026-07-24 04:00:00' },
  { id: 29, fileName: '数据库备份-20260722.sql.gz',      fileKey: 'backups/db_backup_20260722.sql.gz',     fileUrl: baseUrl + 'backups/db_backup_20260722.sql.gz',     fileSize: 50331648, fileType: 'OTHER', mimeType: 'application/gzip',  uploaderId: 0, uploaderName: '系统自动',   createTime: '2026-07-23 04:00:00' },
  { id: 30, fileName: '临时导出-202607251430.xlsx',       fileKey: 'temp/export_202607251430.xlsx',         fileUrl: baseUrl + 'temp/export_202607251430.xlsx',         fileSize: 102400,   fileType: 'OTHER', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', uploaderId: 3, uploaderName: '图书管理员', createTime: '2026-07-25 14:30:00' },
]

export async function mockGetFiles(query: FileQuery): Promise<BaseResponse<PageResponse<FileVO>>> {
  await delay()
  let filtered = [...fileList]

  if (query.fileType) {
    filtered = filtered.filter((f) => f.fileType === query.fileType)
  }

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return { code: 0, msg: 'ok', data: { total: filtered.length, list } }
}

export async function mockUploadFile(data: { fileName: string; fileType: string; fileSize: number }): Promise<BaseResponse<FileVO>> {
  await delay()
  const ext = data.fileName.split('.').pop()?.toLowerCase() || ''
  const mimeMap: Record<string, string> = {
    png: 'image/png', jpg: 'image/jpeg', jpeg: 'image/jpeg', gif: 'image/gif',
    pdf: 'application/pdf', xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    txt: 'text/plain', zip: 'application/zip', gz: 'application/gzip',
  }

  const newFile: FileVO = {
    id: fileList.length + 1,
    fileName: data.fileName,
    fileKey: `uploads/${Date.now()}_${data.fileName}`,
    fileUrl: baseUrl + `uploads/${Date.now()}_${data.fileName}`,
    fileSize: data.fileSize,
    fileType: data.fileType || 'OTHER',
    mimeType: mimeMap[ext] || 'application/octet-stream',
    uploaderId: 1,
    uploaderName: '系统管理员',
    createTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
  }
  fileList.unshift(newFile)
  return { code: 0, msg: '上传成功', data: newFile }
}

export async function mockDeleteFile(id: number): Promise<BaseResponse<null>> {
  await delay()
  const idx = fileList.findIndex((f) => f.id === id)
  if (idx === -1) return { code: 404, msg: '文件不存在', data: null }
  fileList.splice(idx, 1)
  return { code: 0, msg: '删除成功', data: null }
}

export async function mockDownloadFile(id: number): Promise<BaseResponse<{ url: string; fileName: string }>> {
  await delay()
  const file = fileList.find((f) => f.id === id)
  if (!file) return { code: 404, msg: '文件不存在', data: null as any }
  return { code: 0, msg: 'ok', data: { url: file.fileUrl, fileName: file.fileName } }
}

export { fileList }
