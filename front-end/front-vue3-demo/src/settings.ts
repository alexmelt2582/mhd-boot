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
  /** token 在浏览器 cookie 中的 key */
  tokenCookieKey: string;
}

/** 默认配置 */
export const appConfig: AppConfig = {
  title: '系统',
  timeout: 2 * 60 * 1000, // 2 分钟
  tokenKey: 'Authorization',
  tokenCookieKey: 'me-token',
};
