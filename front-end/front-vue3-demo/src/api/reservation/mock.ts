import type { BaseResponse, PageResponse } from '@/utils/service'
import type { ReservationVO, ReservationQuery, CreateReservationDTO } from './type'

/* ========= 空间引用（与 space/mock.ts 数据一致） ========= */
interface SpaceRef { id: number; name: string; type: string }
const spaceRefs: SpaceRef[] = [
  // 1F
  { id: 1, name: '安区-01号座', type: 'SEAT' },
  { id: 2, name: '安区-02号座', type: 'SEAT' },
  { id: 3, name: '安区-03号座', type: 'SEAT' },
  { id: 4, name: '安区-04号座', type: 'SEAT' },
  { id: 5, name: '安区-05号座', type: 'SEAT' },
  { id: 11, name: '研讨室-101', type: 'ROOM' },
  { id: 12, name: '研讨室-102', type: 'ROOM' },
  { id: 15, name: '多区-03号座', type: 'SEAT' },
  { id: 18, name: '休区-01号座', type: 'SEAT' },
  { id: 21, name: '休区-04号座', type: 'SEAT' },
  // 2F
  { id: 23, name: '安区-01号座', type: 'SEAT' },
  { id: 26, name: '安区-04号座', type: 'SEAT' },
  { id: 28, name: '电区-01号座', type: 'SEAT' },
  { id: 33, name: '研讨室-201', type: 'ROOM' },
  { id: 34, name: '研讨室-202', type: 'ROOM' },
  { id: 37, name: '多区-02号座', type: 'SEAT' },
  { id: 40, name: '休区-01号座', type: 'SEAT' },
  // 3F
  { id: 45, name: '安区-01号座', type: 'SEAT' },
  { id: 48, name: '电区-01号座', type: 'SEAT' },
  { id: 55, name: '研讨室-301', type: 'ROOM' },
  { id: 56, name: '研讨室-302', type: 'ROOM' },
  { id: 59, name: '多区-03号座', type: 'SEAT' },
  { id: 63, name: '休区-02号座', type: 'SEAT' },
  // 4F
  { id: 67, name: '安区-01号座', type: 'SEAT' },
  { id: 70, name: '电区-01号座', type: 'SEAT' },
  { id: 77, name: '研讨室-401', type: 'ROOM' },
  { id: 78, name: '研讨室-402', type: 'ROOM' },
  { id: 81, name: '多区-03号座', type: 'SEAT' },
  { id: 85, name: '休区-03号座', type: 'SEAT' },
]
function getSpace(id: number): SpaceRef {
  return spaceRefs.find((s) => s.id === id) || { id, name: `座位-${id}`, type: 'SEAT' }
}

/* ========= 用户引用 ========= */
const userNames: Record<number, string> = {
  1: '系统管理员',
  2: '张三',
  3: '图书管理员',
  4: '李四',
  5: '王五',
}

/* ========= 生成 60 条预约数据 ========= */
function generateReservations(): ReservationVO[] {
  const list: ReservationVO[] = []
  let codeSeq = 0
  const rc = (date: string) => `RES${date.replace(/-/g, '')}${String(++codeSeq).padStart(3, '0')}`

  /* ----- userId=2（张三）30 条 ----- */
  const u2 = 2

  // status=0 已预约 8条 — 近期/未来
  list.push({ id: 1, reservationCode: rc('20260725'), userId: u2, spaceId: 1, startTime: '2026-07-25 14:00:00', endTime: '2026-07-25 16:00:00', participants: [], purpose: '复习高数', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-24 10:30:00', userName: userNames[u2], ...getSpace(1) } as any)
  list.push({ id: 2, reservationCode: rc('20260726'), userId: u2, spaceId: 3, startTime: '2026-07-26 09:00:00', endTime: '2026-07-26 11:00:00', participants: [], purpose: '英语阅读', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-25 08:00:00', userName: userNames[u2], ...getSpace(3) } as any)
  list.push({ id: 3, reservationCode: rc('20260726'), userId: u2, spaceId: 11, startTime: '2026-07-26 14:00:00', endTime: '2026-07-26 17:00:00', participants: ['张三', '李四', '王五'], purpose: '小组课题讨论', approvalStatus: 2, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-25 09:00:00', userName: userNames[u2], ...getSpace(11) } as any)
  list.push({ id: 4, reservationCode: rc('20260727'), userId: u2, spaceId: 4, startTime: '2026-07-27 08:00:00', endTime: '2026-07-27 10:00:00', participants: [], purpose: '早自习', approvalStatus: 3, approvalRemark: '同意预约', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-26 20:00:00', userName: userNames[u2], ...getSpace(4) } as any)
  list.push({ id: 5, reservationCode: rc('20260727'), userId: u2, spaceId: 33, startTime: '2026-07-27 14:00:00', endTime: '2026-07-27 17:00:00', participants: ['张三', '李四'], purpose: '毕业论文讨论', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-26 15:00:00', userName: userNames[u2], ...getSpace(33) } as any)
  list.push({ id: 6, reservationCode: rc('20260728'), userId: u2, spaceId: 15, startTime: '2026-07-28 09:00:00', endTime: '2026-07-28 11:00:00', participants: [], purpose: '看网课', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-27 22:00:00', userName: userNames[u2], ...getSpace(15) } as any)
  list.push({ id: 7, reservationCode: rc('20260729'), userId: u2, spaceId: 55, startTime: '2026-07-29 15:00:00', endTime: '2026-07-29 18:00:00', participants: ['张三', '李四', '王五', '赵六'], purpose: '项目答辩排练', approvalStatus: 2, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-28 10:00:00', userName: userNames[u2], ...getSpace(55) } as any)
  list.push({ id: 8, reservationCode: rc('20260730'), userId: u2, spaceId: 18, startTime: '2026-07-30 10:00:00', endTime: '2026-07-30 12:00:00', participants: [], purpose: '休闲阅读', approvalStatus: 4, approvalRemark: '该时段已被占用', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-29 08:00:00', userName: userNames[u2], ...getSpace(18) } as any)

  // status=1 已签到 6条 — 今天
  list.push({ id: 9, reservationCode: rc('20260725'), userId: u2, spaceId: 2, startTime: '2026-07-25 08:00:00', endTime: '2026-07-25 10:00:00', participants: [], purpose: '晨读', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 1, createTime: '2026-07-24 22:00:00', userName: userNames[u2], ...getSpace(2) } as any)
  list.push({ id: 10, reservationCode: rc('20260725'), userId: u2, spaceId: 12, startTime: '2026-07-25 09:00:00', endTime: '2026-07-25 12:00:00', participants: ['张三', '李四'], purpose: '课程设计讨论', approvalStatus: 3, approvalRemark: '通过', cancelReason: '', cancelTime: '', status: 1, createTime: '2026-07-24 16:00:00', userName: userNames[u2], ...getSpace(12) } as any)
  list.push({ id: 11, reservationCode: rc('20260725'), userId: u2, spaceId: 5, startTime: '2026-07-25 10:00:00', endTime: '2026-07-25 12:00:00', participants: [], purpose: '写作业', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 1, createTime: '2026-07-25 08:30:00', userName: userNames[u2], ...getSpace(5) } as any)
  list.push({ id: 12, reservationCode: rc('20260725'), userId: u2, spaceId: 23, startTime: '2026-07-25 13:00:00', endTime: '2026-07-25 15:00:00', participants: [], purpose: '午间自习', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 1, createTime: '2026-07-25 11:00:00', userName: userNames[u2], ...getSpace(23) } as any)
  list.push({ id: 13, reservationCode: rc('20260725'), userId: u2, spaceId: 34, startTime: '2026-07-25 14:00:00', endTime: '2026-07-25 17:00:00', participants: ['张三', '李四', '王五'], purpose: '期末复习', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 1, createTime: '2026-07-25 10:00:00', userName: userNames[u2], ...getSpace(34) } as any)
  list.push({ id: 14, reservationCode: rc('20260725'), userId: u2, spaceId: 77, startTime: '2026-07-25 18:00:00', endTime: '2026-07-25 21:00:00', participants: ['张三', '赵六'], purpose: '编程练习', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 1, createTime: '2026-07-25 14:00:00', userName: userNames[u2], ...getSpace(77) } as any)

  // status=2 已完成 5条
  list.push({ id: 15, reservationCode: rc('20260724'), userId: u2, spaceId: 28, startTime: '2026-07-24 09:00:00', endTime: '2026-07-24 11:00:00', participants: [], purpose: '查阅电子文献', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-23 20:00:00', userName: userNames[u2], ...getSpace(28) } as any)
  list.push({ id: 16, reservationCode: rc('20260723'), userId: u2, spaceId: 33, startTime: '2026-07-23 14:00:00', endTime: '2026-07-23 17:00:00', participants: ['张三', '李四'], purpose: '项目讨论', approvalStatus: 3, approvalRemark: '同意', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-22 10:00:00', userName: userNames[u2], ...getSpace(33) } as any)
  list.push({ id: 17, reservationCode: rc('20260722'), userId: u2, spaceId: 26, startTime: '2026-07-22 08:00:00', endTime: '2026-07-22 10:00:00', participants: [], purpose: '背诵单词', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-21 22:00:00', userName: userNames[u2], ...getSpace(26) } as any)
  list.push({ id: 18, reservationCode: rc('20260721'), userId: u2, spaceId: 21, startTime: '2026-07-21 10:00:00', endTime: '2026-07-21 12:00:00', participants: [], purpose: '阅读期刊', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-20 18:00:00', userName: userNames[u2], ...getSpace(21) } as any)
  list.push({ id: 19, reservationCode: rc('20260720'), userId: u2, spaceId: 56, startTime: '2026-07-20 14:00:00', endTime: '2026-07-20 17:00:00', participants: ['张三', '李四', '王五', '赵六'], purpose: '课程答辩', approvalStatus: 3, approvalRemark: '已审批', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-19 09:00:00', userName: userNames[u2], ...getSpace(56) } as any)

  // status=3 已取消 7条
  list.push({ id: 20, reservationCode: rc('20260724'), userId: u2, spaceId: 55, startTime: '2026-07-24 15:00:00', endTime: '2026-07-24 18:00:00', participants: ['张三', '李四'], purpose: '项目讨论', approvalStatus: 1, approvalRemark: '', cancelReason: '临时有事无法参加', cancelTime: '2026-07-24 10:00:00', status: 3, createTime: '2026-07-23 22:00:00', userName: userNames[u2], ...getSpace(55) } as any)
  list.push({ id: 21, reservationCode: rc('20260723'), userId: u2, spaceId: 37, startTime: '2026-07-23 09:00:00', endTime: '2026-07-23 11:00:00', participants: [], purpose: '看课件', approvalStatus: 1, approvalRemark: '', cancelReason: '身体不适，需要休息', cancelTime: '2026-07-22 22:00:00', status: 3, createTime: '2026-07-22 20:00:00', userName: userNames[u2], ...getSpace(37) } as any)
  list.push({ id: 22, reservationCode: rc('20260722'), userId: u2, spaceId: 40, startTime: '2026-07-22 14:00:00', endTime: '2026-07-22 16:00:00', participants: [], purpose: '休闲阅读', approvalStatus: 1, approvalRemark: '', cancelReason: '临时有课', cancelTime: '2026-07-22 08:00:00', status: 3, createTime: '2026-07-21 15:00:00', userName: userNames[u2], ...getSpace(40) } as any)
  list.push({ id: 23, reservationCode: rc('20260721'), userId: u2, spaceId: 12, startTime: '2026-07-21 10:00:00', endTime: '2026-07-21 13:00:00', participants: ['张三', '王五'], purpose: '准备PPT', approvalStatus: 3, approvalRemark: '已通过', cancelReason: '组员请假，改天再约', cancelTime: '2026-07-20 20:00:00', status: 3, createTime: '2026-07-19 14:00:00', userName: userNames[u2], ...getSpace(12) } as any)
  list.push({ id: 24, reservationCode: rc('20260720'), userId: u2, spaceId: 45, startTime: '2026-07-20 14:00:00', endTime: '2026-07-20 16:00:00', participants: [], purpose: '自习', approvalStatus: 1, approvalRemark: '', cancelReason: '不需要了', cancelTime: '2026-07-20 09:00:00', status: 3, createTime: '2026-07-19 10:00:00', userName: userNames[u2], ...getSpace(45) } as any)
  list.push({ id: 25, reservationCode: rc('20260719'), userId: u2, spaceId: 34, startTime: '2026-07-19 09:00:00', endTime: '2026-07-19 12:00:00', participants: ['张三', '赵六'], purpose: '课题交流', approvalStatus: 2, approvalRemark: '', cancelReason: '审批时间过长，已过期', cancelTime: '2026-07-19 08:30:00', status: 3, createTime: '2026-07-18 16:00:00', userName: userNames[u2], ...getSpace(34) } as any)
  list.push({ id: 26, reservationCode: rc('20260718'), userId: u2, spaceId: 48, startTime: '2026-07-18 08:00:00', endTime: '2026-07-18 10:00:00', participants: [], purpose: '刷题', approvalStatus: 1, approvalRemark: '', cancelReason: '睡过头了', cancelTime: '2026-07-18 07:50:00', status: 3, createTime: '2026-07-17 22:00:00', userName: userNames[u2], ...getSpace(48) } as any)

  // status=4 已违约 4条
  list.push({ id: 27, reservationCode: rc('20260719'), userId: u2, spaceId: 56, startTime: '2026-07-19 14:00:00', endTime: '2026-07-19 17:00:00', participants: ['张三', '李四', '王五'], purpose: '课题研讨', approvalStatus: 3, approvalRemark: '已审批', cancelReason: '', cancelTime: '', status: 4, createTime: '2026-07-18 10:00:00', userName: userNames[u2], ...getSpace(56) } as any)
  list.push({ id: 28, reservationCode: rc('20260717'), userId: u2, spaceId: 59, startTime: '2026-07-17 09:00:00', endTime: '2026-07-17 11:00:00', participants: [], purpose: '查阅资料', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 4, createTime: '2026-07-16 20:00:00', userName: userNames[u2], ...getSpace(59) } as any)
  list.push({ id: 29, reservationCode: rc('20260714'), userId: u2, spaceId: 63, startTime: '2026-07-14 08:00:00', endTime: '2026-07-14 10:00:00', participants: [], purpose: '晨间自习', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 4, createTime: '2026-07-13 20:00:00', userName: userNames[u2], ...getSpace(63) } as any)
  list.push({ id: 30, reservationCode: rc('20260712'), userId: u2, spaceId: 67, startTime: '2026-07-12 10:00:00', endTime: '2026-07-12 12:00:00', participants: [], purpose: '看论文', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 4, createTime: '2026-07-11 15:00:00', userName: userNames[u2], ...getSpace(67) } as any)

  /* ----- userId=3（图书管理员）12 条 ----- */
  const u3 = 3
  list.push({ id: 31, reservationCode: rc('20260725'), userId: u3, spaceId: 1, startTime: '2026-07-25 09:00:00', endTime: '2026-07-25 11:00:00', participants: [], purpose: '阅读专业书籍', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 1, createTime: '2026-07-24 10:00:00', userName: userNames[u3], ...getSpace(1) } as any)
  list.push({ id: 32, reservationCode: rc('20260725'), userId: u3, spaceId: 70, startTime: '2026-07-25 14:00:00', endTime: '2026-07-25 16:00:00', participants: [], purpose: '电子资源检索', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-25 08:00:00', userName: userNames[u3], ...getSpace(70) } as any)
  list.push({ id: 33, reservationCode: rc('20260724'), userId: u3, spaceId: 11, startTime: '2026-07-24 10:00:00', endTime: '2026-07-24 13:00:00', participants: ['图书管理员', '李四'], purpose: '新书编目讨论', approvalStatus: 3, approvalRemark: '通过', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-23 09:00:00', userName: userNames[u3], ...getSpace(11) } as any)
  list.push({ id: 34, reservationCode: rc('20260723'), userId: u3, spaceId: 45, startTime: '2026-07-23 08:00:00', endTime: '2026-07-23 10:00:00', participants: [], purpose: '文献整理', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-22 18:00:00', userName: userNames[u3], ...getSpace(45) } as any)
  list.push({ id: 35, reservationCode: rc('20260722'), userId: u3, spaceId: 78, startTime: '2026-07-22 14:00:00', endTime: '2026-07-22 17:00:00', participants: ['图书管理员', '王五'], purpose: '采购方案讨论', approvalStatus: 3, approvalRemark: '同意', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-21 10:00:00', userName: userNames[u3], ...getSpace(78) } as any)
  list.push({ id: 36, reservationCode: rc('20260721'), userId: u3, spaceId: 18, startTime: '2026-07-21 15:00:00', endTime: '2026-07-21 17:00:00', participants: [], purpose: '阅读', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 3, cancelTime: '2026-07-21 12:00:00', createTime: '2026-07-20 10:00:00', userName: userNames[u3], ...getSpace(18) } as any)
  list.push({ id: 37, reservationCode: rc('20260720'), userId: u3, spaceId: 55, startTime: '2026-07-20 09:00:00', endTime: '2026-07-20 12:00:00', participants: ['图书管理员', '张三', '赵六'], purpose: '读书分享会', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-19 14:00:00', userName: userNames[u3], ...getSpace(55) } as any)
  list.push({ id: 38, reservationCode: rc('20260719'), userId: u3, spaceId: 48, startTime: '2026-07-19 10:00:00', endTime: '2026-07-19 12:00:00', participants: [], purpose: '编目工作', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-18 20:00:00', userName: userNames[u3], ...getSpace(48) } as any)
  list.push({ id: 39, reservationCode: rc('20260718'), userId: u3, spaceId: 81, startTime: '2026-07-18 14:00:00', endTime: '2026-07-18 16:00:00', participants: [], purpose: '多媒体资料整理', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 3, cancelTime: '2026-07-18 09:00:00', createTime: '2026-07-17 16:00:00', userName: userNames[u3], ...getSpace(81) } as any)
  list.push({ id: 40, reservationCode: rc('20260726'), userId: u3, spaceId: 85, startTime: '2026-07-26 09:00:00', endTime: '2026-07-26 11:00:00', participants: [], purpose: '期刊阅读', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-25 16:00:00', userName: userNames[u3], ...getSpace(85) } as any)
  list.push({ id: 41, reservationCode: rc('20260717'), userId: u3, spaceId: 59, startTime: '2026-07-17 08:00:00', endTime: '2026-07-17 10:00:00', participants: [], purpose: '晨读', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 4, createTime: '2026-07-16 22:00:00', userName: userNames[u3], ...getSpace(59) } as any)
  list.push({ id: 42, reservationCode: rc('20260715'), userId: u3, spaceId: 40, startTime: '2026-07-15 14:00:00', endTime: '2026-07-15 16:00:00', participants: [], purpose: '整理书单', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 3, cancelTime: '2026-07-15 10:00:00', createTime: '2026-07-14 20:00:00', userName: userNames[u3], ...getSpace(40) } as any)

  /* ----- userId=1（系统管理员）10 条 ----- */
  const u1 = 1
  list.push({ id: 43, reservationCode: rc('20260725'), userId: u1, spaceId: 5, startTime: '2026-07-25 08:00:00', endTime: '2026-07-25 10:00:00', participants: [], purpose: '系统巡检', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 1, createTime: '2026-07-24 20:00:00', userName: userNames[u1], ...getSpace(5) } as any)
  list.push({ id: 44, reservationCode: rc('20260724'), userId: u1, spaceId: 23, startTime: '2026-07-24 09:00:00', endTime: '2026-07-24 11:00:00', participants: [], purpose: '测试新系统', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-23 10:00:00', userName: userNames[u1], ...getSpace(23) } as any)
  list.push({ id: 45, reservationCode: rc('20260722'), userId: u1, spaceId: 77, startTime: '2026-07-22 14:00:00', endTime: '2026-07-22 17:00:00', participants: ['系统管理员', '张三'], purpose: '信息化会议', approvalStatus: 3, approvalRemark: '同意', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-21 09:00:00', userName: userNames[u1], ...getSpace(77) } as any)
  list.push({ id: 46, reservationCode: rc('20260721'), userId: u1, spaceId: 48, startTime: '2026-07-21 10:00:00', endTime: '2026-07-21 12:00:00', participants: [], purpose: '查阅技术文档', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-20 15:00:00', userName: userNames[u1], ...getSpace(48) } as any)
  list.push({ id: 47, reservationCode: rc('20260720'), userId: u1, spaceId: 85, startTime: '2026-07-20 08:00:00', endTime: '2026-07-20 10:00:00', participants: [], purpose: '阅读行业期刊', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-19 20:00:00', userName: userNames[u1], ...getSpace(85) } as any)
  list.push({ id: 48, reservationCode: rc('20260719'), userId: u1, spaceId: 15, startTime: '2026-07-19 14:00:00', endTime: '2026-07-19 16:00:00', participants: [], purpose: '培训准备', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 3, cancelTime: '2026-07-19 08:00:00', createTime: '2026-07-18 10:00:00', userName: userNames[u1], ...getSpace(15) } as any)
  list.push({ id: 49, reservationCode: rc('20260718'), userId: u1, spaceId: 26, startTime: '2026-07-18 09:00:00', endTime: '2026-07-18 11:00:00', participants: [], purpose: '代码审查', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-17 22:00:00', userName: userNames[u1], ...getSpace(26) } as any)
  list.push({ id: 50, reservationCode: rc('20260726'), userId: u1, spaceId: 45, startTime: '2026-07-26 10:00:00', endTime: '2026-07-26 12:00:00', participants: [], purpose: '数据分析', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-25 14:00:00', userName: userNames[u1], ...getSpace(45) } as any)
  list.push({ id: 51, reservationCode: rc('20260727'), userId: u1, spaceId: 56, startTime: '2026-07-27 14:00:00', endTime: '2026-07-27 17:00:00', participants: ['系统管理员', '张三', '李四'], purpose: '系统升级讨论', approvalStatus: 2, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-26 09:00:00', userName: userNames[u1], ...getSpace(56) } as any)
  list.push({ id: 52, reservationCode: rc('20260716'), userId: u1, spaceId: 70, startTime: '2026-07-16 08:00:00', endTime: '2026-07-16 10:00:00', participants: [], purpose: '安全检查', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 4, createTime: '2026-07-15 20:00:00', userName: userNames[u1], ...getSpace(70) } as any)

  /* ----- userId=4（李四）8 条 ----- */
  const u4 = 4
  list.push({ id: 53, reservationCode: rc('20260725'), userId: u4, spaceId: 26, startTime: '2026-07-25 10:00:00', endTime: '2026-07-25 12:00:00', participants: [], purpose: '背考研单词', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 1, createTime: '2026-07-25 07:00:00', userName: userNames[u4], ...getSpace(26) } as any)
  list.push({ id: 54, reservationCode: rc('20260725'), userId: u4, spaceId: 37, startTime: '2026-07-25 14:00:00', endTime: '2026-07-25 16:00:00', participants: [], purpose: '看考研视频', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-25 09:00:00', userName: userNames[u4], ...getSpace(37) } as any)
  list.push({ id: 55, reservationCode: rc('20260723'), userId: u4, spaceId: 18, startTime: '2026-07-23 09:00:00', endTime: '2026-07-23 11:00:00', participants: [], purpose: '复习政治', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-22 20:00:00', userName: userNames[u4], ...getSpace(18) } as any)
  list.push({ id: 56, reservationCode: rc('20260721'), userId: u4, spaceId: 55, startTime: '2026-07-21 14:00:00', endTime: '2026-07-21 17:00:00', participants: ['李四', '张三'], purpose: '考研数学讨论', approvalStatus: 3, approvalRemark: '已审批', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-20 10:00:00', userName: userNames[u4], ...getSpace(55) } as any)
  list.push({ id: 57, reservationCode: rc('20260720'), userId: u4, spaceId: 40, startTime: '2026-07-20 08:00:00', endTime: '2026-07-20 10:00:00', participants: [], purpose: '晨读英语', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 2, createTime: '2026-07-19 22:00:00', userName: userNames[u4], ...getSpace(40) } as any)
  list.push({ id: 58, reservationCode: rc('20260719'), userId: u4, spaceId: 59, startTime: '2026-07-19 15:00:00', endTime: '2026-07-19 17:00:00', participants: [], purpose: '刷真题', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 3, cancelTime: '2026-07-19 10:00:00', createTime: '2026-07-18 20:00:00', userName: userNames[u4], ...getSpace(59) } as any)
  list.push({ id: 59, reservationCode: rc('20260717'), userId: u4, spaceId: 67, startTime: '2026-07-17 08:00:00', endTime: '2026-07-17 10:00:00', participants: [], purpose: '专业课复习', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 4, createTime: '2026-07-16 20:00:00', userName: userNames[u4], ...getSpace(67) } as any)
  list.push({ id: 60, reservationCode: rc('20260726'), userId: u4, spaceId: 63, startTime: '2026-07-26 08:00:00', endTime: '2026-07-26 10:00:00', participants: [], purpose: '早自习', approvalStatus: 1, approvalRemark: '', cancelReason: '', cancelTime: '', status: 0, createTime: '2026-07-25 20:00:00', userName: userNames[u4], ...getSpace(63) } as any)

  return list
}

const reservationList: ReservationVO[] = generateReservations()
let nextId = 61

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

/** 获取我的预约列表 */
export async function mockGetMyReservations(query: ReservationQuery): Promise<BaseResponse<PageResponse<ReservationVO>>> {
  await delay()
  let filtered = reservationList.filter((r) => r.userId === 2) // 默认查询当前用户（userId=2 张三）

  if (query.status !== undefined) filtered = filtered.filter((r) => r.status === query.status)
  if (query.approvalStatus !== undefined) filtered = filtered.filter((r) => r.approvalStatus === query.approvalStatus)
  if (query.spaceType) filtered = filtered.filter((r) => r.spaceType === query.spaceType)
  if (query.startTime) filtered = filtered.filter((r) => r.startTime >= query.startTime!)
  if (query.endTime) filtered = filtered.filter((r) => r.endTime <= query.endTime!)
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    filtered = filtered.filter(
      (r) =>
        r.spaceName.includes(kw) ||
        r.reservationCode.toLowerCase().includes(kw) ||
        r.purpose.includes(kw),
    )
  }

  filtered.sort((a, b) => b.createTime.localeCompare(a.createTime))

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  return { code: 0, msg: 'ok', data: { total: filtered.length, list: filtered.slice(start, start + pageSize) } }
}

/** 获取预约详情 */
export async function mockGetReservationById(id: number): Promise<BaseResponse<ReservationVO>> {
  await delay()
  const item = reservationList.find((r) => r.id === id)
  if (!item) return { code: 404, msg: '预约不存在', data: null as any }
  return { code: 0, msg: 'ok', data: { ...item } }
}

/** 创建预约 */
export async function mockCreateReservation(data: CreateReservationDTO): Promise<BaseResponse<ReservationVO>> {
  await delay()
  const space = getSpace(data.spaceId)
  const isRoom = space.type === 'ROOM'

  // 研讨会需要审批
  const needsApproval = isRoom ? 2 : 1

  const newItem: ReservationVO = {
    id: nextId++,
    reservationCode: `RES${new Date().toISOString().slice(0, 10).replace(/-/g, '')}${String(nextId).padStart(3, '0')}`,
    userId: 2,
    spaceId: data.spaceId,
    startTime: data.startTime,
    endTime: data.endTime,
    participants: data.participants || [],
    purpose: data.purpose || '',
    approvalStatus: needsApproval,
    approvalRemark: '',
    cancelReason: '',
    cancelTime: '',
    status: 0,
    createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
    userName: '张三',
    spaceName: space.name,
    spaceType: space.type,
  }
  reservationList.unshift(newItem)
  return { code: 0, msg: '预约成功', data: newItem }
}

/** 取消预约 */
export async function mockCancelReservation(id: number, reason: string): Promise<BaseResponse<null>> {
  await delay()
  const item = reservationList.find((r) => r.id === id)
  if (!item) return { code: 404, msg: '预约不存在', data: null }
  if (item.status === 1) return { code: 1001, msg: '已签到无法取消，请签退', data: null }
  if (item.status !== 0) return { code: 1002, msg: '当前状态不可取消', data: null }
  item.status = 3
  item.cancelReason = reason
  item.cancelTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
  return { code: 0, msg: '取消成功', data: null }
}

/** 管理员：获取所有预约 */
export async function mockGetAllReservations(query: ReservationQuery): Promise<BaseResponse<PageResponse<ReservationVO>>> {
  await delay()
  let filtered = [...reservationList]

  if (query.status !== undefined) filtered = filtered.filter((r) => r.status === query.status)
  if (query.approvalStatus !== undefined) filtered = filtered.filter((r) => r.approvalStatus === query.approvalStatus)
  if (query.spaceType) filtered = filtered.filter((r) => r.spaceType === query.spaceType)
  if (query.startTime) filtered = filtered.filter((r) => r.startTime >= query.startTime!)
  if (query.endTime) filtered = filtered.filter((r) => r.endTime <= query.endTime!)
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    filtered = filtered.filter(
      (r) =>
        r.spaceName.includes(kw) ||
        r.reservationCode.toLowerCase().includes(kw) ||
        r.userName.includes(kw) ||
        r.purpose.includes(kw),
    )
  }

  filtered.sort((a, b) => b.createTime.localeCompare(a.createTime))

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  return { code: 0, msg: 'ok', data: { total: filtered.length, list: filtered.slice(start, start + pageSize) } }
}

/** 管理员：强制取消预约 */
export async function mockForceCancelReservation(id: number): Promise<BaseResponse<null>> {
  await delay()
  const item = reservationList.find((r) => r.id === id)
  if (!item) return { code: 404, msg: '预约不存在', data: null }
  item.status = 3
  item.cancelReason = '管理员强制取消'
  item.cancelTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
  return { code: 0, msg: '已强制取消', data: null }
}

/** 管理员：强制签退 */
export async function mockForceCheckout(id: number): Promise<BaseResponse<null>> {
  await delay()
  const item = reservationList.find((r) => r.id === id)
  if (!item) return { code: 404, msg: '预约不存在', data: null }
  if (item.status !== 1) return { code: 1001, msg: '当前状态不可签退', data: null }
  item.status = 2
  return { code: 0, msg: '已强制签退', data: null }
}

/** 管理员：审批预约 */
export async function mockApproveReservation(
  id: number,
  approved: boolean,
  remark?: string,
): Promise<BaseResponse<null>> {
  await delay()
  const item = reservationList.find((r) => r.id === id)
  if (!item) return { code: 404, msg: '预约不存在', data: null }
  item.approvalStatus = approved ? 3 : 4
  item.approvalRemark = remark || (approved ? '审批通过' : '审批驳回')
  return { code: 0, msg: approved ? '审批通过' : '已驳回', data: null }
}

export { reservationList }
