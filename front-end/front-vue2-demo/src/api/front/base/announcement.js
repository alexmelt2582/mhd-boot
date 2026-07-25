import service from "@/utils/service";

/**
 * 查看有效公告列表
 */
export async function getValidAnnouncements() {
  return service({
    url: "api/front/announcement/list",
    method: "get"
  });
}

/**
 * 分页查询有效公告
 */
export async function getValidAnnouncementsPage(params) {
  return service({
    url: "api/front/announcement/page",
    method: "get",
    params
  });
}

/**
 * 查看公告详情
 */
export async function getAnnouncementDetail(id) {
  return service({
    url: "api/front/announcement/detail",
    method: "get",
    params: { id }
  });
}
