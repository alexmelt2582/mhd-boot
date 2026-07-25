// 引入 vuex
import Vuex from "vuex";
import Vue from "vue";
import user from "@/store/modules/user";
import settings from "@/store/modules/settings";
// 安装插件
Vue.use(Vuex);

// 对外暴露仓库
// 第一个注意:需要关键字 new ，你没有 new 会报错的
// 第二个注意: Store 构造函数,书写的时候别小写
export default new Vuex.Store({
  // 大仓库需要注册全部小仓库
  // vuex 新增的一个配置项:模块式开发.右侧V也是对象
  modules: {
    user,
    settings
  }
});
