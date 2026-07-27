import MeIconSvg from "@/components/MeIconSvg/index.vue";
import MeEditForm from "@/components/MeEditForm/index.vue";
import MeCrudButton from "@/components/MeCrudButton/index.vue";
import MeFileUpload from "@/components/MeFileUpload/index.vue";
import MePagination from "@/components/MePagination/index.vue";
import MeSearchForm from "@/components/MeSearchForm/index.vue";
import MeTable from "@/components/MeTable/index.vue";
import MeCoverImage from "@/components/MeCoverImage/index.vue";
import MeWangEditor from "@/components/MeWangEditor/index.vue";
import MeDescription from "@/components/MeDescription/index.vue";
import MeComment from "@/components/MeComment/index.vue";

/** 对外暴露插件对象，注册全局组件 */
const components = {
  MeComment,
  MeCoverImage,
  MeCrudButton,
  MeDescription,
  MeEditForm,
  MeFileUpload,
  MeIconSvg,
  MePagination,
  MeSearchForm,
  MeTable,
  MeWangEditor,
};

export default {
  // install方法， Object.keys()得到对象所有的key
  install(Vue) {
    Object.keys(components).forEach(key => {
      Vue.component(key, components[key]);
    });
  }
};
