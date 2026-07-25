import service from "@/utils/service";

/**
 * 发布评论
 */
export async function publishComment(data) {
  return service({
    url: "api/front/comment/publish",
    method: "post",
    data
  });
}

/**
 * 回复评论
 */
export async function replyComment(data) {
  return service({
    url: "api/front/comment/reply",
    method: "post",
    data
  });
}

/**
 * 根据类型获取总数
 */
export async function countComment(params) {
  return service({
    url: "api/front/comment/count",
    method: "get",
    params
  });
}

/**
 * 查看目标内容的一级评论列表（分页）
 */
export async function getCommentsByTarget(params) {
  return service({
    url: "api/front/comment/list",
    method: "get",
    params
  });
}

/**
 * 获取评论的回复列表（分页）
 */
export async function getReplyComments(params) {
  return service({
    url: "api/front/comment/replyList",
    method: "get",
    params
  });
}

/**
 * 切换评论点赞状态
 */
export async function toggleCommentLike(commentId) {
  return service({
    url: "api/front/comment/toggle-like",
    method: "post",
    params: { commentId }
  });
}

/**
 * 删除自己的评论
 */
export async function deleteMyComment(commentId) {
  return service({
    url: "api/front/comment/delete",
    method: "delete",
    params: { commentId }
  });
}

/**
 * 查看我的评论列表
 */
export async function getMyComments(params) {
  return service({
    url: "api/front/comment/my/page",
    method: "get",
    params
  });
}
