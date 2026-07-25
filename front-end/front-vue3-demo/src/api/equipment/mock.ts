import type { BaseResponse, PageResponse } from '@/utils/service'
import type { EquipmentVO, EquipmentQuery, EquipmentDTO, AssignEquipmentDTO } from './type'

/* ================================================================
 * 设备数据：25 条
 *   - 投影仪(PROJECTOR): 8 台
 *   - 电脑(COMPUTER): 8 台
 *   - 白板(WHITEBOARD): 5 块
 *   - 网络设备(NETWORK): 4 台
 * ================================================================ */

const spaceNames: Record<number, string> = {
  201: '研讨室-201',
  202: '研讨室-202',
  203: '研讨室-203',
  301: '研讨室-301',
  302: '研讨室-302',
  401: '研讨室-401',
  101: '电子阅览区',
  102: '多媒体区',
}

function equipment(
  id: number,
  name: string,
  model: string,
  type: string,
  spaceId: number | null,
  purchase: string,
  maintenance: string,
  status: number,
  remark: string,
): EquipmentVO {
  return {
    id,
    equipmentName: name,
    equipmentModel: model,
    equipmentType: type as any,
    spaceId,
    spaceName: spaceId ? (spaceNames[spaceId] || `空间-${spaceId}`) : '',
    purchaseDate: purchase,
    lastMaintenanceDate: maintenance,
    status: status as any,
    remark,
    createTime: purchase + ' 00:00:00',
    updateTime: maintenance + ' 10:00:00',
  }
}

const equipmentList: EquipmentVO[] = [
  // ========== 投影仪 8台 ==========
  equipment(1, '爱普生投影仪-CB-L200W', 'EPSON-CB-L200W', 'PROJECTOR', 201, '2025-06-15', '2026-07-10', 1, '4200流明，WXGA分辨率，用于研讨室201'),
  equipment(2, '爱普生投影仪-CB-L200W', 'EPSON-CB-L200W', 'PROJECTOR', 202, '2025-06-15', '2026-07-10', 1, '4200流明，WXGA分辨率，用于研讨室202'),
  equipment(3, '爱普生投影仪-CB-L200W', 'EPSON-CB-L200W', 'PROJECTOR', 203, '2025-06-15', '2026-06-20', 1, '4200流明，WXGA分辨率，用于研讨室203'),
  equipment(4, '索尼激光投影仪-VPL-PHZ10', 'SONY-VPL-PHZ10', 'PROJECTOR', 301, '2025-09-01', '2026-07-15', 1, '5000流明，激光光源，用于大型研讨室'),
  equipment(5, '索尼激光投影仪-VPL-PHZ10', 'SONY-VPL-PHZ10', 'PROJECTOR', 302, '2025-09-01', '2026-07-15', 1, '5000流明，激光光源，用于大型研讨室'),
  equipment(6, '爱普生投影仪-CB-L200W', 'EPSON-CB-L200W', 'PROJECTOR', null, '2025-06-15', '2026-07-01', 0, '灯泡需要更换，待维修'),
  equipment(7, '明基投影仪-MH550', 'BENQ-MH550', 'PROJECTOR', 401, '2026-03-01', '2026-07-20', 1, '3800流明，全高清，用于研讨室401'),
  equipment(8, '明基投影仪-MH550', 'BENQ-MH550', 'PROJECTOR', null, '2026-03-01', '2026-06-15', 1, '备用投影仪，未分配空间'),

  // ========== 电脑 8台 ==========
  equipment(9, '联想启天M4500', 'LENOVO-M4500', 'COMPUTER', 101, '2025-03-15', '2026-07-18', 1, 'i5-12400/16GB/512GB SSD，电子阅览区用机'),
  equipment(10, '联想启天M4500', 'LENOVO-M4500', 'COMPUTER', 101, '2025-03-15', '2026-07-18', 1, 'i5-12400/16GB/512GB SSD，电子阅览区用机'),
  equipment(11, '联想启天M4500', 'LENOVO-M4500', 'COMPUTER', 101, '2025-03-15', '2026-07-18', 1, 'i5-12400/16GB/512GB SSD，电子阅览区用机'),
  equipment(12, '联想启天M4500', 'LENOVO-M4500', 'COMPUTER', 101, '2025-03-15', '2026-06-25', 1, 'i5-12400/16GB/512GB SSD，电子阅览区用机'),
  equipment(13, '联想启天M4500', 'LENOVO-M4500', 'COMPUTER', 101, '2025-03-15', '2026-07-05', 2, '主板损坏，已报废'),
  equipment(14, 'Dell OptiPlex 7000', 'DELL-OPT7000', 'COMPUTER', 102, '2025-09-01', '2026-07-22', 1, 'i7-12700/32GB/1TB SSD，多媒体区高性能工作站'),
  equipment(15, 'Dell OptiPlex 7000', 'DELL-OPT7000', 'COMPUTER', 102, '2025-09-01', '2026-07-22', 1, 'i7-12700/32GB/1TB SSD，多媒体区高性能工作站'),
  equipment(16, 'HP EliteDesk 800 G9', 'HP-800G9', 'COMPUTER', null, '2026-01-10', '2026-07-12', 1, 'i5-13500/16GB/512GB SSD，备用机'),

  // ========== 白板 5块 ==========
  equipment(17, '希沃交互式电子白板', 'SEEWO-SW86', 'WHITEBOARD', 201, '2025-06-20', '2026-07-10', 1, '86寸，4K触控，研讨室201配套'),
  equipment(18, '希沃交互式电子白板', 'SEEWO-SW86', 'WHITEBOARD', 202, '2025-06-20', '2026-07-10', 1, '86寸，4K触控，研讨室202配套'),
  equipment(19, '希沃交互式电子白板', 'SEEWO-SW86', 'WHITEBOARD', 203, '2025-06-20', '2026-06-20', 1, '86寸，4K触控，研讨室203配套'),
  equipment(20, '希沃交互式电子白板', 'SEEWO-SW75', 'WHITEBOARD', 301, '2025-09-05', '2026-07-15', 1, '75寸，4K触控，研讨室301配套'),
  equipment(21, '希沃交互式电子白板', 'SEEWO-SW75', 'WHITEBOARD', 302, '2025-09-05', '2026-07-15', 0, '75寸，触控失灵，待维修'),

  // ========== 网络设备 4台 ==========
  equipment(22, '华为交换机 S5735-L24P4X', 'HUAWEI-S5735', 'NETWORK', null, '2025-01-10', '2026-07-20', 1, '24口千兆POE交换机，机房核心设备'),
  equipment(23, '华为交换机 S5735-L24P4X', 'HUAWEI-S5735', 'NETWORK', null, '2025-01-10', '2026-07-20', 1, '24口千兆POE交换机，备用'),
  equipment(24, 'H3C无线AP WA6638', 'H3C-WA6638', 'NETWORK', null, '2025-06-01', '2026-07-01', 1, 'Wi-Fi6无线接入点，覆盖二楼阅览区'),
  equipment(25, 'H3C无线AP WA6638', 'H3C-WA6638', 'NETWORK', null, '2025-06-01', '2026-06-15', 1, 'Wi-Fi6无线接入点，覆盖三楼阅览区'),
]

export { equipmentList }

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

/* ================================================================
 * Mock API 函数
 * ================================================================ */

/** 获取设备列表（分页 + 筛选） */
export async function mockGetEquipmentList(query: EquipmentQuery): Promise<BaseResponse<PageResponse<EquipmentVO>>> {
  await delay()
  let filtered = [...equipmentList]

  if (query.equipmentType) {
    filtered = filtered.filter((e) => e.equipmentType === query.equipmentType)
  }
  if (query.spaceId !== undefined && query.spaceId !== null) {
    filtered = filtered.filter((e) => e.spaceId === query.spaceId)
  }
  if (query.status !== undefined && query.status !== null) {
    filtered = filtered.filter((e) => e.status === query.status)
  }
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    filtered = filtered.filter(
      (e) => e.equipmentName.toLowerCase().includes(kw) || e.equipmentModel.toLowerCase().includes(kw),
    )
  }

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return { code: 0, msg: 'ok', data: { total: filtered.length, list } }
}

/** 创建设备 */
export async function mockCreateEquipment(data: EquipmentDTO): Promise<BaseResponse<null>> {
  await delay()
  const maxId = Math.max(...equipmentList.map((e) => e.id), 25)
  const spName = data.spaceId ? (spaceNames[data.spaceId] || `空间-${data.spaceId}`) : ''
  const now = new Date().toISOString().replace('T', ' ').slice(0, 10)
  const newEq: EquipmentVO = {
    id: maxId + 1,
    equipmentName: data.equipmentName,
    equipmentModel: data.equipmentModel,
    equipmentType: data.equipmentType,
    spaceId: data.spaceId ?? null,
    spaceName: spName,
    purchaseDate: data.purchaseDate || now,
    lastMaintenanceDate: data.lastMaintenanceDate || now,
    status: 1,
    remark: data.remark || '',
    createTime: now + ' 00:00:00',
    updateTime: now + ' 10:00:00',
  }
  equipmentList.unshift(newEq)
  return { code: 0, msg: '创建成功', data: null }
}

/** 更新设备 */
export async function mockUpdateEquipment(data: EquipmentDTO): Promise<BaseResponse<null>> {
  await delay()
  const idx = equipmentList.findIndex((e) => e.id === data.id)
  if (idx === -1) return { code: 404, msg: '设备不存在', data: null }
  const spName = data.spaceId ? (spaceNames[data.spaceId] || `空间-${data.spaceId}`) : ''
  Object.assign(equipmentList[idx], data, {
    spaceName: spName,
    spaceId: data.spaceId ?? equipmentList[idx].spaceId,
    updateTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
  })
  return { code: 0, msg: '更新成功', data: null }
}

/** 删除设备 */
export async function mockDeleteEquipment(id: number): Promise<BaseResponse<null>> {
  await delay()
  const idx = equipmentList.findIndex((e) => e.id === id)
  if (idx === -1) return { code: 404, msg: '设备不存在', data: null }
  equipmentList.splice(idx, 1)
  return { code: 0, msg: '删除成功', data: null }
}

/** 分配设备到空间 */
export async function mockAssignEquipment(data: AssignEquipmentDTO): Promise<BaseResponse<null>> {
  await delay()
  const eq = equipmentList.find((e) => e.id === data.equipmentId)
  if (!eq) return { code: 404, msg: '设备不存在', data: null }
  eq.spaceId = data.spaceId
  eq.spaceName = spaceNames[data.spaceId] || `空间-${data.spaceId}`
  eq.updateTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
  return { code: 0, msg: '分配成功', data: null }
}

/** 取消分配设备 */
export async function mockUnassignEquipment(equipmentId: number): Promise<BaseResponse<null>> {
  await delay()
  const eq = equipmentList.find((e) => e.id === equipmentId)
  if (!eq) return { code: 404, msg: '设备不存在', data: null }
  eq.spaceId = null
  eq.spaceName = ''
  eq.updateTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
  return { code: 0, msg: '取消分配成功', data: null }
}
