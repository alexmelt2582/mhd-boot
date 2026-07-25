/**
 * 格式化 url 地址，处理 url 中存在多余 / 导致访问失败的问题
 * @param baseUrl 基础 url
 * @param resourcePath 资源路径
 * @returns {string} 格式化后的 url
 */
export function normalizeUrl(baseUrl, resourcePath) {
  return `${baseUrl.replace(/\/+$/, "")}/${resourcePath.replace(/^\/+/, "")}`;
}
