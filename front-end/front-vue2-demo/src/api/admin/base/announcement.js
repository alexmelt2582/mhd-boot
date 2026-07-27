import service from "@/utils/service";

/**
 * 分页查询公告列表
 */
export async function getAnnouncementPage(params) {
  return service({
    url: "api/admin/announcement/page",
    method: "get",
    params
  });
}

/**
 * 查询单个公告详情
 */
export async function getAnnouncementById(id) {
  return service({
    url: "api/admin/announcement/get",
    method: "get",
    params: { id }
  });
}

/**
 * 创建公告
 */
export async function createAnnouncement(data) {
  return service({
    url: "api/admin/announcement/create",
    method: "post",
    data
  });
}

/**
 * 更新公告状态
 */
export async function updateAnnouncementStatus(data) {
  return service({
    url: "api/admin/announcement/updateStatus",
    method: "post",
    data
  });
}

/**
 * 批量删除公告
 */
export async function deleteAnnouncement(ids) {
  return service({
    url: "api/admin/announcement/delete",
    method: "delete",
    data: ids
  });
}
