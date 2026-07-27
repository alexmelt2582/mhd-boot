<template>
  <div class="bg-white rounded-lg shadow-sm p-6">
    <h2 class="text-xl font-bold text-gray-900 mb-6">修改密码</h2>
    <el-form
      ref="passwordForm"
      :model="passwordForm"
      :rules="passwordRules"
      label-width="100px"
      class="space-y-6"
    >
      <el-form-item label="当前密码" prop="oldPassword">
        <el-input
          v-model="passwordForm.oldPassword"
          type="password"
          placeholder="请输入当前密码"
          show-password
        ></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
        ></el-input>
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="handleUpdatePassword"
          :loading="passwordLoading"
        >
          确认修改
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { passwordValidator } from "@/enums/validator";
import { updatePassword } from "@/api/front/base/user";
import { meMsgSuccess } from "@/utils/modal";

export default {
  name: "ProfileChangePassword",
  data() {
    const confirmPasswordValidator = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };
    return {
      // 密码修改表单
      passwordForm: {
        oldPassword: "",
        newPassword: "",
        confirmPassword: ""
      },
      // 密码验证规则
      passwordRules: {
        oldPassword: [
          { required: true, message: "请输入当前密码", trigger: "blur" }
        ],
        newPassword: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          { validator: passwordValidator, trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: "请再次输入新密码", trigger: "blur" },
          { validator: confirmPasswordValidator, trigger: "blur" }
        ]
      },
      passwordLoading: false
    };
  },
  methods: {
    // 修改密码
    async handleUpdatePassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (!valid) return;
        this.passwordLoading = true;
        try {
          const formData = new FormData();
          formData.append("oldPass", this.editPassForm.oldPass);
          formData.append("newPass", this.editPassForm.newPass);
          await updatePassword(formData);
          meMsgSuccess({ message: "密码修改成功" });
          this.passwordForm = {
            oldPassword: "",
            newPassword: "",
            confirmPassword: ""
          };
        } catch (error) {
          console.error("修改密码失败:", error);
        } finally {
          this.passwordLoading = false;
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped></style>
