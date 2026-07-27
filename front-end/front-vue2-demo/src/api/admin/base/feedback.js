import service from "@/utils/service";

/**
 * 分页查询反馈列表
 */
export async function getFeedbackPage(params) {
  return service({
    url: "api/admin/feedback/page",
    method: "get",
    params
  });
}

/**
 * 查询单个反馈详情
 */
export async function getFeedbackDetail(id) {
  return service({
    url: "api/admin/feedback/get",
    method: "get",
    params: { id }
  });
}

/**
 * 更新反馈处理状态
 */
export async function updateFeedbackStatus(data) {
  return service({
    url: "api/admin/feedback/updateStatus",
    method: "post",
    data
  });
}

/**
 * 批量删除反馈
 */
export async function deleteFeedback(ids) {
  return service({
    url: "api/admin/feedback/delete",
    method: "delete",
    data: ids
  });
}

/**
 * 回复反馈
 */
export async function replyFeedback(data) {
  return service({
    url: "api/admin/feedback/reply",
    method: "post",
    data
  });
}
