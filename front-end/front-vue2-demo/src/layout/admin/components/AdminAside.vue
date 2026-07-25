<template>
  <div class="has-logo">
    <div class="aside-logo flex items-center justify-center">
      <transition name="fade">
        <div v-if="isCollapse" class="flex items-center justify-center">
          <img :src="logo" alt="logo" class="w-10 h-10 rounded-md" />
        </div>
        <div v-else class="flex items-center justify-center">
          <img :src="logo" alt="logo" class="w-10 h-10 rounded-md mr-4" />
          <h1 class="font-bold text-lg">{{ title }}</h1>
        </div>
      </transition>
    </div>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :active-text-color="variables.menuActiveText"
        :collapse-transition="false"
        unique-opened
        mode="vertical"
        @select="handleSelect"
      >
        <el-menu-item
          v-for="(item, index) in menus"
          :key="index"
          :index="item.path"
        >
          <me-icon-svg :name="item.icon" class="menu-icon" />
          <span>{{ item.name }}</span>
        </el-menu-item>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import variables from "@/assets/styles/variables.module.scss";
import logo from "@/assets/images/logo/logo.png";
import defaultSettings from "@/settings";

export default {
  name: "AdminAside",
  data() {
    return {
      logo,
      title: defaultSettings.title,
      activeMenu: this.$route.path,
      menus: [
        {
          name: "数据总览",
          icon: "el-icon-house",
          path: "/admin"
        },
        {
          name: "反馈管理",
          icon: "el-icon-chat-dot-square",
          path: "/admin/feedback"
        },
        {
          name: "消息管理",
          icon: "el-icon-chat-dot-round",
          path: "/admin/notification"
        },
        {
          name: "用户管理",
          icon: "el-icon-user",
          path: "/admin/user"
        },
        {
          name: "评论管理",
          icon: "el-icon-chat-dot-square",
          path: "/admin/comment"
        },
        {
          name: "轮播图管理",
          icon: "el-icon-chat-dot-square",
          path: "/admin/banner"
        }
      ]
    };
  },
  computed: {
    variables() {
      return variables;
    },
    isCollapse() {
      return this.$store.state.settings.isCollapse;
    }
  },
  watch: {
    $route: {
      handler() {
        this.activeMenu = this.$route.path;
      },
      immediate: true
    }
  },
  methods: {
    handleSelect(index) {
      this.$router.push(index);
    }
  }
};
</script>

<style lang="scss" scoped>
@import "@/assets/styles/variables.module";

.menu-icon {
  @apply mr-2;
  color: $menuText !important;
}

.aside-logo {
  color: $menuText !important;
  height: $headerHeight !important;
}
</style>
