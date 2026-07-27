// 1. 统一延迟处理
import type {BaseResponse} from "@/utils/service.ts";
import {RESPONSE_CODE} from "@/constants/status.ts";

// 1. 统一延迟处理
const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300));

/**
 * 2. 统一 Mock 成功响应包装器
 * @param dataOrFn - 直接的数据对象，或者返回数据的同步/异步函数
 * @param msg - 可选的成功消息，默认为 'ok'
 */
export function mockSuccess<T>(
  dataOrFn: T | (() => T) | (() => Promise<T>),
  msg: string = 'ok' // 增加可选的 msg 参数
): Promise<BaseResponse<T>> {
  return new Promise(async (resolve) => {
    await delay();
    let data: T;
    if (typeof dataOrFn === 'function') {
      data = await (dataOrFn as () => T | Promise<T>)();
    } else {
      data = dataOrFn;
    }
    // 使用传入的 msg 参数
    resolve({ code: RESPONSE_CODE.SUCCESS, msg, data });
  });
}

/**
 * 3. 统一 Mock 失败响应包装器
 * @param msg - 错误消息
 * @param code - 可选的错误码，默认为 RESPONSE_CODE.ERROR
 */
export function mockFail<T = any>(
  msg: string,
  code: string = RESPONSE_CODE.ERROR
): Promise<BaseResponse<T>> {
  return new Promise(async (resolve) => {
    await delay();
    // 这里也使用传入的 msg 参数
    resolve({ code, msg, data: null as T });
  });
}

/**
 * 通用分页函数
 * @param list 需要分页的完整数组
 * @param pageNum 当前页码
 * @param pageSize 每页数量
 * @returns 分页后的数据对象
 */
export function paginate<T>(list: T[], pageNum?: number, pageSize?: number) {
  const pageNumFinal = pageNum || 1;
  const pageSizeFinal = pageSize || 10;
  const start = (pageNumFinal - 1) * pageSizeFinal;
  const end = start + pageSizeFinal;
  return {
    list: list.slice(start, end),
    total: list.length
  };
}

/**
 * 通用过滤函数
 * @param list 需要过滤的数组
 * @param query 查询条件对象
 * @param fieldMap 字段映射关系，key是查询参数的键，value是数据对象的键
 * @returns 过滤后的数组
 */
export function filterList<T extends Record<string, any>>(
  list: T[],
  query: Record<string, any>,
  fieldMap: Record<string, string>
) {
  return list.filter((item) => {
    return Object.keys(fieldMap).every((queryKey) => {
      const queryValue = query[queryKey];

      // 1. 更严谨的空值判断（包含去除首尾空格后的判断）
      if (queryValue === undefined || queryValue === null || String(queryValue).trim() === '') {
        return true; // 跳过此项过滤
      }
      const itemKey = fieldMap[queryKey];
      if (!itemKey) {
        return true;
      }
      // 2. 安全地获取 item 的值，防止 TS 报错
      const itemValue = item?.[itemKey];
      // 3. 字符串模糊匹配（忽略大小写）
      if (typeof itemValue === 'string') {
        return itemValue.toLowerCase().includes(String(queryValue).toLowerCase());
      }
      // 4. 其他类型（如数字、布尔值）进行严格相等匹配
      return itemValue === queryValue;
    });
  });
}
