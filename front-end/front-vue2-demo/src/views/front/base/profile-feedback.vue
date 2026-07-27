<template>
  <div class="bg-white rounded-lg shadow-sm p-6">
    <h2 class="text-xl font-bold text-gray-900 mb-6">意见反馈</h2>
    <el-form
      ref="feedbackForm"
      :model="feedbackForm"
      :rules="feedbackRules"
      label-width="100px"
      class="space-y-6"
    >
      <el-form-item label="反馈类型" prop="feedbackType">
        <el-select
          v-model="feedbackForm.feedbackType"
          placeholder="请选择反馈类型"
          class="w-full"
        >
          <el-option
            v-for="(item, index) in feedBackEnum"
            :key="index"
            :label="item.label"
            :value="item.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="反馈标题" prop="title">
        <el-input
          v-model="feedbackForm.title"
          placeholder="请输入反馈标题"
        ></el-input>
      </el-form-item>
      <el-form-item label="反馈内容" prop="content">
        <el-input
          v-model="feedbackForm.content"
          type="textarea"
          :rows="6"
          placeholder="请详细描述您的意见或建议"
        ></el-input>
      </el-form-item>
      <el-form-item label="联系方式" prop="contactInfo">
        <el-input
          v-model="feedbackForm.contactInfo"
          placeholder="请输入您的联系方式（可选）"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="handleSubmitFeedback"
          :loading="feedbackLoading"
        >
          提交反馈
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { meMsgSuccess } from "@/utils/modal";
import { submitFeedback } from "@/api/front/base/feedback";

export default {
  name: "ProfileFeedback",
  data() {
    const feedBackEnum = [
      { label: "建议", value: 1 },
      { label: "Bug反馈", value: 2 },
      { label: "举报", value: 3 }
    ];
    return {
      feedBackEnum,
      // 意见反馈表单
      feedbackForm: {
        feedbackType: null,
        title: "",
        content: "",
        contactInfo: ""
      },
      // 反馈验证规则
      feedbackRules: {
        feedbackType: [
          { required: true, message: "请选择反馈类型", trigger: "change" }
        ],
        title: [{ required: true, message: "请输入反馈标题", trigger: "blur" }],
        content: [
          { required: true, message: "请输入反馈内容", trigger: "blur" },
          { min: 10, message: "反馈内容不能少于10个字符", trigger: "blur" }
        ]
      },
      feedbackLoading: false
    };
  },
  methods: {
    // 提交反馈
    async handleSubmitFeedback() {
      this.$refs.feedbackForm.validate(async valid => {
        if (!valid) return;
        this.feedbackLoading = true;
        try {
          await submitFeedback(this.feedbackForm);
          meMsgSuccess({ message: "反馈提交成功，感谢您的建议" });
          await this.fetchUserInfo();
          this.feedbackForm = {
            feedbackType: null,
            title: "",
            content: "",
            contactInfo: ""
          };
        } catch (error) {
          console.error("提交反馈失败:", error);
        } finally {
          this.feedbackLoading = false;
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped></style>
