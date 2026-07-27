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
          alt="Register illustration"
          class="absolute inset-0 w-full h-full object-cover"
        />
      </div>

      <!-- 右侧注册表单 -->
      <div class="w-full lg:w-1/2 px-8 py-12 sm:px-12">
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-gray-800">欢迎加入</h2>
          <p class="mt-2 text-sm text-gray-600">创建你的账号</p>
        </div>

        <el-form
          ref="registerForm"
          :model="registerForm"
          :rules="registerRules"
          class="space-y-6"
        >
          <el-form-item prop="username">
            <el-input
              v-model.trim="registerForm.username"
              placeholder="用户名"
              prefix-icon="el-icon-user"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model.trim="registerForm.email"
              placeholder="邮箱"
              prefix-icon="el-icon-message"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model.trim="registerForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="el-icon-lock"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model.trim="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              prefix-icon="el-icon-lock"
              show-password
              class="custom-input"
            />
          </el-form-item>
          <el-button
            type="primary"
            class="w-full"
            :loading="loading"
            @click="handleRegister"
          >
            立即注册
          </el-button>

          <div class="text-center text-sm text-gray-600">
            已有账号？
            <el-button
              type="text"
              class="text-blue-600 hover:text-blue-800"
              @click="$router.push('/login')"
            >
              立即登录
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import {
  emailValidator,
  passwordValidator,
  usernameValidator
} from "@/enums/validator";
import { register } from "@/api/login/login";
import { meMsgSuccess } from "@/utils/modal";
import defaultSettings from "@/settings";

export default {
  name: "Register",
  data() {
    const confirmPasswordValidator = (rule, value, callback) => {
      if (value) {
        if (this.registerForm.password !== value) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      } else {
        callback(new Error("请再次输入密码"));
      }
    };
    return {
      title: defaultSettings.title,
      defaultRegisterForm: {},
      registerForm: {
        username: "",
        email: "",
        password: "",
        confirmPassword: ""
      },
      registerRules: {
        username: [
          { required: true, message: "请输入账号", trigger: "blur" },
          { validator: usernameValidator, trigger: "blur" }
        ],
        email: [
          { required: false, trigger: "blur", message: "请输入邮箱" },
          { validator: emailValidator, trigger: "blur" }
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { validator: passwordValidator, trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: "请再次输入密码", trigger: "blur" },
          { validator: confirmPasswordValidator, trigger: "blur" }
        ]
      },
      loading: false
    };
  },
  created() {
    this.defaultRegisterForm = this.$deepCopy(this.registerForm);
  },
  mounted() {
    this.initForm();
  },
  methods: {
    initForm() {
      this.registerForm = this.$deepCopy(this.defaultRegisterForm);
    },
    handleRegister() {
      this.$refs.registerForm.validate(async valid => {
        if (!valid) return;
        this.loading = true;
        try {
          // 这里调用注册API
          await register({
            username: this.registerForm.username,
            email: this.registerForm.email,
            password: this.registerForm.password
          });
          meMsgSuccess({
            message: "注册成功，请登录"
          });
          await this.$router.push("/login");
        } catch (error) {
          console.error("注册失败:", error);
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
</style>
