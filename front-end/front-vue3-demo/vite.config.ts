import { fileURLToPath, URL } from 'node:url'

import { ConfigEnv, defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
// 自动导入
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
// 自动导入 ElementPlus
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
// 自动导入 TailwindCSS
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig(({ command, mode }: ConfigEnv) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [
      vue(),
      tailwindcss(),
      AutoImport({
        imports: ['vue', 'vue-router'],
        dts: 'auto-imports.d.ts',
        resolvers: [ElementPlusResolver()],
      }),
      Components({
        // 自动导入 src/components 目录下的Vue组件
        dts: 'components.d.ts',
        resolvers: [ElementPlusResolver()],
      }),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      // 服务器将监听所有公共 IP
      // host: '0.0.0.0',
      // 服务器的端口号
      port: Number(env.VITE_APP_PORT) || 18080,
      // 设置为 true 时，Vite 会在启动时自动打开浏览器窗口
      open: false,
      proxy: {
        // 匹配请求路径的模式
        [env.VITE_APP_BASE_API]: {
          // 代理的目标地址
          target: env.VITE_APP_SERVER_PATH,
          // 更改请求的origin为代理服务器的origin
          changeOrigin: true,
          // 路径重写规则，移除匹配的路径前缀
          rewrite: (path: string) => path.replace(new RegExp(`^${env.VITE_APP_BASE_API}`), ''),
        },
      },
    },
  }
})
