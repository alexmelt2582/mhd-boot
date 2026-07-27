<template>
  <div class="bg-white rounded-lg shadow-sm p-6">
    <h2 class="text-xl font-bold text-gray-900 mb-6">基本信息</h2>
    <el-form
      ref="basicForm"
      :model="basicForm"
      :rules="basicRules"
      label-width="100px"
      class="space-y-6"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="姓名" prop="username">
            <el-input
              v-model="basicForm.username"
              disabled
              placeholder="请输入姓名"
            ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-select
              v-model="basicForm.gender"
              placeholder="请选择性别"
              class="w-full"
            >
              <el-option label="男" :value="1"></el-option>
              <el-option label="女" :value="2"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="手机号" prop="mobile">
            <el-input
              v-model="basicForm.mobile"
              placeholder="请输入手机号"
            ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="basicForm.email"
              placeholder="请输入邮箱"
            ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="个人简介" prop="introduction">
        <el-input
          v-model="basicForm.introduction"
          type="textarea"
          :rows="4"
          placeholder="请输入个人简介"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="handleUpdateBasicInfo"
          :loading="basicLoading"
        >
          保存修改
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {
  emailValidator,
  phoneValidator,
  usernameValidator
} from "@/enums/validator";
import { meMsgSuccess } from "@/utils/modal";
import { getCurrentUserInfo, updateUserInfo } from "@/api/front/base/user";

export default {
  name: "ProfileBasicInfo",
  data() {
    return {
      // 基本信息表单
      basicForm: {
        id: undefined,
        username: "",
        gender: null,
        mobile: "",
        email: "",
        introduction: ""
      },
      // 基本信息验证规则
      basicRules: {
        username: [
          { required: true, message: "请输入账号", trigger: "blur" },
          { validator: usernameValidator, trigger: "blur" }
        ],
        email: [{ validator: emailValidator, trigger: "blur" }],
        mobile: [{ validator: phoneValidator, trigger: "blur" }]
      },
      basicLoading: false
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    async init() {
      // 初始化时获取用户信息
      await this.fetchUserInfo();
    },
    // 获取用户信息
    async fetchUserInfo() {
      try {
        const { data } = await getCurrentUserInfo();
        // 填充基本信息表单
        this.basicForm = {
          id: data.id,
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
    // 更新基本信息
    async handleUpdateBasicInfo() {
      this.$refs.basicForm.validate(async valid => {
        if (!valid) return;
        this.basicLoading = true;
        try {
          await updateUserInfo(this.basicForm);
          meMsgSuccess({ message: "基本信息更新成功" });
          await this.fetchUserInfo();
        } catch (error) {
          console.error("更新基本信息失败:", error);
        } finally {
          this.basicLoading = false;
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped></style>
