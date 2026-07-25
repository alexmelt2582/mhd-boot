import type { BaseResponse, PageResponse } from '@/utils/service'
import type { MessageVO, MessageQuery } from './type'

/* ================================================================
 * 消息数据：30 条，归属于 userId=2（学生张三）
 *   类型分布：
 *     - SYSTEM(系统公告):      6 条
 *     - RESERVATION(预约相关): 12 条
 *     - CREDIT(信用变更):      7 条
 *     - VIOLATION(违规提醒):   5 条
 *   已读/未读混合
 * ================================================================ */

function msg(
  id: number,
  title: string,
  content: string,
  messageType: string,
  relatedId: number | null,
  isRead: number,
  sendTime: string,
): MessageVO {
  return {
    id,
    userId: 2,
    title,
    content,
    messageType: messageType as any,
    relatedId,
    isRead,
    sendTime,
    createTime: sendTime,
  }
}

const messageList: MessageVO[] = [
  // ==================== RESERVATION 预约相关 (12条) ====================
  msg(1, '座位预约成功',
    '您已成功预约 2026-07-25 09:00-12:00 的座位「C区-03号座」（安静学习区 2F），预约码：RC20260725003，请按时签到。',
    'RESERVATION', 1001, 1, '2026-07-24 22:30:00'),
  msg(2, '研讨室预约成功',
    '您已成功预约 2026-07-26 14:00-17:00 的研讨室「研讨室-201」（2F），预约码：RC20260726201，请按时到达。',
    'RESERVATION', 1002, 1, '2026-07-25 08:15:00'),
  msg(3, '预约即将开始提醒',
    '您的座位预约（C区-03号座）将于15分钟后开始，请及时前往签到，预约码：RC20260725003。',
    'RESERVATION', 1001, 1, '2026-07-25 08:45:00'),
  msg(4, '预约签到成功',
    '您已成功签到座位「C区-03号座」，使用时间 09:00-12:00，祝您学习愉快。',
    'RESERVATION', 1001, 1, '2026-07-25 09:02:00'),
  msg(5, '预约即将到期提醒',
    '您的座位预约（C区-03号座）将于30分钟后到期，如需续时请在到期前操作。',
    'RESERVATION', 1001, 1, '2026-07-25 11:30:00'),
  msg(6, '座位预约成功',
    '您已成功预约 2026-07-25 14:00-18:00 的座位「A区-08号座」（安静学习区 1F），预约码：RC20260725008，请按时签到。',
    'RESERVATION', 1003, 1, '2026-07-25 10:00:00'),
  msg(7, '研讨室预约即将开始提醒',
    '您的研讨室预约（研讨室-201）将于15分钟后开始，请及时到达2F研讨室区域，预约码：RC20260726201。',
    'RESERVATION', 1002, 0, '2026-07-26 13:45:00'),
  msg(8, '座位预约取消通知',
    '您的座位预约（B区-12号座，2026-07-24 10:00-12:00）已成功取消，预约码：RC20260724012。',
    'RESERVATION', 1004, 1, '2026-07-23 23:50:00'),
  msg(9, '续时预约成功',
    '您已将座位「C区-03号座」使用时间续时至 13:00，新的预约码：RC20260725003E。',
    'RESERVATION', 1005, 1, '2026-07-25 11:50:00'),
  msg(10, '座位预约成功',
    '您已成功预约 2026-07-27 08:00-12:00 的座位「D区-05号座」（电子阅览区 2F），预约码：RC20260727005，请按时签到。',
    'RESERVATION', 1006, 0, '2026-07-26 21:00:00'),
  msg(11, '预约签到提醒',
    '您的座位预约（D区-05号座）即将开始，请于15分钟内签到，否则将自动取消并扣除信用分。',
    'RESERVATION', 1006, 0, '2026-07-27 07:45:00'),
  msg(12, '预约未签到通知',
    '您预约的座位（E区-15号座，2026-07-20 09:00-12:00）未在规定时间内签到，预约已自动取消。',
    'RESERVATION', 1007, 1, '2026-07-20 09:20:00'),

  // ==================== CREDIT 信用变更 (7条) ====================
  msg(13, '信用分减少通知',
    '由于您未在规定时间内签到（预约码：RC20260720015），信用分扣除2分，当前信用分：85分。',
    'CREDIT', 1007, 1, '2026-07-20 09:25:00'),
  msg(14, '信用分恢复通知',
    '恭喜您连续7天正常使用图书馆座位，信用分恢复1分，当前信用分：86分。',
    'CREDIT', null, 1, '2026-07-22 06:00:00'),
  msg(15, '信用分减少通知',
    '由于您在座位上遗留物品未清理，信用分扣除1分，当前信用分：85分。',
    'CREDIT', null, 1, '2026-07-18 18:30:00'),
  msg(16, '信用分预警',
    '您的信用分已降至85分，低于90分将无法预约热门时段座位，请注意维护信用记录。',
    'CREDIT', null, 1, '2026-07-18 18:31:00'),
  msg(17, '信用分恢复通知',
    '恭喜您连续14天无违规记录，信用分恢复2分，当前信用分：87分。',
    'CREDIT', null, 1, '2026-07-24 06:00:00'),
  msg(18, '信用分减少通知',
    '由于您在研讨室内大声喧哗影响他人，信用分扣除3分，当前信用分：84分。',
    'CREDIT', 1002, 0, '2026-07-26 17:10:00'),
  msg(19, '信用分恢复通知',
    '您参与的图书馆志愿活动获得信用分奖励1分，当前信用分：85分。',
    'CREDIT', null, 0, '2026-07-25 09:00:00'),

  // ==================== VIOLATION 违规提醒 (5条) ====================
  msg(20, '违规行为警告',
    '您在研讨室-201内声音过大，已被管理员提醒，请保持安静以免影响他人。再次违规将扣除信用分。',
    'VIOLATION', 1002, 0, '2026-07-26 16:50:00'),
  msg(21, '违规处理通知',
    '您在图书馆内携带食品入馆，违反图书馆使用规定第3条，信用分扣除2分。请自觉遵守馆内秩序。',
    'VIOLATION', null, 1, '2026-07-15 14:20:00'),
  msg(22, '违规处理通知',
    '您将座位转让他人使用，违反座位使用规定，信用分扣除5分，暂停预约权限3天。',
    'VIOLATION', null, 1, '2026-07-10 10:00:00'),
  msg(23, '违规行为提醒',
    '检测到您的预约账号在短时间内频繁取消预约（本周已取消3次），请注意合理预约，避免影响信用评级。',
    'VIOLATION', null, 1, '2026-07-23 15:00:00'),
  msg(24, '违规处理通知',
    '您在图书馆闭馆后未及时离开座位区域，违反管理规定，信用分扣除1分。',
    'VIOLATION', null, 0, '2026-07-25 22:15:00'),

  // ==================== SYSTEM 系统公告 (6条) ====================
  msg(25, '系统维护通知',
    '图书馆预约系统将于 2026-07-28 凌晨02:00-06:00 进行维护升级，届时预约功能暂停使用，请提前安排好预约时间。给您带来不便，敬请谅解。',
    'SYSTEM', null, 0, '2026-07-25 08:00:00'),
  msg(26, '暑期开放时间调整公告',
    '亲爱的读者：2026年暑期（7月30日-9月1日）图书馆开放时间调整为每日08:00-21:00，部分区域关闭。详情请查看图书馆官网。',
    'SYSTEM', null, 0, '2026-07-25 08:00:00'),
  msg(27, '新学期预约规则更新',
    '自2026年9月1日起，每位学生每日最多可预约2个时段（每时段不超过4小时），教师不限。研讨室预约需至少2人以上方可使用。',
    'SYSTEM', null, 1, '2026-07-20 09:00:00'),
  msg(28, '图书馆新设备上线通知',
    '三楼电子阅览区新增20台高性能工作站，即日起开放预约使用。配置：i7-12700/32GB/1TB SSD/27寸4K显示器。',
    'SYSTEM', null, 1, '2026-07-15 10:00:00'),
  msg(29, '文明使用图书馆倡议书',
    '为营造良好的学习环境，图书馆倡议：1）保持安静，手机调至静音模式；2）离开时带走随身物品；3）按预约时间使用座位，不占座不转借；4）爱护馆内公共设施。感谢您的配合！',
    'SYSTEM', null, 1, '2026-07-01 08:00:00'),
  msg(30, '图书馆志愿者招募通知',
    '新学期图书馆志愿者招募开始啦！招募对象：全体在校学生。工作时间灵活，可获取信用分奖励及志愿服务证明。报名截止：2026-08-15。报名链接见图书馆官网。',
    'SYSTEM', null, 0, '2026-07-25 08:30:00'),
]

let messageListInternal = [...messageList]

export { messageListInternal as messageList }

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

/* ================================================================
 * Mock API 函数
 * ================================================================ */

/** 获取我的消息列表（分页 + 筛选） */
export async function mockGetMyMessages(query: MessageQuery): Promise<BaseResponse<PageResponse<MessageVO>>> {
  await delay()
  let filtered = [...messageListInternal].sort(
    (a, b) => new Date(b.sendTime).getTime() - new Date(a.sendTime).getTime(),
  )

  if (query.messageType) {
    filtered = filtered.filter((m) => m.messageType === query.messageType)
  }
  if (query.isRead !== undefined && query.isRead !== null) {
    filtered = filtered.filter((m) => m.isRead === query.isRead)
  }

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return { code: 0, msg: 'ok', data: { total: filtered.length, list } }
}

/** 获取未读消息数量 */
export async function mockGetUnreadCount(): Promise<BaseResponse<number>> {
  await delay()
  const count = messageListInternal.filter((m) => m.isRead === 0).length
  return { code: 0, msg: 'ok', data: count }
}

/** 标记单条消息为已读 */
export async function mockMarkAsRead(messageId: number): Promise<BaseResponse<null>> {
  await delay()
  const msg = messageListInternal.find((m) => m.id === messageId)
  if (!msg) return { code: 404, msg: '消息不存在', data: null }
  msg.isRead = 1
  return { code: 0, msg: '已标记为已读', data: null }
}

/** 全部标记为已读 */
export async function mockMarkAllAsRead(): Promise<BaseResponse<null>> {
  await delay()
  messageListInternal.forEach((m) => {
    m.isRead = 1
  })
  return { code: 0, msg: '已全部标记为已读', data: null }
}

/** 删除消息 */
export async function mockDeleteMessage(messageId: number): Promise<BaseResponse<null>> {
  await delay()
  const idx = messageListInternal.findIndex((m) => m.id === messageId)
  if (idx === -1) return { code: 404, msg: '消息不存在', data: null }
  messageListInternal.splice(idx, 1)
  return { code: 0, msg: '删除成功', data: null }
}

/** 根据ID获取消息详情 */
export async function mockGetMessageById(messageId: number): Promise<BaseResponse<MessageVO>> {
  await delay()
  const msg = messageListInternal.find((m) => m.id === messageId)
  if (!msg) return { code: 404, msg: '消息不存在', data: null as any }
  return { code: 0, msg: 'ok', data: { ...msg } }
}
