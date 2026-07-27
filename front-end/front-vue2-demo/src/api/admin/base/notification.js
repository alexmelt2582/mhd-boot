import service from "@/utils/service";

/**
 * 分页查询通知列表
 */
export async function getNotificationPage(params) {
  return service({
    url: "api/admin/notification/page",
    method: "get",
    params
  });
}

/**
 * 查询单个通知详情
 */
export async function getNotificationDetail(id) {
  return service({
    url: "api/admin/notification/get",
    method: "get",
    params: { id }
  });
}

/**
 * 发送通知（群发）
 */
export async function sendNotification(data) {
  return service({
    url: "api/admin/notification/send",
    method: "post",
    data
  });
}

/**
 * 批量删除通知
 */
export async function deleteNotification(ids) {
  return service({
    url: "api/admin/notification/delete",
    method: "delete",
    data: ids
  });
}
