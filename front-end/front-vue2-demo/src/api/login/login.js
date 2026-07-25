import service from "@/utils/service";

export async function login(user) {
  return service({
    url: "api/auth/login",
    method: "post",
    data: user
  });
}

export async function logout() {
  return service({
    url: "api/auth/logout",
    method: "delete"
  });
}

export async function register(data) {
  return service({
    url: "api/auth/register",
    method: "post",
    data
  });
}

export async function getLoginInfo() {
  return service({
    url: "api/auth/info",
    method: "get"
  });
}
