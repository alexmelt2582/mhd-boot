/**
 * 格式化日期为「多久前」的人性化显示
 */
export const formatTimeAgo = (dateStr: string | number): string => {
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) {
    return String(dateStr)
  }
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  if (diffMs <= 0) return '刚刚'
  const seconds = Math.floor(diffMs / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  if (seconds < 60) return `${seconds} 秒前`
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days === 1) return '昨天'
  if (days < 7) return `${days} 天前`
  if (days < 30) return `${Math.floor(days / 7)} 周前`
  if (days < 365) return `${Math.floor(days / 30)} 月前`
  return date.toLocaleDateString('zh-CN')
}

/**
 * 格式化日期为 YYYY-MM-DD
 */
export const formatDate = (date: string | number | Date, fmt = 'YYYY-MM-DD'): string => {
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return fmt
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期时间为 YYYY-MM-DD HH:mm:ss
 */
export const formatDateTime = (date: string | number | Date): string => {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

/**
 * 分钟数转人性化时长
 */
export const formatDuration = (minutes: number): string => {
  if (minutes < 60) return `${minutes} 分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours} 小时 ${mins} 分钟` : `${hours} 小时`
}

/**
 * 计算两个时间相差的分钟数
 */
export const diffMinutes = (date1: string | Date, date2: string | Date): number => {
  const d1 = new Date(date1).getTime()
  const d2 = new Date(date2).getTime()
  return Math.floor(Math.abs(d1 - d2) / (1000 * 60))
}
