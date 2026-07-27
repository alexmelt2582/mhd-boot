import service from "@/utils/service";

/**
 * 提交反馈
 */
export async function submitFeedback(data) {
  return service({
    url: "api/front/feedback/submit",
    method: "post",
    data
  });
}

/**
 * 分页查询我的反馈
 */
export async function getMyFeedbackPage(params) {
  return service({
    url: "api/front/feedback/my/page",
    method: "get",
    params
  });
}

/**
 * 查看我的反馈详情
 */
export async function getMyFeedbackDetail(id) {
  return service({
    url: "api/front/feedback/my/detail",
    method: "get",
    params: { id }
  });
}
