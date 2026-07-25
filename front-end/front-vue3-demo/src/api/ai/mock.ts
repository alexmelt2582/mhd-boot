import type { BaseResponse, PageResponse } from '@/utils/service'
import type { AIConversationVO, AIConversationQuery } from './type'

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

const qaList: AIConversationVO[] = [
  { id: 1,  userId: 2, userName: '张三', question: '图书馆周末开放时间是什么？', answer: '图书馆周末开放时间为上午8:00至晚上22:00，与工作日一致。节假日安排请关注图书馆公告。', convType: 'QA', isFavorite: 1, isUseful: 1, createTime: '2026-07-25 21:30:00' },
  { id: 2,  userId: 2, userName: '张三', question: '如何预约自习座位？', answer: '打开图书馆微信公众号或APP，进入"空间预约"模块，选择日期、区域和座位号，确认后即可完成预约。每人每天最多预约2次，单次最长为4小时。', convType: 'QA', isFavorite: 1, isUseful: 1, createTime: '2026-07-25 20:15:00' },
  { id: 3,  userId: 4, userName: '李四', question: '预约后可以取消吗？', answer: '可以取消。在预约开始时间前30分钟可自由取消，不会影响信用分。如果在开始时间后30分钟内未签到，系统将自动取消并扣减信用分2分。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-25 19:00:00' },
  { id: 4,  userId: 4, userName: '李四', question: '图书馆有免费的WiFi吗？', answer: '有的。图书馆全区域覆盖免费WiFi，SSID为"Library-WiFi"，连接后输入学工号和统一身份认证密码即可上网。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-25 18:45:00' },
  { id: 5,  userId: 5, userName: '王五', question: '信用积分是怎么计算的？', answer: '初始信用分为100分。正常签到每次+1分，未签到-2分，迟到-1分，破坏公物-10分，代他人签到或被代签双方各-5分。每年1月1日重置年度统计。若信用分低于60分将被限制预约。', convType: 'QA', isFavorite: 1, isUseful: 1, createTime: '2026-07-25 17:20:00' },
  { id: 6,  userId: 5, userName: '王五', question: '研讨室怎么预约？', answer: '研讨室至少需要2人才能预约，通过"空间预约"选择"研讨室"类型即可。每次最长可使用4小时。预约成功后系统自动生成开门密码，在预约时间前后5分钟内有效。', convType: 'QA', isFavorite: 1, isUseful: 1, createTime: '2026-07-25 16:30:00' },
  { id: 7,  userId: 2, userName: '张三', question: '忘记签到怎么办？', answer: '签到窗口在预约开始时间前后15分钟内开放（即开始前15分钟至开始后15分钟）。若超时未签到，系统将自动取消预约，并扣除信用分2分。建议开启消息提醒功能。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-25 15:00:00' },
  { id: 8,  userId: 6, userName: '赵六', question: '图书馆可以借书吗？', answer: '目前本系统主要面向空间管理和预约服务。借书服务请前往总馆二楼借还台或使用图书馆借阅APP办理。电子资源可通过图书馆官网访问。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-25 14:30:00' },
  { id: 9,  userId: 6, userName: '赵六', question: '电子阅览区的电脑怎么使用？', answer: '电子阅览区配备了联网计算机，使用学工号登录即可。首次使用需在电子阅览区入口刷卡激活。每台电脑配有办公软件和数据库访问权限，适合文献查阅和论文写作。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-25 14:00:00' },
  { id: 10, userId: 2, userName: '张三', question: '可以随身携带食物进馆吗？', answer: '图书馆规定禁止携带任何食物进入馆内。可以在馆外休息区饮食。馆内每个楼层均设有饮水机，可携带密封水杯入馆。', convType: 'QA', isFavorite: 0, isUseful: 0, createTime: '2026-07-25 13:20:00' },
  { id: 11, userId: 4, userName: '李四', question: '黑名单是怎么触发的？', answer: '以下行为会被加入黑名单：1）连续3次未签到；2）累计5次违规；3）严重破坏公物行为。黑名单期限为7-30天不等，期间无法预约任何空间。', convType: 'QA', isFavorite: 1, isUseful: 1, createTime: '2026-07-25 12:00:00' },
  { id: 12, userId: 7, userName: '孙七', question: '如何查看统计报表？', answer: '管理员登录后台后可查看各类统计报表：签到率、空间使用率、用户活跃度等。报表支持按日期、区域等维度筛选，并支持导出为Excel格式。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-25 11:30:00' },
  { id: 13, userId: 7, userName: '孙七', question: '有没有推荐功能？', answer: '有的！在首页和预约页面，系统会根据你的历史使用偏好和当前时段的热门区域，智能推荐合适的座位或研讨室。你也可以说"给我推荐一个安静的学习座位"。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-25 10:45:00' },
  { id: 14, userId: 2, userName: '张三', question: '多媒体区有什么设备？', answer: '多媒体区配备投影仪、音响系统、视频会议设备。适合小组演示、视频播放等需求。使用多媒体设备需提前30分钟到达进行设备调试。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-25 10:00:00' },
  { id: 15, userId: 5, userName: '王五', question: '可以带校外朋友进来吗？', answer: '校外人员需在入口处登记并抵押有效证件（身份证、驾驶证等），每天限额50人。校外人员不能预约座位，只能在指定的访客阅读区就座。', convType: 'QA', isFavorite: 0, isUseful: 0, createTime: '2026-07-25 09:30:00' },
  { id: 16, userId: 2, userName: '张三', question: '预约的座位被别人占了怎么办？', answer: '遇到座位被占情况，你可以：1）出示预约记录礼貌提醒对方；2）联系现场工作人员协助处理；3）在APP中点击"座位纠纷"进行在线申诉。工作人员会在10分钟内到场处理。', convType: 'QA', isFavorite: 1, isUseful: 1, createTime: '2026-07-25 08:45:00' },
  { id: 17, userId: 6, userName: '赵六', question: '研究生和本科生待遇一样吗？', answer: '基本一致。研究生每天最多可预约3次（本科生2次），单次最长6小时（本科生4小时）。博士生导师可预约专用研究室。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-25 08:00:00' },
  { id: 18, userId: 4, userName: '李四', question: '图书馆打印怎么收费？', answer: '图书馆各楼层均设有自助打印机。A4黑白单面0.1元/张，双面0.18元/张；A4彩色单面0.5元/张，双面1.0元/张。通过校园卡刷卡支付。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-24 21:00:00' },
  { id: 19, userId: 5, userName: '王五', question: '签到签退有时间限制吗？', answer: '签到时间：预约开始前15分钟至开始后15分钟（共30分钟窗口）。签退时间：预约结束前15分钟至结束后15分钟。提前签退不扣分，未签退扣1分。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-24 19:30:00' },
  { id: 20, userId: 2, userName: '张三', question: '图书馆空调几点开？', answer: '夏季（5月-10月）空调开放时间为开馆前30分钟至闭馆前1小时；冬季（11月-次年4月）供暖时间相同。部分区域（如特殊藏书区）为24小时恒温控制。如有温度异常可向服务台反馈。', convType: 'QA', isFavorite: 0, isUseful: 0, createTime: '2026-07-24 18:00:00' },
  { id: 21, userId: 4, userName: '李四', question: '怎么找回密码？', answer: '在登录页面点击"忘记密码"，输入学工号和注册手机号/邮箱，系统会发送验证码。验证通过后可设置新密码。如仍无法找回，请联系管理员处理。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-24 16:30:00' },
  { id: 22, userId: 6, userName: '赵六', question: '推荐一个适合小组讨论的空间', answer: '推荐"研讨区"的研讨室，可容纳6-12人，配备投影仪和白板。目前302和401研讨室设备最齐全（投影仪+视频会议）。建议提前1天预约，热门时段很抢手哦！', convType: 'RECOMMEND', isFavorite: 1, isUseful: 1, createTime: '2026-07-24 15:00:00' },
  { id: 23, userId: 2, userName: '张三', question: '推荐一个安静的座位', answer: '推荐"安静学习区"A区的单人座位，采光好、远离走廊。1F和3F的人流量相对较小，靠近窗户的位置（如A-03、A-07号座）最受欢迎。当前时段3F空位较多，建议选择。', convType: 'RECOMMEND', isFavorite: 0, isUseful: 1, createTime: '2026-07-24 14:20:00' },
  { id: 24, userId: 5, userName: '王五', question: '论文写作需要长时间使用电脑，推荐哪里？', answer: '推荐"电子阅览区"C区座位，配备高配台式机、双显示器和正版Office。每个座位有独立电源和网线接口。C-05和C-08靠墙位置最安静，适合长时间论文写作。', convType: 'RECOMMEND', isFavorite: 1, isUseful: 1, createTime: '2026-07-24 13:00:00' },
  { id: 25, userId: 7, userName: '孙七', question: '考研冲刺阶段推荐哪里学习？', answer: '首选"安静学习区"4F（顶层人少安静），其次是2F靠近东侧窗户区域。4F的A-01至A-10号座最安静。建议预约早晨8点开始的时段，避开下午高峰期。另外"休闲阅读区"的沙发座位适合轻度阅读。', convType: 'RECOMMEND', isFavorite: 1, isUseful: 1, createTime: '2026-07-24 11:30:00' },
  { id: 26, userId: 4, userName: '李四', question: '有没有适合录课的空间？', answer: '推荐"多媒体区"的录播研讨室（ROOM-404），配备隔音墙、专业麦克风和绿幕背景。需要至少提前2天预约。该室也配备了视频会议设备，适合在线答辩和远程面试。', convType: 'RECOMMEND', isFavorite: 1, isUseful: 1, createTime: '2026-07-24 10:00:00' },
  { id: 27, userId: 2, userName: '张三', question: '周末和朋友想一起自习，有连座吗？', answer: '安静学习区暂不支持连座预约，但你可以和你的朋友分别预约相邻的座位（如A-05和A-06）。或者选择"研讨区"的二人研讨室（ROOM-201和202），适合两人安静学习交流。', convType: 'RECOMMEND', isFavorite: 0, isUseful: 1, createTime: '2026-07-24 09:00:00' },
  { id: 28, userId: 6, userName: '赵六', question: '今天下午有空的研讨室吗？', answer: '我来帮你看看。目前下午时段可预约的研讨室：2F的202室（14:00-18:00空闲）、3F的301室（15:00-17:00空闲）、302室（16:00-18:00空闲）。建议尽快预约，下午研讨室普遍比较紧张。', convType: 'RECOMMEND', isFavorite: 1, isUseful: 1, createTime: '2026-07-24 08:00:00' },
  { id: 29, userId: 5, userName: '王五', question: '离校前需要提前多久预约图书馆位置？', answer: '热门时段（上午9-11点、下午2-4点）建议提前1天预约；非热门时段当天预约一般没问题。考试周期间建议每天凌晨开放预约时（0:00）就下手，好位置很快就被抢光了。', convType: 'QA', isFavorite: 1, isUseful: 1, createTime: '2026-07-23 21:00:00' },
  { id: 30, userId: 2, userName: '张三', question: '文明使用图书馆的规则有哪些？', answer: '主要规则：1）保持安静，手机调至静音；2）禁止占座，离开超过30分钟视为放弃；3）禁止损坏公物和乱涂乱画；4）离开时带走个人物品；5）禁止在馆内吸烟、饮食（水除外）；6）请勿在书架间追逐打闹。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-23 19:00:00' },
  { id: 31, userId: 4, userName: '李四', question: '预约记录在哪里查看？', answer: '在APP首页点击"我的预约"即可查看所有预约记录，包括待签到、进行中、已完成和已取消的预约。同时可以在"消息中心"查看预约相关的系统通知。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-23 17:00:00' },
  { id: 32, userId: 7, userName: '孙七', question: '信用分能恢复吗？', answer: '可以。每次正常完成预约并签退可获得+1信用分。系统也在特定节假日或活动期发放信用分奖励。信用分低于60的恢复期通常为2-4周（如果此期间内保持良好记录的话）。', convType: 'QA', isFavorite: 1, isUseful: 1, createTime: '2026-07-23 15:30:00' },
  { id: 33, userId: 2, userName: '张三', question: '移动端和网页端数据同步吗？', answer: '是的，数据完全同步。您可以在手机端预约，在网页端查看记录，或者反过来。签到签退只能通过手机端扫码完成。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-23 14:00:00' },
  { id: 34, userId: 6, userName: '赵六', question: '身份证丢了能用其他证件入馆吗？', answer: '校内师生使用校园卡或学工号二维码即可入馆，无需身份证。如果校园卡丢失，可凭学工号和密码在自助闸机处扫码入馆，同时尽快到信息中心补办校园卡。', convType: 'QA', isFavorite: 0, isUseful: 0, createTime: '2026-07-23 12:00:00' },
  { id: 35, userId: 5, userName: '王五', question: '电阅区的专业软件有哪些？', answer: '电子阅览区预装有：MATLAB、SPSS、Stata、Python（Anaconda）、R Studio、AutoCAD、Adobe系列（PS/AI/ID）等。如有特殊软件需求可联系电子阅览区管理员协助安装。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-23 10:30:00' },
  { id: 36, userId: 2, userName: '张三', question: '有没有充电插座不够用的情况？', answer: '安静学习区和电子阅览区每个座位都配备了独立电源插座和USB充电口。其他区域（休闲区）70%的座位有电源。如果发现插座故障可在线报修。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-23 09:00:00' },
  { id: 37, userId: 4, userName: '李四', question: '失物招领处在哪里？', answer: '失物招领处位于图书馆一楼服务台旁。每天18:00工作人员会整理当日的遗失物品集中存放。贵重物品请尽快认领，超过30天未认领的物品将按相关规定处理。', convType: 'QA', isFavorite: 1, isUseful: 1, createTime: '2026-07-22 20:00:00' },
  { id: 38, userId: 8, userName: '陈教授', question: '老师有专门的阅览室吗？', answer: '有的。图书馆3F设有教师专用阅览室，需刷卡进入。室内配备高端电脑、打印扫描一体机和专业数据库终端。另外各学院教授可申请长期研究室用于科研工作。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-22 16:00:00' },
  { id: 39, userId: 8, userName: '陈教授', question: '可以帮学生批量预约座位吗？', answer: '批量预约功能暂未开放。建议让学生自行预约。如果是课程需要（如辅导课、讨论班），可以联系图书馆办公室提前预留研讨室，需提供课程安排和人数。', convType: 'QA', isFavorite: 0, isUseful: 1, createTime: '2026-07-22 14:00:00' },
  { id: 40, userId: 2, userName: '张三', question: '我对AI助手的回答不满意怎么办？', answer: '非常抱歉给您带来了不好的体验！您可以在对话记录中对回答进行"有用/无用"评价。我们会根据您的反馈持续优化AI助手的回答质量。您也可以转接人工客服获取更准确的帮助。', convType: 'QA', isFavorite: 0, isUseful: 0, createTime: '2026-07-22 11:00:00' },
]

export async function mockGetConversations(query: AIConversationQuery): Promise<BaseResponse<PageResponse<AIConversationVO>>> {
  await delay()
  let filtered = [...qaList]

  if (query.userId !== undefined) {
    filtered = filtered.filter((c) => c.userId === query.userId)
  }
  if (query.convType) {
    filtered = filtered.filter((c) => c.convType === query.convType)
  }
  if (query.startTime) {
    filtered = filtered.filter((c) => c.createTime >= query.startTime!)
  }
  if (query.endTime) {
    filtered = filtered.filter((c) => c.createTime <= query.endTime!)
  }

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return { code: 0, msg: 'ok', data: { total: filtered.length, list } }
}

export async function mockDeleteConversation(id: number): Promise<BaseResponse<null>> {
  await delay()
  const idx = qaList.findIndex((c) => c.id === id)
  if (idx === -1) return { code: 404, msg: '对话记录不存在', data: null }
  qaList.splice(idx, 1)
  return { code: 0, msg: '删除成功', data: null }
}

export { qaList }
