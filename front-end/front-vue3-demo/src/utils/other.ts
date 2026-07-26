/**
 * 安全合并对象：仅修改目标对象中已经存在的键，忽略源对象中的多余键
 * @param target - 要被修改的目标对象
 * @param source - 含有修改值的源对象
 */
export function patchObject<T extends object>(target: T, source: Partial<T>): T {
  // 遍历源对象的 keys（作为 keyof T 强转，确保类型安全）
  const keys = Object.keys(source) as (keyof T)[];
  for (const key of keys) {
    // 关键判断：仅在 target 自身拥有该属性时，才进行赋值
    if (Object.prototype.hasOwnProperty.call(target, key)) {
      target[key] = source[key] as T[keyof T];
    }
  }
  return target;
}
