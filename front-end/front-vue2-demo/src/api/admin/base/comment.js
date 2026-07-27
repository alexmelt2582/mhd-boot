import service from "@/utils/service";

/**
 * 分页查询评论列表
 */
export async function getCommentPage(params) {
  return service({
    url: "api/admin/comment/page",
    method: "get",
    params
  });
}

/**
 * 查询单个评论详情
 */
export async function getCommentDetail(id) {
  return service({
    url: "api/admin/comment/get",
    method: "get",
    params: { id }
  });
}

/**
 * 更新评论状态
 */
export async function updateCommentStatus(data) {
  return service({
    url: "api/admin/comment/updateStatus",
    method: "post",
    data
  });
}

/**
 * 批量删除评论
 */
export async function deleteComment(ids) {
  return service({
    url: "api/admin/comment/delete",
    method: "delete",
    data: ids
  });
}
