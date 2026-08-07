import service from "@/utils/service";

export async function page${methodName}(query) {
  return service({
    url: "api/${urlPath}/page",
    method: "get",
    params: query || undefined
  });
}

export async function get${methodName}(id) {
  return service({
    url: "api/${urlPath}/get?id=" + id,
    method: "get"
  });
}

export async function add${methodName}(data) {
  return service({
    url: "api/${urlPath}/create",
    method: "post",
    data
  });
}

export async function update${methodName}(data) {
  return service({
    url: "api/${urlPath}/update",
    method: "put",
    data: data
  });
}

export async function del${methodName}(ids) {
  return service({
    url: "api/${urlPath}/delete",
    method: "delete",
    data: ids
  });
}

export async function all${methodName}() {
  return service({
    url: "api/${urlPath}/all",
    method: "get"
  });
}