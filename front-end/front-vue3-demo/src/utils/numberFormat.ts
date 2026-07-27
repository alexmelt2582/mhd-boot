/**
 * 数字千分位简化格式化
 * @param num 待格式化的数字（支持数字/字符串类型，非数字则返回0）
 * @param decimalDigits 保留小数位数，默认1位
 * @returns 格式化后的字符串（如 0、1k、1.2k、10.5w 等）
 */
export const formatNumberToK = (
  num: number | string | undefined | null,
  decimalDigits: number = 1
): string => {
  // 1. 处理空值/非数字：返回0
  const parsedNum = Number(num);
  if (isNaN(parsedNum)) {
    return '0';
  }
  // 2. 小于1000：直接返回原数字（避免 999 变成 0.9k）
  if (parsedNum < 1000) {
    return parsedNum.toString();
  }
  // 3. 千位及以上：按单位简化（k/万/亿，可扩展）
  const units: Array<{ value: number; symbol: string }> = [
    { value: 1000, symbol: 'k' },
    { value: 10000, symbol: 'w' },
    { value: 100000000, symbol: '亿' }
  ];
  // 从大到小匹配单位（修复 TS 类型提示问题）
  for (let i = units.length - 1; i >= 0; i--) {
    // 方式1：非空断言（简单直接，适合确定索引安全的场景）
    // const unit = units[i]!; // 加 ! 告诉 TS 该值一定非空
    // 方式2：可选链 + 提前退出（更严谨，推荐）
    const unit = units[i]
    if (!unit) continue
    if (parsedNum >= unit.value) {
      // 计算简化后数值，保留指定小数位并四舍五入
      const simplified = (parsedNum / unit.value).toFixed(decimalDigits);
      // 去除末尾的0和小数点（如 1.0k → 1k，1.20k → 1.2k）
      const formatted = parseFloat(simplified).toString();
      return `${formatted}${unit.symbol}`;
    }
  }
  // 兜底：若未匹配到任何单位（如 999 已提前返回，这里理论上不会执行）
  return parsedNum.toString();
};
/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}