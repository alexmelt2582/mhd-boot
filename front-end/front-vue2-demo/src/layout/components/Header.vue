<template>
  <div
    class="
      header-container
      px-32
      py-3
      flex
      items-center
      justify-between
      sticky
      top-0
      shadow-sm
      z-50
    "
  >
    <!-- 左侧Logo和菜单区域 -->
    <div class="flex items-center">
      <!-- Logo部分 -->
      <div
        class="flex items-center mr-8"
        @click="$router.push('/')"
        style="cursor: pointer"
      >
        <img :src="logo" loading="lazy" alt="Logo" class="h-8 w-auto" />
        <span class="ml-2 text-xl font-bold text-meText">{{ title }}</span>
      </div>

      <!-- 导航菜单 -->
      <div class="hidden md:flex items-center space-x-6">
        <div
          v-for="(item, index) in menuItems"
          :key="index"
          class="menu-item cursor-pointer"
          :class="{ 'menu-item-active': activeMenu === item.path }"
          @click="handleSelect(item.path)"
        >
          {{ item.name }}
        </div>
      </div>
    </div>

    <!-- 中间搜索框 -->
    <div class="hidden flex-1 md:flex justify-center max-w-xl mx-auto px-4">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索..."
        class="w-full"
        prefix-icon="el-icon-search"
        @keyup.enter="handleSearch"
      >
      </el-input>
    </div>

    <!-- 右侧用户信息区域 -->
    <div class="flex items-center space-x-4">
      <!-- 消息通知 -->
      <div
        v-if="userInfo.id"
        class="
          message-btn
          relative
          text-meText
          hover:text-meHoverText
          cursor-pointer
        "
        @click="$router.push('/notification')"
      >
        <span
          v-show="unreadCount > 0"
          class="
            z-10
            absolute
            -top-1
            -right-1
            h-4
            w-4
            bg-red-500
            rounded-full
            text-xs text-white
            flex
            items-center
            justify-center
          "
          >{{ unreadCount > 99 ? "99+" : unreadCount }}</span
        >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-6 w-6"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
          />
        </svg>
      </div>
      <!-- 用户信息下拉菜单 -->
      <el-dropdown v-if="userInfo.id" trigger="click" placement="bottom">
        <div class="flex items-center space-x-2 cursor-pointer">
          <el-avatar
            :size="32"
            :src="attachImageUrl(userInfo.avatar)"
            @error="attachImageUrlError"
            class="rounded-full"
          />
          <span class="text-meText">{{ userInfo.username }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <span style="display: block" @click="$router.push('/profile')">
              <el-dropdown-item>
                <i class="el-icon-user mr-2"></i>
                个人中心
              </el-dropdown-item>
            </span>
            <span style="display: block" @click="$router.push('/favorite')">
              <el-dropdown-item>
                <i class="el-icon-star-off mr-2"></i>
                我的收藏
              </el-dropdown-item>
            </span>
            <span
              style="display: block"
              @click="$router.push('/health/record')"
            >
              <el-dropdown-item>
                <i class="el-icon-document mr-2"></i>
                健康记录
              </el-dropdown-item>
            </span>
            <span style="display: block" @click="handleLogout">
              <el-dropdown-item divided>
                <i class="el-icon-switch-button mr-2"></i>
                退出登录
              </el-dropdown-item>
            </span>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 未登录状态显示登录按钮 -->
      <el-button
        v-else
        type="text"
        class="login-btn text-meTheme hover:text-meHoverText"
        @click="$router.push('/login')"
      >
        <i class="el-icon-user mr-1"></i>
        登录
      </el-button>
    </div>
  </div>
</template>

<script>
import Logo from "@/assets/images/logo/logo.png";
import { getUnreadCount } from "@/api/front/base/notification";
import defaultSettings from "@/settings";
import imgMixin from "@/mixins/img";

export default {
  name: "Header",
  mixins: [imgMixin],
  data() {
    return {
      title: defaultSettings.title,
      logo: Logo,
      searchKeyword: "",
      unreadCount: 0,
      messageTimer: null,
      menuItems: [
        { name: "首页", path: "/" },
        { name: "我的收藏", path: "/favorite" },
        { name: "消息中心", path: "/notification" }
      ]
    };
  },
  computed: {
    activeMenu() {
      // 去掉尾部多余斜杠，再按 / 分割
      const segments = this.$route.path.replace(/\/$/, "").split("/");
      // 取前 3 段：['', 'health', 'record'] → '/health/record'
      return segments.slice(0, 3).join("/") || "/";
    },
    userInfo() {
      return this.$store.state.user.userInfo;
    }
  },
  watch: {
    "userInfo.id": {
      handler(newVal) {
        if (newVal) {
          this.startNotificationPolling();
        } else {
          this.stopNotificationPolling();
        }
      }
    }
  },
  created() {
    if (this.userInfo.id) {
      this.startNotificationPolling();
    }
  },
  beforeDestroy() {
    this.stopNotificationPolling();
  },
  methods: {
    // 导航菜单选择
    handleSelect(path) {
      this.$router.push(`${path}`);
    },
    // 搜索处理
    handleSearch() {
      if (!this.searchKeyword.trim()) return;
      this.$router.push({
        path: "/search",
        query: { keyword: this.searchKeyword }
      });
      this.searchKeyword = "";
    },
    // 退出登录
    handleLogout() {
      const that = this;
      this.$confirm("确定退出系统吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        that.stopNotificationPolling();
        that.$store.dispatch("user/HandleLogout").then(() => {
          setTimeout(() => {
            location.reload();
          }, 100);
        });
      });
    },
    // 开始轮询未读消息
    startNotificationPolling() {
      this.fetchUnreadCount();
      this.messageTimer = setInterval(() => {
        this.fetchUnreadCount();
      }, 60000); // 每分钟查询一次
    },
    // 停止轮询
    stopNotificationPolling() {
      if (this.messageTimer) {
        clearInterval(this.messageTimer);
        this.messageTimer = null;
      }
    },
    // 获取未读消息数量
    async fetchUnreadCount() {
      try {
        const { data } = await getUnreadCount();
        this.unreadCount = data;
      } catch (error) {
        console.error("获取未读消息数量失败:", error);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.header-container {
  border-bottom: 1px solid #ebeef5;
  height: 60px;
}

.menu-item {
  font-size: 15px;
  color: var(--me-text-color);
  padding: 4px 0;
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    color: var(--me-hover-text-color);
  }

  &.menu-item-active {
    color: var(--me-active-text-color);
    font-weight: 500;

    &::after {
      content: "";
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 100%;
      height: 2px;
      background-color: var(--me-theme-color);
      border-radius: 1px;
    }
  }
}

.message-btn {
  .me-icon-svg {
    font-size: 24px;
    transition: all 0.3s ease;
  }

  &:hover {
    .me-icon-svg {
      transform: scale(1.1);
    }
  }
}

::v-deep .el-input {
  .el-input__inner {
    border-radius: 20px;
    background-color: #f5f7fa;
    border: 1px solid #f5f7fa;
    padding-left: 40px;
    height: 36px;

    &:focus {
      border-color: var(--el-color-primary);
      background-color: #fff;
    }
  }

  .el-input__prefix {
    left: 15px;
  }
}

.login-btn {
  padding: 8px 16px;
  font-size: 14px;
}
</style>
