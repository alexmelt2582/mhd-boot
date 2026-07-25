/**
 * 字体图标, 统一使用SVG Sprite矢量图标(http://www.iconfont.cn/）
 *
 * 使用:
 *  1. 在阿里矢量图标站创建一个项目, 并添加图标(这一步非必须, 创建方便项目图标管理)
 *  2-1. 添加icon, 选中新增的icon图标, 复制代码 -> 下载 -> SVG下载 -> 粘贴代码(重命名)
 *  3. 组件模版中使用 [<me-icon-svg name="user" />]
 */
// true 代表递归查找 svg 目录下内容
const req = require.context('./svg', true, /\.svg$/)
const requireAll = requireContext => requireContext.keys().map(requireContext)
requireAll(req)