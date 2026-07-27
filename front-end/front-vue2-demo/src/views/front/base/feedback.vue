<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="bg-white rounded-lg shadow-sm">
      <!-- 页面头部 -->
      <div class="px-6 py-4 border-b border-gray-200">
        <div class="flex justify-between items-center">
          <h1 class="text-2xl font-bold text-gray-900">我的反馈</h1>
          <el-button type="primary" @click="handleSubmitFeedback">
            <i class="el-icon-plus mr-1"></i>
            提交反馈
          </el-button>
        </div>
      </div>
      <!-- 搜索筛选 -->
      <div class="px-6 py-4 border-b border-gray-200">
        <el-form :model="searchForm" inline>
          <el-form-item label="反馈类型">
            <el-select
              v-model="searchForm.feedbackType"
              placeholder="全部类型"
              clearable
            >
              <el-option label="建议" :value="1"></el-option>
              <el-option label="Bug反馈" :value="2"></el-option>
              <el-option label="举报" :value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态">
            <el-select
              v-model="searchForm.status"
              placeholder="全部状态"
              clearable
            >
              <el-option label="待处理" :value="1"></el-option>
              <el-option label="处理中" :value="2"></el-option>
              <el-option label="已解决" :value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleResetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <!-- 反馈列表 -->
      <div class="p-6" v-loading="loading">
        <div
          v-if="feedbackList.length === 0"
          class="text-center py-12 text-gray-500"
        >
          <i class="el-icon-document text-4xl mb-4"></i>
          <p>暂无反馈记录</p>
        </div>
        <div v-else class="space-y-4">
          <div
            v-for="item in feedbackList"
            :key="item.id"
            class="
              border
              rounded-lg
              p-6
              hover:shadow-md
              transition-shadow
              cursor-pointer
            "
            @click="handleViewDetail(item)"
          >
            <div class="flex justify-between items-start mb-3">
              <h3 class="text-lg font-medium text-gray-900">
                {{ item.title }}
              </h3>
              <el-tag :type="getStatusTagType(item.status)">
                {{ item.statusDesc }}
              </el-tag>
            </div>
            <div class="flex justify-between items-center mb-3">
              <el-tag :type="getTypeTagType(item.feedbackType)" size="small">
                {{ item.feedbackTypeDesc }}
              </el-tag>
              <span class="text-sm text-gray-500">{{ item.createTime }}</span>
            </div>
            <p class="text-gray-600 line-clamp-2">{{ item.content }}</p>
          </div>
        </div>
        <!-- 分页 -->
        <div v-if="total > 0" class="flex justify-center mt-6">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="queryParams.pageNo"
            :page-sizes="[10, 20, 50]"
            :page-size="queryParams.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
          />
        </div>
      </div>
    </div>
    <!-- 反馈详情对话框 -->
    <el-dialog
      title="反馈详情"
      :visible.sync="detailVisible"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="currentDetail" class="space-y-4">
        <div class="flex justify-between items-start">
          <h3 class="text-lg font-medium">{{ currentDetail.title }}</h3>
          <el-tag :type="getStatusTagType(currentDetail.status)">
            {{ currentDetail.statusDesc }}
          </el-tag>
        </div>
        <div class="flex items-center space-x-4">
          <el-tag
            :type="getTypeTagType(currentDetail.feedbackType)"
            size="small"
          >
            {{ currentDetail.feedbackTypeDesc }}
          </el-tag>
          <span class="text-sm text-gray-500"
            >提交时间：{{ currentDetail.createTime }}</span
          >
        </div>
        <div>
          <h4 class="font-medium mb-2">反馈内容：</h4>
          <div class="bg-gray-50 p-3 rounded text-sm whitespace-pre-wrap">
            {{ currentDetail.content }}
          </div>
        </div>
        <div v-if="currentDetail.contactInfo">
          <h4 class="font-medium mb-2">联系方式：</h4>
          <p class="text-sm text-gray-600">{{ currentDetail.contactInfo }}</p>
        </div>
        <div v-if="currentDetail.processTime">
          <h4 class="font-medium mb-2">处理时间：</h4>
          <p class="text-sm text-gray-600">{{ currentDetail.processTime }}</p>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </div>
    </el-dialog>
    <!-- 提交反馈对话框 -->
    <el-dialog
      title="提交反馈"
      :visible.sync="submitVisible"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="submitForm"
        :model="submitForm"
        :rules="submitRules"
        label-width="80px"
      >
        <el-form-item label="反馈类型" prop="feedbackType">
          <el-select
            v-model="submitForm.feedbackType"
            placeholder="请选择反馈类型"
            class="w-full"
          >
            <el-option label="建议" :value="1"></el-option>
            <el-option label="Bug反馈" :value="2"></el-option>
            <el-option label="举报" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="反馈标题" prop="title">
          <el-input
            v-model="submitForm.title"
            placeholder="请输入反馈标题"
          ></el-input>
        </el-form-item>
        <el-form-item label="反馈内容" prop="content">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="5"
            placeholder="请详细描述您的反馈内容"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input
            v-model="submitForm.contactInfo"
            placeholder="请输入您的联系方式（可选）"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleConfirmSubmit"
        >
          确定提交
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMyFeedbackPage,
  getMyFeedbackDetail,
  submitFeedback
} from "@/api/front/base/feedback";
import { meMsgSuccess } from "@/utils/modal";

export default {
  name: "FrontFeedback",
  data() {
    return {
      loading: false, // 列表加载状态
      feedbackList: [], // 反馈列表
      total: 0, // 总数
      queryParams: {
        // 查询参数
        pageNo: 1,
        pageSize: 10
      },
      searchForm: {
        // 搜索表单
        feedbackType: null,
        status: null
      },
      detailVisible: false, // 详情对话框显示状态
      currentDetail: null, // 当前查看的详情
      submitVisible: false, // 提交对话框显示状态
      submitLoading: false, // 提交加载状态
      submitForm: {
        // 提交表单
        feedbackType: null,
        title: "",
        content: "",
        contactInfo: ""
      },
      submitRules: {
        // 提交表单验证规则
        feedbackType: [
          { required: true, message: "请选择反馈类型", trigger: "change" }
        ],
        title: [{ required: true, message: "请输入反馈标题", trigger: "blur" }],
        content: [
          { required: true, message: "请输入反馈内容", trigger: "blur" },
          { min: 10, message: "反馈内容不能少于10个字符", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.fetchList();
  },
  methods: {
    // 获取反馈列表
    async fetchList() {
      try {
        this.loading = true;
        const params = {
          ...this.queryParams,
          ...this.searchForm
        };
        const res = await getMyFeedbackPage(params);
        this.feedbackList = res.list || [];
        this.total = res.total || 0;
      } catch (error) {
        console.error("获取反馈列表失败:", error);
      } finally {
        this.loading = false;
      }
    },
    // 搜索
    handleSearch() {
      this.queryParams.pageNo = 1;
      this.fetchList();
    },
    // 重置搜索
    handleResetSearch() {
      this.searchForm = {
        feedbackType: null,
        status: null
      };
      this.handleSearch();
    },
    // 查看详情
    async handleViewDetail(item) {
      try {
        const res = await getMyFeedbackDetail(item.id);
        this.currentDetail = res.data;
        this.detailVisible = true;
      } catch (error) {
        console.error("获取反馈详情失败:", error);
      }
    },
    // 提交反馈
    handleSubmitFeedback() {
      this.submitForm = {
        feedbackType: null,
        title: "",
        content: "",
        contactInfo: ""
      };
      this.submitVisible = true;
      this.$nextTick(() => {
        this.$refs.submitForm?.clearValidate();
      });
    },
    // 确认提交
    async handleConfirmSubmit() {
      try {
        await this.$refs.submitForm.validate();
        this.submitLoading = true;
        await submitFeedback(this.submitForm);
        meMsgSuccess({ message: "反馈提交成功" });
        this.submitVisible = false;
        this.fetchList();
      } catch (error) {
        if (error !== false) {
          console.error("提交反馈失败:", error);
        }
      } finally {
        this.submitLoading = false;
      }
    },
    // 分页大小变更
    handleSizeChange(val) {
      this.queryParams.pageSize = val;
      this.queryParams.pageNo = 1;
      this.fetchList();
    },
    // 当前页变更
    handleCurrentChange(val) {
      this.queryParams.pageNo = val;
      this.fetchList();
    },
    // 获取类型标签样式
    getTypeTagType(type) {
      const typeMap = {
        1: "info", // 建议
        2: "warning", // Bug反馈
        3: "danger" // 举报
      };
      return typeMap[type] || "info";
    },
    // 获取状态标签样式
    getStatusTagType(status) {
      const statusMap = {
        1: "warning", // 待处理
        2: "primary", // 处理中
        3: "success" // 已解决
      };
      return statusMap[status] || "info";
    }
  }
};
</script>

<style lang="scss" scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
