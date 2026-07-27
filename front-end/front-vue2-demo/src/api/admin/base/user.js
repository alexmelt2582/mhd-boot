import service from "@/utils/service";

export async function pageBaseUser(query) {
  return service({
    url: "api/admin/user/page",
    method: "get",
    params: query || undefined
  });
}

export async function addBaseUser(data) {
  return service({
    url: "api/admin/user/create",
    method: "post",
    data
  });
}

export async function updateBaseUser(data) {
  return service({
    url: "api/admin/user/update",
    method: "put",
    data
  });
}

export async function delBaseUser(ids) {
  return service({
    url: "api/admin/user/delete",
    method: "delete",
    data: ids
  });
}

export async function updateStatus(data) {
  return service({
    url: "api/admin/user/updateStatus",
    method: "post",
    data
  });
}

export async function resetPwd(ids) {
  return service({
    url: "api/admin/user/resetPwd",
    method: "post",
    data: ids
  });
}

export async function updatePass(data) {
  return service({
    url: "api/admin/user/updatePass",
    method: "post",
    data
  });
}

export async function updateAvatar(data) {
  return service({
    url: "api/admin/user/updateAvatar",
    method: "post",
    data
  });
}
