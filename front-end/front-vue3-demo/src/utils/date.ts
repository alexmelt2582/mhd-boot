/**
 * 格式化日期为「多久前」的人性化显示
 * @param dateStr 日期字符串/时间戳
 * @returns 人性化时间（如：刚刚、5分钟前、昨天、2025/1/1）
 */
export const formatTimeAgo = (dateStr: string | number): string => {
  // 容错处理：非法日期直接返回原字符串/时间戳的字符串形式
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) {
    return String(dateStr);
  }
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  // 未来时间/0秒内 → 显示「刚刚」
  if (diffMs <= 0) return "刚刚";
  const seconds = Math.floor(diffMs / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  // 秒级
  if (seconds < 60) return `${seconds} 秒前`;
  // 分钟级
  if (minutes < 60) return `${minutes} 分钟前`;
  // 小时级
  if (hours < 24) return `${hours} 小时前`;
  // 天级
  if (days === 1) return "昨天";
  if (days === 0) return "今天"; // 理论上不会走到这里，兜底
  if (days < 7) return `${days} 天前`;
  // 周级
  if (days < 30) return `${Math.floor(days / 7)} 周前`;
  // 月级
  if (days < 365) return `${Math.floor(days / 30)} 月前`;
  // 超过1年 → 显示具体日期（格式：2025/1/18）
  return date.toLocaleDateString('zh-CN');
};
