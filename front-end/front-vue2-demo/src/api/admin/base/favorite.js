import service from "@/utils/service";

/**
 * 分页查询收藏数据
 */
export async function getFavoritePage(params) {
  return service({
    url: "api/admin/favorite/page",
    method: "get",
    params
  });
}

/**
 * 获取收藏统计数据
 */
export async function getFavoriteStatistics() {
  return service({
    url: "api/admin/favorite/statistics",
    method: "get"
  });
}

/**
 * 获取热门收藏内容
 */
export async function getPopularContent(targetType, limit = 10) {
  return service({
    url: "api/admin/favorite/popular",
    method: "get",
    params: { targetType, limit }
  });
}
