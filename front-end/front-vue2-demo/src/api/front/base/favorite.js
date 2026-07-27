import service from "@/utils/service";

/**
 * 添加收藏
 */
export async function addFavorite(data) {
  return service({
    url: "api/front/favorite/add",
    method: "post",
    data
  });
}

/**
 * 取消收藏
 */
export async function removeFavorite(data) {
  return service({
    url: "api/front/favorite/remove",
    method: "delete",
    data
  });
}

/**
 * 分页查询我的收藏
 */
export async function getMyFavorites(params) {
  return service({
    url: "api/front/favorite/my/page",
    method: "get",
    params
  });
}

/**
 * 分页查询我的收藏
 */
export async function getMyFavoritesNews(params) {
  return service({
    url: "api/front/favorite/my/page/news",
    method: "get",
    params
  });
}

/**
 * 检查是否已收藏
 */
export async function checkIsFavorited(targetType, targetId) {
  return service({
    url: "api/front/favorite/check",
    method: "get",
    params: { targetType, targetId }
  });
}

/**
 * 批量删除收藏
 */
export async function batchRemoveFavorites(favoriteIds) {
  return service({
    url: "api/front/favorite/batchRemove",
    method: "delete",
    data: favoriteIds
  });
}

/**
 * 按类型查询收藏数量
 */
export async function getFavoriteCountByType() {
  return service({
    url: "api/front/favorite/countByType",
    method: "get"
  });
}
