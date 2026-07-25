<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div
      class="
        max-w-4xl
        w-full
        flex
        rounded-lg
        shadow-lg
        overflow-hidden
        bg-white
      "
    >
      <!-- 左侧插图 -->
      <div class="hidden lg:block lg:w-1/2 bg-blue-100 relative">
        <img
          src="@/assets/images/login/login.png"
          alt="Login illustration"
          class="absolute inset-0 w-full h-full object-cover"
        />
      </div>

      <!-- 右侧登录表单 -->
      <div class="w-full lg:w-1/2 px-8 py-12 sm:px-12">
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-gray-800">Hi, 终于等到你</h2>
          <p class="mt-2 text-sm text-gray-600">{{ title }}</p>
        </div>

        <el-form
          ref="loginForm"
          :model="loginForm"
          :rules="loginRules"
          class="space-y-6"
        >
          <el-form-item prop="username">
            <el-input
              v-model.trim="loginForm.username"
              placeholder="用户名"
              prefix-icon="el-icon-user"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model.trim="loginForm.password"
              type="password"
              show-password
              placeholder="密码"
              prefix-icon="el-icon-lock"
              class="custom-input"
              @keyup.enter.native="handleLogin"
            />
          </el-form-item>

          <div class="flex items-center justify-between text-sm">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          </div>

          <el-button
            type="primary"
            class="w-full"
            :loading="loading"
            @click="handleLogin"
          >
            立即登录
          </el-button>

          <div class="text-center text-sm text-gray-600">
            还没有账号？
            <el-button
              type="text"
              class="text-blue-600 hover:text-blue-800"
              @click="$router.push('/register')"
            >
              立即注册
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { passwordValidator, usernameValidator } from "@/enums/validator";
import { meMsgSuccess } from "@/utils/modal";
import defaultSettings from "@/settings";

export default {
  name: "Login",
  data() {
    return {
      title: defaultSettings.title,
      defaultLoginForm: {},
      loginForm: {
        username: "",
        password: ""
      },
      loginRules: {
        username: [
          { required: true, message: "请输入账号", trigger: "blur" },
          { validator: usernameValidator, trigger: "blur" }
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { validator: passwordValidator, trigger: "blur" }
        ]
      },
      rememberMe: false,
      loading: false
    };
  },
  created() {
    this.defaultLoginForm = this.$deepCopy(this.loginForm);
  },
  mounted() {
    this.initForm();
  },
  methods: {
    initForm() {
      this.loginForm = this.$deepCopy(this.defaultLoginForm);
    },
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return;
        this.loading = true;
        try {
          // 这里调用登录API
          const res = await this.$store.dispatch(
            "user/HandleLogin",
            this.loginForm,
            this.rememberMe
          );
          if (res) {
            meMsgSuccess({ message: "登录成功" });
            await this.$router.push("/");
          }
        } catch (error) {
          console.error("登录失败:", error);
        } finally {
          this.loading = false;
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.custom-input {
  :deep(.el-input__inner) {
    height: 48px;
    line-height: 48px;
    font-size: 16px;
    border-radius: 8px;
    background-color: #f3f4f6;
    border: 2px solid transparent;

    &:focus {
      background-color: white;
      border-color: #60a5fa;
      box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.2);
    }
  }
}

.social-btn {
  display: flex;
  justify-content: center;
  width: 100%;
  padding: 0.5rem 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  background-color: white;
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;

  &:hover {
    background-color: #f9fafb;
  }

  i {
    font-size: 1.25rem;
  }
}
</style>
