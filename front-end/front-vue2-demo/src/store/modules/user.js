import { meLocalStorage, meSessionStorage } from "@/utils/storage";
import defaultSettings from "@/settings";
import { getLoginInfo, login, logout } from "@/api/login/login";

export default {
  // 启用命名空间，避免命名冲突：多个 Vuex 模块，并且这些模块之间有可能存在相同的 getter、mutation 或 action 名称。
  namespaced: true,
  state: {
    token:
      meLocalStorage.handleGetItem(defaultSettings.tokenCookieKey) ||
      meSessionStorage.handleGetItem(defaultSettings.tokenCookieKey) ||
      "", // token
    userInfo: {}
  },
  mutations: {
    setToken(state, token) {
      state.token = token;
    },
    setUserInfo(state, userInfo) {
      state.userInfo = userInfo;
    }
  },
  actions: {
    // 获取用户信息
    async getUserInfo({ commit }) {
      return new Promise((resolve, reject) => {
        getLoginInfo()
          .then(res => {
            if (res.code === 0) {
              commit("setUserInfo", res.data);
              resolve(res);
            } else {
              resolve(res);
            }
          })
          .catch(error => {
            reject(error);
          });
      });
    },
    // 登入
    async HandleLogin({ commit }, user, rememberMe) {
      return new Promise((resolve, reject) => {
        login(user)
          .then(res => {
            if (res.code === 0) {
              if (rememberMe) {
                meLocalStorage.handleSetItem(
                  defaultSettings.tokenCookieKey,
                  res.data.token
                );
              } else {
                meSessionStorage.handleSetItem(
                  defaultSettings.tokenCookieKey,
                  res.data.token
                );
              }
              commit("setToken", res.data.token);
              commit("setUserInfo", res.data.user);
              resolve(res);
            } else {
              resolve(res);
            }
          })
          .catch(error => {
            reject(error);
          });
      });
    },
    // 登出
    async HandleLogout({ commit }) {
      return new Promise((resolve, reject) => {
        logout()
          .then(() => {
            meLocalStorage.handleRemoveItem(defaultSettings.tokenCookieKey);
            meSessionStorage.handleRemoveItem(defaultSettings.tokenCookieKey);
            commit("setToken", "");
            commit("setUserInfo", {});
            resolve();
          })
          .catch(error => {
            meLocalStorage.handleRemoveItem(defaultSettings.tokenCookieKey);
            meSessionStorage.handleRemoveItem(defaultSettings.tokenCookieKey);
            commit("setToken", "");
            commit("setUserInfo", {});
            reject(error);
          });
      });
    }
  }
};
