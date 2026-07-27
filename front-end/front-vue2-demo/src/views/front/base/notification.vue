<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- 标签页切换和操作按钮 -->
    <div class="flex justify-between items-center mb-6">
      <div class="flex space-x-1 bg-gray-100 p-1 rounded-lg">
        <div
          v-for="tab in tabs"
          :key="tab.value"
          class="
            flex-1
            py-2
            px-4
            text-gray-600
            hover:text-gray-800
            cursor-pointer
          "
          :class="{
            'rounded-md bg-white shadow-sm': activeTab === tab.value
          }"
          @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>
      <!-- 全部标记为已读按钮 -->
      <el-button
        v-if="hasUnreadMessages"
        type="primary"
        size="small"
        @click="handleMarkAllRead"
        :loading="markAllLoading"
      >
        全部标记为已读
      </el-button>
    </div>

    <!-- 消息列表 -->
    <div class="space-y-4" v-loading="loading">
      <div
        v-for="message in messageList"
        :key="message.id"
        class="message-item"
      >
        <div
          class="bg-white rounded-lg shadow-sm p-4 cursor-pointer hover:shadow-md transition-shadow"
          :class="{ 'opacity-75': message.readStatus === 1 }"
          @click="handleViewDetail(message)"
        >
          <div class="flex items-start">
            <div class="flex-shrink-0" v-if="message.readStatus === 0">
              <div class="w-2 h-2 mt-2 bg-blue-500 rounded-full"></div>
            </div>
            <div
              class="flex-1"
              :class="message.readStatus === 1 ? 'ml-6' : 'ml-4'"
            >
              <div class="flex items-center justify-between">
                <h3
                  class="text-lg font-medium"
                  :class="
                    message.readStatus === 1 ? 'text-gray-700' : 'text-gray-900'
                  "
                >
                  {{ message.messageTypeDesc }}
                </h3>
                <div class="flex items-center space-x-2">
                  <span class="text-sm text-gray-500">{{
                    timeAgo(message.sendTime)
                  }}</span>
                  <el-button
                    v-if="message.readStatus === 0"
                    type="text"
                    size="mini"
                    @click.stop="handleMarkAsRead(message)"
                    class="text-blue-500 hover:text-blue-700"
                  >
                    标记已读
                  </el-button>
                </div>
              </div>
              <p class="mt-1 text-gray-600">{{ message.content }}</p>
            </div>
          </div>
        </div>
      </div>
      <!-- 空状态 -->
      <div
        v-if="!loading && messageList.length === 0"
        class="text-center py-12"
      >
        <div class="text-meHintText text-lg">暂无消息</div>
      </div>
    </div>
    <!-- 加载更多 -->
    <div class="mt-8 text-center" v-if="hasMore">
      <span
        @click="handleLoadMore"
        class="
          px-6
          py-2
          border border-gray-300
          rounded-md
          text-gray-600
          hover:bg-gray-50
        "
        >加载更多</span
      >
    </div>

    <!-- 消息详情对话框 -->
    <el-dialog
      title="消息详情"
      :visible.sync="detailDialogVisible"
      width="600px"
      :before-close="handleCloseDetail"
    >
      <div v-if="currentMessage" class="message-detail">
        <div class="detail-header mb-4">
          <h3 class="text-lg font-semibold text-gray-900">
            {{ currentMessage.messageTypeDesc }}
          </h3>
          <p class="text-sm text-gray-500 mt-1">
            {{ timeAgo(currentMessage.sendTime) }}
          </p>
        </div>
        <div class="detail-content">
          <div class="bg-gray-50 p-4 rounded-lg">
            <p class="text-gray-800 leading-relaxed">
              {{ currentMessage.content }}
            </p>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleCloseDetail">关闭</el-button>
        <el-button
          v-if="currentMessage && currentMessage.readStatus === 0"
          type="primary"
          @click="handleMarkAsReadInDetail"
        >
          标记已读
        </el-button>
        <el-button
          type="danger"
          @click="handleDeleteInDetail"
          :loading="deleteLoading"
        >
          删除消息
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { timeAgo } from "@/utils/date";
import { meMsgSuccess } from "@/utils/modal";
import utilMixin from "@/mixins/util";
import {
  getMyNotifications,
  getMyNotificationDetail,
  markAsRead,
  markAllAsRead,
  deleteMyNotifications
} from "@/api/front/base/notification";

export default {
  name: "Message",
  mixins: [utilMixin],
  data() {
    return {
      loading: false, // 加载状态
      markAllLoading: false, // 全部标记为已读加载状态
      deleteLoading: false, // 删除加载状态
      activeTab: "all", // 当前标签页
      hoveredMessageId: null, // 鼠标悬浮的消息ID
      messageList: [], // 消息列表
      hasMore: false, // 是否有更多数据
      total: 0, // 总数量
      detailDialogVisible: false, // 详情对话框显示状态
      currentMessage: null, // 当前查看的消息
      sortingFields: ["read_status:asc", "id:desc"],
      queryParams: {
        pageNo: 1,
        pageSize: 10
      },
      // 标签页配置
      tabs: [
        { label: "全部", value: "all" },
        { label: "未读", value: 0 },
        { label: "已读", value: 1 }
      ]
    };
  },
  computed: {
    // 是否有未读消息
    hasUnreadMessages() {
      return this.messageList.some(message => message.readStatus === 0);
    }
  },
  created() {
    this.fetchMessageList();
  },
  methods: {
    timeAgo,
    // 获取消息列表
    async fetchMessageList() {
      try {
        this.loading = true;
        const readStatus = this.activeTab === "all" ? null : this.activeTab;
        const params = Object.assign(
          { sortingFields: this.sortingFields },
          this.queryParams,
          { readStatus }
        );
        const { list, total } = await getMyNotifications(params);
        this.messageList = [...this.messageList, ...(list || [])];
        this.total = total || 0;
        this.hasMore =
          this.queryParams.pageNo * this.queryParams.pageSize < this.total;
      } catch (error) {
        console.error("获取消息列表失败:", error);
      } finally {
        this.loading = false;
      }
    },
    // 标签页切换
    handleTabChange(tab) {
      this.activeTab = tab;
      this.queryParams.pageNo = 1;
      this.messageList = [];
      this.fetchMessageList();
    },

    // 删除消息
    async handleDeleteMessage(messageId) {
      try {
        await this.$confirm("确定要删除这条消息吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        });
        await deleteMyNotifications([messageId]);
        meMsgSuccess({ message: "删除成功" });
      } catch (error) {
        console.error("删除消息失败:", error);
      } finally {
        this.queryParams.pageNo = 1;
        this.messageList = [];
        await this.fetchMessageList();
      }
    },
    // 加载更多
    handleLoadMore() {
      this.queryParams.pageNo = this.queryParams.pageNo + 1;
      this.fetchMessageList();
    },
    // 标记单条消息为已读
    async handleMarkAsRead(message) {
      if (message.readStatus === 1) return; // 已读消息不需要处理
      try {
        await markAsRead({ notificationIds: [message.id] });
        // 更新本地状态
        message.readStatus = 1;
        meMsgSuccess({ message: "标记已读成功" });
      } catch (error) {
        console.error("标记已读失败:", error);
      }
    },
    // 全部标记为已读
    async handleMarkAllRead() {
      try {
        this.markAllLoading = true;
        await markAllAsRead();
        // 更新本地状态
        this.messageList.forEach(message => {
          if (message.readStatus === 0) {
            message.readStatus = 1;
          }
        });
        meMsgSuccess({ message: "全部标记已读成功" });
      } catch (error) {
        console.error("全部标记已读失败:", error);
      } finally {
        this.markAllLoading = false;
      }
    },
    // 查看消息详情
    async handleViewDetail(message) {
      try {
        const { data } = await getMyNotificationDetail(message.id);
        this.currentMessage = data;
        this.detailDialogVisible = true;
        // 如果是未读消息，自动标记为已读
        if (message.readStatus === 0) {
          await this.handleMarkAsRead(message);
        }
      } catch (error) {
        console.error("获取消息详情失败:", error);
      }
    },
    // 关闭详情对话框
    handleCloseDetail() {
      this.detailDialogVisible = false;
      this.currentMessage = null;
    },
    // 在详情中标记已读
    async handleMarkAsReadInDetail() {
      if (this.currentMessage) {
        await this.handleMarkAsRead(this.currentMessage);
        this.currentMessage.readStatus = 1;
      }
    },
    // 在详情中删除消息
    async handleDeleteInDetail() {
      if (!this.currentMessage) return;

      try {
        await this.$confirm("确定要删除这条消息吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        });

        this.deleteLoading = true;
        await deleteMyNotifications([this.currentMessage.id]);
        meMsgSuccess({ message: "删除成功" });

        // 关闭详情对话框
        this.handleCloseDetail();

        // 刷新消息列表
        this.queryParams.pageNo = 1;
        this.messageList = [];
        await this.fetchMessageList();
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除消息失败:", error);
        }
      } finally {
        this.deleteLoading = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.message-item {
  margin-bottom: 1rem;
}

.message-detail {
  .detail-header {
    border-bottom: 1px solid #e5e7eb;
    padding-bottom: 1rem;
  }

  .detail-content {
    margin-top: 1rem;
  }
}

.dialog-footer {
  .el-button {
    margin-left: 8px;
  }
}
</style>
