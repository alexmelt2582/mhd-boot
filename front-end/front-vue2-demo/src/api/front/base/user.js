import service from "@/utils/service";

// 获取当前用户信息
export async function getCurrentUserInfo() {
  return service({
    url: "api/front/user/info",
    method: "get"
  });
}

// 更新用户基本信息
export async function updateUserInfo(data) {
  return service({
    url: "api/front/user/update",
    method: "put",
    data
  });
}

// 修改密码
export async function updatePassword(data) {
  return service({
    url: "api/front/user/updatePassword",
    method: "post",
    data
  });
}

// 上传头像
export async function uploadAvatar(data) {
  return service({
    url: "api/front/user/uploadAvatar",
    method: "post",
    data
  });
}

// 注销账户
export async function logOff() {
  return service({
    url: "api/front/user/logOff",
    method: "delete"
  });
}
