module.exports = {
  /**
   * @description 网站标题
   */
  title: "系统",
  /**
   * @description 端口号
   */
  port: 8080,
  /**
   * @description 请求超时时间，毫秒
   */
  timeout: 2 * 60 * 1000,
  /**
   * @description token 传递时的请求头名称
   */
  tokenKey: "Authorization",
  /**
   * @description token 保存浏览器的 key
   */
  tokenCookieKey: "me-token"
};
