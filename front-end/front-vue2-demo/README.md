## Project Execute

```
pnpm install
pnpm run serve
pnpm run prettier
pnpm run build
```

## 目录介绍

- .idea：这是IDE（如IntelliJ IDEA或WebStorm）生成的项目配置文件夹，包含项目的配置信息。
- node_modules：存放项目依赖的第三方库，由npm或yarn管理。
- public：
    - index.html：项目的入口HTML文件，所有的Vue组件最终会被挂载到这个文件的某个元素上。
    - vue.png：通常是项目的logo或图标，可能会在index.html中引用。
- src：存放项目的源代码。
    - api：存放与后端API交互的代码，如HTTP请求函数。根据 views 目录下的页面合理编写。例如 views 目录下是
      views/system/user.index，则在当前 api 目录下应该编写 api/system/user.js 文件，文件中的函数写法应遵循 template.js 文件
    - assets：存放静态资源，如图片、字体等。
    - components：存放Vue组件，每个组件通常是一个独立的功能模块。
    - config：存放项目的配置文件，如环境变量配置。
    - directives：存放自定义指令，可以在模板中使用。
    - routers：存放路由配置，定义应用的路由规则。
    - stores：存放Vuex状态管理相关的代码，用于管理全局 状态。
    - utils：存放工具函数，这些函数可以在项目中多个地 方复用。
    - views：存放视图组件，通常与路由一一对应。
    - App.vue：根组件，是所有其他组件的父组件。
    - main.js：项目的入口文件，用于初始化Vue实例，挂载根组件，配置全局设置等。
- .env.development：开发环境的环境变量配置文件。
- .eslintignore：ESLint忽略检查的文件和目录列表。
- .eslintrc.js：ESLint的配置文件，用于定义代码检查规则。
- .gitignore：Git忽略的文件和目录列表，通常用于忽略node_modules、编译后的文件等。
- .npmrc：npm的配置文件，用于定义npm的行为。
- .prettierignore：Prettier忽略格式化的文件和目录列表。
- .prettierrc.js：Prettier的配置文件，用于定义代码格式化规则。
- babel.config.js：Babel的配置文件，用于定义JavaScript的编译规则。
- jsconfig.json：JavaScript项目的配置文件，通常用于定义编译选项和路径别名。
- package.json：项目的配置文件，定义了项目的依赖、脚本、版本等信息。
- pnpm-lock.yaml：pnpm的锁定文件，记录了依赖的具体版本，确保项目在不同环境下的一致性。
- README.md：项目的说明文件，通常包含项目的介绍、安装和使用说明等。
- vue.config.js：Vue CLI的配置文件，用于自定义Vue CLI的行为和配置
