import service from "@/utils/service";

/**
 * 分页查询我的通知
 */
export async function getMyNotifications(params) {
  return service({
    url: "api/front/notification/my/page",
    method: "get",
    params
  });
}

/**
 * 查看我的通知详情
 */
export async function getMyNotificationDetail(id) {
  return service({
    url: "api/front/notification/my/detail",
    method: "get",
    params: { id }
  });
}

/**
 * 标记通知为已读
 */
export async function markAsRead(data) {
  return service({
    url: "api/front/notification/markRead",
    method: "post",
    data
  });
}

/**
 * 标记全部通知为已读
 */
export async function markAllAsRead() {
  return service({
    url: "api/front/notification/markAllRead",
    method: "post"
  });
}

/**
 * 删除我的通知
 */
export async function deleteMyNotifications(ids) {
  return service({
    url: "api/front/notification/delete",
    method: "delete",
    data: ids
  });
}

/**
 * 获取未读通知数量
 */
export async function getUnreadCount() {
  return service({
    url: "api/front/notification/unreadCount",
    method: "get"
  });
}
