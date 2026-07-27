/** 对外暴露插件对象，注册全局指令 */
const directives = {};

export default {
  // install方法， Object.keys()得到对象所有的key
  install(Vue) {
    Object.keys(directives).forEach(key => {
      Vue.directive(key, directives[key]);
    });
  }
};
