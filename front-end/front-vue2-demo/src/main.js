import Vue from "vue";
import App from "./App.vue";

Vue.config.productionTip = false;
// 引入 normalize.css ，清除浏览器本身样式
import "normalize.css";
// 引入styles，保持在 ElementUI 之前引入
import "@/assets/styles/index.scss";
// 引入 router
import router from "@/router";
// 引入 vuex
import store from "@/store";
// 引入 ElementUI
import ElementUI from "element-ui";
// 引入 ElementUI 的样式
import "element-ui/lib/theme-chalk/index.css";
// 引入全局组件配置
import MeComponents from "@/components/index.js";
// 引入全局指令配置
import MeDirectives from "@/directives/index.js";
// 引入 icon
import "@/assets/icons";
// 引入 echarts
import * as echarts from "echarts";
// 引入全局的深拷贝
import deepCopy from "@/utils/deepCopy";

Vue.prototype.$echarts = echarts;
Vue.prototype.$deepCopy = deepCopy;

// 使用 ElementUI
Vue.use(ElementUI, {
  size: "small"
});
// 默认点击背景不关闭弹窗
ElementUI.Dialog.props.closeOnClickModal.default = false;
// 使用自定义组件
Vue.use(MeComponents);
// 使用自定义指令
Vue.use(MeDirectives);

new Vue({
  el: "#app",
  router: router,
  store: store,
  render: h => h(App)
});
