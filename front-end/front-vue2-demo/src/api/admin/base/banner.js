import service from "@/utils/service";

export async function pageBaseBanner(query) {
  return service({
    url: "api/admin/banner/page",
    method: "get",
    params: query || undefined
  });
}

export async function getBaseBanner(id) {
  return service({
    url: "api/admin/banner/get?id=" + id,
    method: "get"
  });
}

export async function addBaseBanner(data) {
  return service({
    url: "api/admin/banner/create",
    method: "post",
    data
  });
}

export async function updateBaseBanner(data) {
  return service({
    url: "api/admin/banner/update",
    method: "put",
    data
  });
}

export async function delBaseBanner(ids) {
  return service({
    url: "api/admin/banner/delete",
    method: "delete",
    data: ids
  });
}

export async function changeBannerStatus(data) {
  return service({
    url: "api/admin/banner/status",
    method: "post",
    data
  });
}
