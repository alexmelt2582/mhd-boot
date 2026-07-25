import { createApp } from 'vue'
import App from './App.vue'
// 引入 router
import router from '@/router/index'
// 引入仓库pinia
import pinia from "@/store/index";
// 引入 ElementPlus
import ElementPlus from 'element-plus'
// 引入 ElementPlus 的样式
import 'element-plus/dist/index.css'
// 引入 ElementPlus 的暗黑模式 css
import 'element-plus/theme-chalk/dark/css-vars.css'
// 引入 ElementPlus 所有图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 引入 ElementPlus 中文包
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// 引入styles
import '@/assets/styles/index.scss'
// 引入 Tailwind CSS，放置在最下方，防止覆盖 ElementPlus
import '@/assets/styles/tailwind.css'

const app = createApp(App)

// 使用 ElementPlus
app.use(ElementPlus, {
  locale: zhCn,
})
// 注册ElementPlus所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用 router
app.use(router)
// 使用 pinia
app.use(pinia)
app.mount('#app')
