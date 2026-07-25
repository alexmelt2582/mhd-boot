/**
 * 前端全局配置
 */
export interface AppConfig {
  /** 网站标题 */
  title: string;
  /** 请求超时时间（ms） */
  timeout: number;
  /** 发给后端的 token 请求头名 */
  tokenKey: string;
}

/** 默认配置 */
export const appConfig: AppConfig = {
  title: '智慧图书馆空间预约系统',
  timeout: 2 * 60 * 1000,
  tokenKey: 'Authorization',
}
