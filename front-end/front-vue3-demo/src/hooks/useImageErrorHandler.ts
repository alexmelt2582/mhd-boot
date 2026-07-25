// src/hooks/useImageErrorHandler.ts
export function useImageErrorHandler() {
  // 仅对「非跨域/本地图片」尝试转换 Base64，HTTP 链接直接使用
  const attachImageUrlError = async (event: Event, fallbackUrl: string) => {
    const target = event.target as HTMLImageElement
    // 核心优化：Base64 格式才转换，HTTP/HTTPS 直接使用
    if (fallbackUrl.startsWith('data:')) {
      target.src = fallbackUrl
    } else {
      // 优先直接使用 HTTP 链接，避免跨域报错
      target.src = fallbackUrl
    }
  }
  return { attachImageUrlError }
}
