const path = require("path");
// 默认配置
const defaultSettings = require("./src/settings.js");
// gzip压缩
const CompressionPlugin = require("compression-webpack-plugin");

function resolve(dir) {
  return path.join(__dirname, dir);
}

const title = defaultSettings.title; // 网址标题
const port = defaultSettings.port; // 端口配置

// vue.config.js 配置说明
// 官方vue.config.js 参考文档 https://cli.vuejs.org/zh/config/#css-loaderoptions// 这里只列一部分，具体配置参考文档
module.exports = {
  /*
   * 默认情况下，Vue CLI 会假设你的应用是被部署在一个域名的根路径上，
   * 例如 https://www.my-app.com/。
   * 如果你的应用部署在一个子路径上，你就需要用这个选项指定这个子路径。
   * 例如，如果你的应用部署在 https://www.my-app.com/my-app/，则设置 publicPath 为 /my-app/
   */
  publicPath: "/",
  // 在npm run build 或 yarn build 时 ，生成文件的目录名称（要和baseUrl的生产环境路径一致）（默认dist）
  outputDir: "dist",
  // 是否开启 eslint 检测，有效值：true | false,开发模式启用
  lintOnSave: process.env.NODE_ENV === "development",
  // 如果你不需要生产环境的 source map，可以将其设置为 false 以加速生产环境构建。
  productionSourceMap: false,
  // webpack 配置
  configureWebpack: {
    resolve: {
      // 设置路径别名
      alias: { "@": resolve("src") },
      fallback: { path: require.resolve("path-browserify") }
    },
    plugins: [
      // https://www.ydyno.com/archives/1260.html 使用gzip解压缩静态文件
      new CompressionPlugin({
        algorithm: "gzip", // 使用gzip压缩
        // test: /\.(js|css|html)?$/i, // 压缩文件格式
        test: /\.(js|css|json|txt|html|ico|svg)(\?.*)?$/i, // 压缩文件格式
        // filename: '[path].gz[query]', // 压缩后的文件名，目前打开报错
        // threshold: 10240, // 仅压缩大于 10kb 的文件
        minRatio: 0.8 // 压缩率至少 0.8 才进行压缩
      })
    ]
  },
  // 跨域
  devServer: {
    // 服务器将监听所有公共 IP
    host: "0.0.0.0",
    // 端口
    port: port,
    // 自动打开浏览器
    open: false,
    client: {
      // 取消错误时的全屏遮罩
      overlay: false
    },
    proxy: {
      "/api": {
        target: process.env.VUE_APP_WEB_BASE_URL,
        logLevel: "debug", // 显示代理真实路径
        changeOrigin: true, // 是否允许跨域
        ws: false, // 如果要代理 websockets，配置这个参数
        secure: false, // 如果是https接口，需要配置这个参数
        //路径重写
        pathRewrite: {
          // 重写路径请求
          "^/api": "api/"
        }
      }
    }
  },
  chainWebpack: config => {
    // 网页标题
    config.plugin("html").tap(args => {
      args[0].title = title;
      return args;
    });
    // 引入svg
    config.module.rule("svg").exclude.add(resolve("src/assets/icons")).end();
    config.module
      .rule("icons")
      .test(/\.svg$/)
      .include.add(resolve("src/assets/icons"))
      .end()
      .use("svg-sprite-loader")
      .loader("svg-sprite-loader")
      .options({
        symbolId: "icon-[name]"
      })
      .end();
  }
};
