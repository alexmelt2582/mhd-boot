<template>
  <div class="bg-white rounded-lg shadow-sm p-6 overflow-auto">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-900">我的反馈</h2>
      <el-button type="primary" size="small" @click="handleRefreshFeedback">
        刷新
      </el-button>
    </div>
    <div v-loading="myFeedbackLoading">
      <div
        v-if="myFeedbackList.length === 0"
        class="text-center py-8 text-gray-500"
      >
        暂无反馈记录
      </div>
      <div v-else class="space-y-4">
        <div
          v-for="item in myFeedbackList"
          :key="item.id"
          class="border rounded-lg p-4 hover:shadow-sm transition-shadow"
        >
          <div class="flex justify-between items-start mb-2">
            <h3 class="font-medium text-gray-900">{{ item.title }}</h3>
            <el-tag :type="getFeedbackStatusTagType(item.status)" size="small">
              {{ item.statusDesc }}
            </el-tag>
          </div>
          <div class="flex justify-between items-center mb-2">
            <el-tag
              :type="getFeedbackTypeTagType(item.feedbackType)"
              size="mini"
            >
              {{ item.feedbackTypeDesc }}
            </el-tag>
            <span class="text-sm text-gray-500">{{ item.createTime }}</span>
          </div>
          <p class="text-gray-600 text-sm line-clamp-2">
            {{ item.content }}
          </p>
          <div class="mt-3 flex justify-end">
            <el-button
              type="text"
              size="small"
              @click="handleViewFeedbackDetail(item)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </div>
      <div
        v-if="myFeedbackTotal > myFeedbackList.length"
        class="text-center mt-4"
      >
        <el-button @click="handleLoadMoreFeedback" :loading="loadMoreLoading">
          加载更多
        </el-button>
      </div>
    </div>
    <!-- 反馈详情对话框 -->
    <el-dialog
      title="反馈详情"
      :visible.sync="feedbackDetailVisible"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="currentFeedbackDetail" class="space-y-4">
        <div class="flex justify-between items-start">
          <h3 class="text-lg font-medium">{{ currentFeedbackDetail.title }}</h3>
          <el-tag
            :type="getFeedbackStatusTagType(currentFeedbackDetail.status)"
          >
            {{ currentFeedbackDetail.statusDesc }}
          </el-tag>
        </div>
        <div class="flex items-center space-x-4">
          <el-tag
            :type="getFeedbackTypeTagType(currentFeedbackDetail.feedbackType)"
            size="small"
          >
            {{ currentFeedbackDetail.feedbackTypeDesc }}
          </el-tag>
          <span class="text-sm text-gray-500"
            >提交时间：{{ currentFeedbackDetail.createTime }}</span
          >
        </div>
        <div>
          <h4 class="font-medium mb-2">反馈内容：</h4>
          <div class="bg-gray-50 p-3 rounded text-sm">
            {{ currentFeedbackDetail.content }}
          </div>
        </div>
        <div v-if="currentFeedbackDetail.contactInfo">
          <h4 class="font-medium mb-2">联系方式：</h4>
          <p class="text-sm text-gray-600">
            {{ currentFeedbackDetail.contactInfo }}
          </p>
        </div>
        <div v-if="currentFeedbackDetail.processTime">
          <h4 class="font-medium mb-2">处理时间：</h4>
          <p class="text-sm text-gray-600">
            {{ currentFeedbackDetail.processTime }}
          </p>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="feedbackDetailVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMyFeedbackDetail,
  getMyFeedbackPage
} from "@/api/front/base/feedback";

export default {
  name: "ProfileFeedbackList",
  data() {
    return {
      myFeedbackPage: 1, // 当前页码
      myFeedbackPageSize: 10, // 每页大小
      myFeedbackList: [], // 我的反馈列表
      myFeedbackTotal: 0, // 我的反馈总数
      myFeedbackLoading: false, // 我的反馈加载状态
      loadMoreLoading: false, // 加载更多状态
      feedbackDetailVisible: false, // 反馈详情对话框显示状态
      currentFeedbackDetail: null // 当前查看的反馈详情
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    async init() {
      // 初始化时获取我的反馈列表
      await this.fetchMyFeedbackList();
    },
    // 刷新我的反馈
    handleRefreshFeedback() {
      this.fetchMyFeedbackList(true);
    },
    // 查看反馈详情
    async handleViewFeedbackDetail(item) {
      try {
        const res = await getMyFeedbackDetail(item.id);
        this.currentFeedbackDetail = res.data;
        this.feedbackDetailVisible = true;
      } catch (error) {
        console.error("获取反馈详情失败:", error);
      }
    },
    // 获取反馈类型标签样式
    getFeedbackTypeTagType(type) {
      const typeMap = {
        1: "info", // 建议
        2: "warning", // Bug反馈
        3: "danger" // 举报
      };
      return typeMap[type] || "info";
    },
    // 获取反馈状态标签样式
    getFeedbackStatusTagType(status) {
      const statusMap = {
        1: "warning", // 待处理
        2: "primary", // 处理中
        3: "success" // 已解决
      };
      return statusMap[status] || "info";
    },
    // 获取我的反馈列表
    async fetchMyFeedbackList(reset = true) {
      try {
        if (reset) {
          this.myFeedbackPage = 1;
          this.myFeedbackList = [];
        }
        this.myFeedbackLoading = true;
        const params = {
          pageNo: this.myFeedbackPage,
          pageSize: this.myFeedbackPageSize
        };
        const res = await getMyFeedbackPage(params);
        if (reset) {
          this.myFeedbackList = res.list || [];
        } else {
          this.myFeedbackList.push(...(res.list || []));
        }
        this.myFeedbackTotal = res.total || 0;
      } catch (error) {
        console.error("获取我的反馈列表失败:", error);
      } finally {
        this.myFeedbackLoading = false;
      }
    },
    // 加载更多反馈
    async handleLoadMoreFeedback() {
      try {
        this.loadMoreLoading = true;
        this.myFeedbackPage++;
        await this.fetchMyFeedbackList(false);
      } catch (error) {
        console.error("加载更多反馈失败:", error);
      } finally {
        this.loadMoreLoading = false;
      }
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
