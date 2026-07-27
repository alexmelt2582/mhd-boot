import { format } from "date-fns";

/**
 * 格式化日期，当前默认格式方式是 "yyyy-MM-dd HH:mm:ss"
 * <p>
 * 如果传递的 datetime 为 undefined，则返回空字符串
 * @param datetime 日期
 * @param formatPattern 格式化方式
 * @returns {string} 格式化后的日期
 */
export function formatDate(datetime, formatPattern) {
  let defaultValue = "";
  if (datetime === undefined) {
    return defaultValue;
  }
  let pattern = formatPattern || "yyyy-MM-dd HH:mm:ss";
  // 假设从后端接收的时间是 ISO 8601 格式 "2025-03-06T12:34:56"
  const date = new Date(datetime);
  return format(date, pattern);
}

/**
 * 时间段问候语
 * @returns {string} 问候语
 */
export function getDayText() {
  // 获取当前时间
  let timeNow = new Date();
  // 获取当前小时
  let hours = timeNow.getHours();
  if (hours >= 6 && hours <= 10) return `早上好⛅，今天依旧是开心的一天！`;
  if (hours >= 10 && hours <= 14) return `中午好🌻，此刻吃饭、睡觉、打豆豆。`;
  if (hours >= 14 && hours <= 18) return `下午好🌞，尽情享受今天的美好时光。`;
  if (hours >= 18 && hours <= 24) return `晚上好🌇，注意早点休息哟！`;
  if (hours >= 0 && hours <= 6) return `凌晨好🌃，注意不要熬夜哟！`;
}

export function getGreeting() {
  const hour = new Date().getHours();
  if (hour < 6) {
    return "凌晨好";
  } else if (hour < 9) {
    return "早上好";
  } else if (hour < 12) {
    return "上午好";
  } else if (hour < 14) {
    return "中午好";
  } else if (hour < 17) {
    return "下午好";
  } else if (hour < 19) {
    return "傍晚好";
  } else {
    return "晚上好";
  }
}

export function timeAgo(dateString) {
  const now = new Date();
  const date = new Date(dateString);

  // 容错：传入非法日期直接返回原字符串
  if (isNaN(date.getTime())) return dateString;
  const diffMs = now.getTime() - date.getTime();
  // 未来时间或 0 秒以内，统一显示「刚刚」
  if (diffMs <= 0) return "刚刚";
  const seconds = Math.floor(diffMs / 1000);
  if (seconds <= 0) return "刚刚";
  if (seconds < 60) return `${seconds} 秒前`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)} 分钟前`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)} 小时前`;

  const days = Math.floor(seconds / 86400);
  return days === 1 ? "1 天前" : `${days} 天前`;
}
