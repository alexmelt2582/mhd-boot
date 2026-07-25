<template>
  <div class="app-main-container">
    <el-row :gutter="20">
      <el-col
        :xs="24"
        :sm="24"
        :md="8"
        :lg="6"
        :xl="5"
        style="margin-bottom: 10px"
      >
        <el-card>
          <div slot="header">
            <span>个人信息</span>
          </div>
          <div style="text-align: center">
            <img
              :src="attachImageUrl(userInfo.avatar)"
              class="avatar"
              alt=""
              title="点击上传头像"
              @click="handleUploadAvatar"
            />
          </div>
          <ul class="user-info">
            <li>
              <i class="el-icon-user" />
              登录账号
              <div class="user-right">
                {{ userInfo.username }}
              </div>
            </li>
            <li>
              <i class="el-icon-message" />
              邮箱
              <div class="user-right">
                {{ userInfo.email }}
              </div>
            </li>
            <li>
              <i class="el-icon-phone-outline" />
              手机号码
              <div class="user-right">
                {{ userInfo.mobile }}
              </div>
            </li>
          </ul>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="16" :lg="18" :xl="19">
        <el-card>
          <el-tabs v-model="activeName">
            <el-tab-pane label="用户资料" name="first">
              <el-form
                ref="formRef"
                :model="form"
                :rules="rules"
                style="margin-top: 10px"
                label-width="65px"
              >
                <el-form-item label="账号" prop="username">
                  <el-input v-model="form.username" style="width: 35%" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" style="width: 35%" />
                  <span style="color: #c0c0c0; margin-left: 10px"
                    >邮箱不能重复</span
                  >
                </el-form-item>
                <el-form-item label="手机号" prop="mobile">
                  <el-input v-model="form.mobile" style="width: 35%" />
                  <span style="color: #c0c0c0; margin-left: 10px"
                    >手机号码不能重复</span
                  >
                </el-form-item>
                <el-form-item label="">
                  <el-button
                    :loading="saveLoading"
                    type="primary"
                    @click="handleSubmit"
                    >保存配置
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
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
import imgMixin from "@/mixins/img";
import { updateAvatar, updateBaseUser } from "@/api/admin/base/user";
import { meMsgSuccess } from "@/utils/modal";
import { usernameValidator, phoneValidator } from "@/enums/validator";

export default {
  name: "SystemUserCenter",
  mixins: [imgMixin],
  mounted() {
    this.init();
  },
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
      activeName: "first",
      saveLoading: false,
      userInfo: {},
      form: {},
      rules: {
        username: [
          { required: true, message: "请输入账号", trigger: "blur" },
          { validator: usernameValidator, trigger: "blur" }
        ],
        mobile: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          { validator: phoneValidator, trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    async init() {
      let res = await this.$store.dispatch("user/getUserInfo");
      this.userInfo = res.data;
      let userInfo = res.data;
      this.form = {
        id: userInfo.id,
        username: userInfo.username,
        email: userInfo.email,
        mobile: userInfo.mobile
      };
    },
    handleSubmit() {
      const that = this;
      this.$refs.formRef.validate(valid => {
        if (valid) {
          that.saveLoading = true;
          updateBaseUser(this.form)
            .then(() => {
              meMsgSuccess({
                message: "修改成功"
              });
              that.$store.dispatch("user/getUserInfo").then(() => {});
            })
            .finally(() => {
              this.saveLoading = false;
            });
        }
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
      updateAvatar(formData).then(() => {
        meMsgSuccess({
          message: "上传成功"
        });
        that.avatarReset();
      });
    },
    handleAvatarClose() {
      this.avatarOpen = false;
      this.avatarReset();
    }
  }
};
</script>

<style lang="scss" scoped>
.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
}

.user-info {
  padding-left: 0;
  list-style: none;

  li {
    border-bottom: 1px solid #f0f3f4;
    padding: 11px 0;
    font-size: 13px;
  }

  .user-right {
    float: right;

    a {
      color: #317ef3;
    }
  }
}
</style>
