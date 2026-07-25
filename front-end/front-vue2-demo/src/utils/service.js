import axios from "axios";
import { meMsgError } from "@/utils/modal.js";
import store from "@/store";
import defaultSettings from "@/settings";

const BASE_URL = process.env.VUE_APP_WEB_BASE_URL;

const service = axios.create({
  baseURL: BASE_URL,
  timeout: defaultSettings.timeout // 请求超时时间
});

/**
 * @description: 请求拦截器
 * @returns {*}
 */
service.interceptors.request.use(
  config => {
    let token = store.state.user.token;
    // 如果实现挤下线功能，需要用户绑定一个uuid，uuid发生变化，后端将数据进行处理[直接使用Sa-Token框架也阔以]
    if (token) {
      config.headers[defaultSettings.tokenKey] = token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

/**
 * @description: 响应拦截器
 * @returns {*}
 */
service.interceptors.response.use(
  res => {
    // 返回数据格式 { data: {}, status: 200, statusText: "OK", headers: {} }
    // status 是 http 状态码，data 中是实际返回的数据
    // const status = res.status; // http 状态码
    // 判断是否返回的是文件
    if (res.request.responseType === "blob") {
      return res;
    }
    const code = res.data.code; // 后端返回数据状态
    if (code === undefined) {
      // 返回list列表
      return res.data;
    } else if (code === 0) {
      // 服务器连接状态，非后端返回的status 或者 code
      // console.log("200状态", status);
      return res.data;
    } else if (code === 100300002) {
      // token 异常
      store.dispatch("user/HandleLogout").then(() => {
        setTimeout(() => {
          location.reload();
        }, 1000);
      });
    } else {
      // console.log("后端返回数据：", res.data.msg)
      meMsgError({
        message: res.data.message + "" || "服务器未知错误"
      });
      return Promise.reject(res.data.message + "" || "服务器未知错误"); // 可以将异常信息延续到页面中处理，使用try{}catch(error){};
    }
  },
  error => {
    // 处理网络错误，不是服务器响应的数据
    // console.log("进入错误", error);
    error.data = {};
    if (error && error.response) {
      switch (error.response.status) {
        case 400:
          error.data.message = "错误请求";
          break;
        case 401:
          error.data.message = "未授权，请重新登录";
          break;
        case 403:
          error.data.message = "对不起，您没有权限访问";
          break;
        case 404:
          error.data.message = "请求错误,未找到请求路径";
          break;
        case 405:
          error.data.message = "请求方法未允许";
          break;
        case 408:
          error.data.message = "请求超时";
          break;
        case 500:
          error.data.message = "服务器出错，请重试";
          break;
        case 501:
          error.data.message = "网络未实现";
          break;
        case 502:
          error.data.message = "网络错误";
          break;
        case 503:
          error.data.message = "服务不可用";
          break;
        case 504:
          error.data.message = "网络超时";
          break;
        case 505:
          error.data.message = "http版本不支持该请求";
          break;
        default:
          error.data.message = `连接错误${error.response.status}`;
      }
    } else {
      error.data.message = "连接到服务器失败";
    }
    meMsgError({
      message: error.data.message
    });
    return Promise.reject(error); // 将错误返回给 try{} catch(){} 中进行捕获，就算不进行捕获，上方 res.data.status != 200 也会抛出提示。
  }
);

export function getBaseURL() {
  return BASE_URL;
}

export default service;
