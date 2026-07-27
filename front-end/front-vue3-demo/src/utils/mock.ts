// 1. 统一延迟处理
import type {BaseResponse} from "@/utils/service.ts";
import {RESPONSE_CODE} from "@/constants/status.ts";

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300));

/**
 * 2. 统一 Mock 成功响应包装器
 * @param dataOrFn - 直接的数据对象，或者返回数据的同步/异步函数（用于处理复杂的过滤逻辑）
 */
export function mockSuccess<T>(dataOrFn: T | (() => T) | (() => Promise<T>)): Promise<BaseResponse<T>> {
  return new Promise(async (resolve) => {
    await delay(); // 每次请求都先延迟

    let data: T;
    if (typeof dataOrFn === 'function') {
      // 如果传入的是函数，执行它获取真实数据
      data = await (dataOrFn as () => T | Promise<T>)();
    } else {
      // 如果传入的是直接对象，直接赋值
      data = dataOrFn;
    }

    resolve({code: RESPONSE_CODE.SUCCESS, msg: 'ok', data});
  });
}

/**
 * 3. 统一 Mock 失败响应包装器（模拟后端报错）
 */
export function mockFail<T = any>(msg: string, code: string = RESPONSE_CODE.ERROR): Promise<BaseResponse<T>> {
  return new Promise(async (resolve) => {
    await delay();
    resolve({code, msg, data: null as T});
  });
}
