<template>
  <div class="header flex items-center justify-center">
    <me-icon-svg
      :name="
        $store.state.settings.isCollapse ? 'el-icon-s-fold' : 'el-icon-s-unfold'
      "
      class="icon-btn"
      @click="
        $store.commit(
          'settings/setIsCollapse',
          !$store.state.settings.isCollapse
        )
      "
    />
    <div class="ml-auto flex justify-center items-center">
      <el-tooltip
        class="box-item"
        effect="dark"
        content="跳转前台"
        placement="bottom"
      >
        <me-icon-svg
          name="el-icon-monitor"
          class="icon-btn"
          @click="$router.push('/')"
        />
      </el-tooltip>
      <el-tooltip
        class="box-item"
        effect="dark"
        content="全屏"
        placement="bottom"
      >
        <me-icon-svg
          name="el-icon-full-screen"
          class="icon-btn"
          @click="clickFull"
        />
      </el-tooltip>
      <el-dropdown
        class="avatar-container right-menu-item hover-effect mx-5"
        trigger="click"
      >
        <span class="flex justify-center items-center">
          <el-avatar
            :size="30"
            :src="attachImageUrl(userInfo.avatar)"
            class="mr-2"
          />
          {{ userInfo.username }}
          <i class="el-icon-caret-bottom" />
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <span
              style="display: block"
              @click="$router.push('/admin/user/center')"
            >
              <el-dropdown-item> 个人中心 </el-dropdown-item>
            </span>
            <span style="display: block" @click="handleUpdatePass">
              <el-dropdown-item> 修改密码 </el-dropdown-item>
            </span>
            <span style="display: block" @click="handleLogout">
              <el-dropdown-item divided> 退出登录 </el-dropdown-item>
            </span>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <me-edit-form
      title="修改密码"
      edit-form-ref="editPassFormRef"
      :open.sync="editPassFormOpen"
      :edit-config="editPassConfig"
      :edit-form-rules="editPassRule"
      :edit-form-data="editPassForm"
      @handleEdit="handlePassSubmit"
      @handleReset="handlePassReset"
    />
  </div>
</template>

<script>
import screenfull from "screenfull";
import { meMsgSuccess, meMsgWarning } from "@/utils/modal";
import imgMixin from "@/mixins/img";
import { mapState } from "vuex";
import { passwordValidator } from "@/enums/validator";
import { updatePass } from "@/api/admin/base/user";

export default {
  name: "AdminHeader",
  mixins: [imgMixin],
  data() {
    const confirmPass = (rule, value, callback) => {
      if (value) {
        if (this.editPassForm.newPass !== value) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      } else {
        callback(new Error("请再次输入密码"));
      }
    };
    return {
      isFullscreen: false,
      // region 修改密码
      editPassFormOpen: false,
      editPassConfig: [
        { type: "text", label: "旧密码", field: "oldPass", showPassword: true },
        { type: "text", label: "新密码", field: "newPass", showPassword: true },
        {
          type: "text",
          label: "确认密码",
          field: "confirmPass",
          showPassword: true
        }
      ],
      editPassRule: {
        oldPass: [
          { required: true, message: "请输入旧密码", trigger: "blur" },
          {
            validator: passwordValidator,
            trigger: "blur"
          }
        ],
        newPass: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          {
            validator: passwordValidator,
            trigger: "blur"
          }
        ],
        confirmPass: [
          { required: true, message: "请再次输入密码", trigger: "blur" },
          { validator: confirmPass, trigger: "blur" }
        ]
      },
      defaultEditPassForm: {
        id: undefined,
        oldPass: undefined,
        newPass: undefined,
        confirmPass: undefined
      },
      editPassForm: {}
      // endregion
    };
  },
  computed: {
    ...mapState("user", ["userInfo"])
  },
  methods: {
    clickFull() {
      if (screenfull.isEnabled) {
        this.isFullscreen = !this.isFullscreen;
        screenfull.toggle();
      } else {
        meMsgWarning({
          message: "您的浏览器不支持全屏"
        });
        return false;
      }
    },
    handleLogout() {
      this.$confirm("确定注销并退出系统吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.$store.dispatch("user/HandleLogout").then(() => {
          setTimeout(() => {
            location.reload();
          }, 100);
        });
      });
    },
    // region 修改密码
    handleUpdatePass() {
      this.resetEditPassForm();
      this.editPassFormOpen = true;
    },
    handlePassSubmit(formRef) {
      const that = this;
      formRef.validate(valid => {
        if (valid) {
          // 提交
          const formData = new FormData();
          formData.append("oldPass", this.editPassForm.oldPass);
          formData.append("newPass", this.editPassForm.newPass);
          updatePass(formData)
            .then(() => {
              meMsgSuccess({
                message: "密码修改成功，请重新登录"
              });
              setTimeout(() => {
                that.$store.dispatch("user/HandleLogout").then(() => {
                  location.reload();
                });
              }, 1500);
            })
            .finally(() => {
              that.editPassFormOpen = false;
            });
        }
      });
    },
    handlePassReset(formRef) {
      this.editPassFormOpen = false;
      formRef.resetFields();
      formRef.clearValidate();
      this.resetEditPassForm();
    },
    resetEditPassForm() {
      this.editPassForm = JSON.parse(JSON.stringify(this.defaultEditPassForm));
    }
    // endregion
  }
};
</script>

<style lang="scss" scoped>
@import "@/assets/styles/variables.module";

.hamburger-container {
  line-height: $headerHeight;
  height: 100%;
  float: left;
  cursor: pointer;
  transition: background 0.3s;
  -webkit-tap-highlight-color: transparent;

  &:hover {
    background: rgba(0, 0, 0, 0.025);
  }
}

.header {
  height: $headerHeight;
  z-index: 100;
}

.icon-btn {
  @apply flex justify-center items-center;
  width: 42px;
  height: $headerHeight;
  cursor: pointer;
  color: #374151;
}

.icon-btn:hover {
  @apply bg-gray-200;
}

.header .dropdown {
  height: $headerHeight;
  cursor: pointer;
  color: #374151 !important;
}
</style>
