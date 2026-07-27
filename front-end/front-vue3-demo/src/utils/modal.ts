import { ElNotification, ElMessage } from 'element-plus'

// 定义提示类型
type NoticeType = 'success' | 'warning' | 'info' | 'error'

// 通知配置
interface MeNoticeOptions {
  message: string
  title?: string
  duration?: number
  type?: NoticeType
  parseHtml?: boolean
  showClose?: boolean
}

// 消息配置
interface MeMsgOptions {
  message: string
  duration?: number
  type?: NoticeType
  parseHtml?: boolean
  showClose?: boolean
}

/** 基础通知封装 */
const baseNotice = (options: MeNoticeOptions) => {
  const {
    message,
    title = '温馨提示',
    duration = 2000,
    type = 'info',
    parseHtml = false,
    showClose = true,
  } = options

  ElNotification.closeAll()
  ElNotification({
    title,
    message,
    type,
    duration,
    showClose,
    dangerouslyUseHTMLString: parseHtml,
  })
}

/** 基础消息封装 */
const baseMsg = (options: MeMsgOptions) => {
  const { message, duration = 2000, type = 'info', parseHtml = false, showClose = true } = options

  ElMessage({
    message,
    type,
    duration,
    showClose,
    dangerouslyUseHTMLString: parseHtml,
  })
}

/** 封装任意提示类型通知，默认info */
export function meNotice(options: MeNoticeOptions) {
  baseNotice(options)
}

/** 成功通知 */
export function meNoticeSuccess(options: Omit<MeNoticeOptions, 'type'>) {
  baseNotice({ ...options, type: 'success' })
}

/** 错误通知 */
export function meNoticeError(options: Omit<MeNoticeOptions, 'type'>) {
  baseNotice({ ...options, type: 'error' })
}

/** 警告通知 */
export function meNoticeWarning(options: Omit<MeNoticeOptions, 'type'>) {
  baseNotice({ ...options, type: 'warning' })
}

/** 信息通知 */
export function meNoticeInfo(options: Omit<MeNoticeOptions, 'type'>) {
  baseNotice({ ...options, type: 'info' })
}

/** 封装任意提示类型消息，默认info */
export function meMsg(options: MeMsgOptions) {
  baseMsg(options)
}

/** 成功消息 */
export function meMsgSuccess(options: Omit<MeMsgOptions, 'type'>) {
  baseMsg({ ...options, type: 'success' })
}

/** 错误消息 */
export function meMsgError(options: Omit<MeMsgOptions, 'type'>) {
  baseMsg({ ...options, type: 'error' })
}

/** 警告消息 */
export function meMsgWarning(options: Omit<MeMsgOptions, 'type'>) {
  baseMsg({ ...options, type: 'warning' })
}

/** 信息消息 */
export function meMsgInfo(options: Omit<MeMsgOptions, 'type'>) {
  baseMsg({ ...options, type: 'info' })
}
