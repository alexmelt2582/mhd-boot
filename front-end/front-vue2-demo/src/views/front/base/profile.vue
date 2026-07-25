<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex flex-col lg:flex-row gap-8">
      <!-- 左侧菜单 -->
      <div class="lg:w-1/4">
        <!-- 个人信息卡片 -->
        <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div class="flex flex-col items-center">
            <div class="relative mb-4">
              <el-image
                :src="attachImageUrl(userInfo.avatar)"
                alt="用户头像"
                class="w-24 h-24 rounded-full object-cover"
                fit="cover"
                lazy
              >
                <div
                  slot="error"
                  class="
                    w-24
                    h-24
                    rounded-full
                    bg-gray-200
                    flex
                    items-center
                    justify-center
                  "
                >
                  <i class="el-icon-picture-outline text-gray-400 text-2xl"></i>
                </div>
              </el-image>
              <div class="absolute bottom-0 right-0">
                <el-button
                  type="primary"
                  icon="el-icon-camera"
                  circle
                  size="small"
                  class="shadow-sm"
                  @click="handleUploadAvatar"
                ></el-button>
              </div>
            </div>
            <h2 class="text-xl font-medium text-gray-900">
              {{ userInfo.username || "未设置" }}
            </h2>
            <p class="text-gray-500 text-sm">用户ID: {{ userInfo.id }}</p>
            <p class="text-gray-500 text-sm mt-2">
              上次登录：{{ formatTime(userInfo.loginTime) }}
            </p>
          </div>
        </div>
        <!-- 功能列表 -->
        <div class="bg-white rounded-lg shadow-sm overflow-hidden">
          <div
            v-for="menu in menuList"
            :key="menu.key"
            class="p-4 hover:bg-gray-50 cursor-pointer border-b last:border-b-0"
            :class="{ 'bg-blue-50 border-blue-200': activePanel === menu.key }"
            @click="handleMenuClick(menu.key)"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <i :class="menu.icon" class="text-gray-400"></i>
                <span class="text-gray-700">{{ menu.label }}</span>
              </div>
              <i class="el-icon-arrow-right text-gray-400"></i>
            </div>
          </div>
        </div>
        <!-- 退出登录按钮 -->
        <div class="mt-6">
          <el-button type="danger" class="w-full" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </div>
      <!-- 右侧内容区 -->
      <!--<div class="lg:w-3/4 transition-all">-->
      <!--  <component :is="componentMap[activePanel]" ref="dynamicComponent" />-->
      <!--</div>-->
      <div class="lg:w-3/4 relative">
        <!-- 要切换的组件，v-if 触发销毁/重建，用 key 强制重新渲染 -->
        <transition name="fade" mode="out-in" appear>
          <component
            :is="componentMap[activePanel]"
            ref="dynamicComponent"
            :key="activePanel"
            class="absolute inset-0 transition-opacity duration-300 ease-in-out"
          />
        </transition>
      </div>
    </div>

    <el-dialog
      v-if="avatarOpen"
      title="头像上传"
      :visible.sync="avatarOpen"
      width="800px"
      :append-to-body="true"
      :before-close="handleAvatarClose"
    >
      <me-cover-image
        :custom-options="{
          fixedBox: true
        }"
        :initial-img="
          avatarForm.avatar ? attachImageUrl(avatarForm.avatar) : ''
        "
        @handleFile="handleAvatarFile"
        @close="handleAvatarClose"
      />
    </el-dialog>
  </div>
</template>

<script>
import { getCurrentUserInfo, uploadAvatar } from "@/api/front/base/user";
import { meMsgSuccess } from "@/utils/modal";
import imgMixin from "@/mixins/img";
import { timeAgo } from "@/utils/date";

export default {
  name: "Profile",
  mixins: [imgMixin],
  data() {
    return {
      // region 编辑表格
      avatarOpen: false,
      defaultAvatarForm: {
        id: undefined,
        avatar: undefined
      },
      avatarForm: {},
      // endregion
      activePanel: "basic-info", // 当前激活面板
      userInfo: {}, // 用户信息
      // 菜单列表
      menuList: [
        { key: "basic-info", label: "基本信息", icon: "el-icon-user" },
        { key: "change-password", label: "修改密码", icon: "el-icon-key" },
        { key: "feedback", label: "意见反馈", icon: "el-icon-chat-dot-round" },
        { key: "my-feedback", label: "我的反馈", icon: "el-icon-document" },
        { key: "about", label: "关于我们", icon: "el-icon-info" }
      ],
      // 组件映射
      componentMap: {
        "basic-info": () => import("./profile-basicinfo.vue"),
        "change-password": () => import("./profile-changepassword.vue"),
        feedback: () => import("./profile-feedback.vue"),
        "my-feedback": () => import("./profile-feedback-list.vue"),
        about: () => import("./profile-about.vue")
      }
    };
  },
  created() {
    this.fetchUserInfo();
  },
  methods: {
    // 格式化时间
    formatTime(time) {
      return time ? timeAgo(time) : "暂无";
    },
    // 获取用户信息
    async fetchUserInfo() {
      try {
        const { data } = await getCurrentUserInfo();
        this.userInfo = data || {};
        // 填充基本信息表单
        this.basicForm = {
          username: data.username || "",
          gender: data.gender || null,
          mobile: data.mobile || "",
          email: data.email || "",
          introduction: data.introduction || ""
        };
      } catch (error) {
        console.error("获取用户信息失败:", error);
      }
    },
    // 菜单点击
    handleMenuClick(key) {
      this.activePanel = key;
    },
    // 退出登录
    handleLogout() {
      const that = this;
      this.$confirm("确定要退出登录吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        that.$store.dispatch("user/HandleLogout").then(() => {
          setTimeout(() => {
            location.reload();
          }, 1000);
        });
      });
    },
    handleUploadAvatar() {
      this.avatarForm.id = this.userInfo.id;
      this.avatarForm.avatar = this.userInfo.avatar;
      this.avatarOpen = true;
    },
    avatarReset() {
      this.avatarForm = JSON.parse(JSON.stringify(this.defaultAvatarForm));
    },
    async handleAvatarFile(blob) {
      const that = this;
      const formData = new FormData();
      formData.append("file", blob, "user.jpg");
      await uploadAvatar(formData);
      meMsgSuccess({
        message: "上传成功"
      });
      that.avatarReset();
      await that.fetchUserInfo();
    },
    handleAvatarClose() {
      this.avatarOpen = false;
      this.avatarReset();
    }
  }
};
</script>
