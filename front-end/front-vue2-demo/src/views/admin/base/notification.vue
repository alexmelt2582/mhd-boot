<template>
  <div class="app-main-container">
    <me-search-form
      class="app-main-search"
      :show-search="showSearch"
      :search-params="searchParams"
      :search-config="searchConfig"
      @handleSearch="handleSearch"
      @handleReset="handleResetSearch"
    />
    <div class="app-main-content">
      <me-crud-button
        :show-search.sync="showSearch"
        :selections.sync="selections"
        :show-add-button="true"
        :show-update-button="false"
        @refresh="fetchList"
        @delete="handleBatchDelete"
        @add="handleAdd"
      />
      <me-table
        :loading="loading"
        row-key="id"
        :showSelection="true"
        :table-data="tableData"
        :table-columns="tableColumns"
        @handleSelectChange="handleSelectionChange"
      >
        <template #operate>
          <el-table-column
            label="操作"
            fixed="right"
            align="center"
            width="200"
            class-name="small-padding fixed-width"
          >
            <template v-slot="scope">
              <el-button
                type="text"
                size="small"
                @click="handleViewDetail(scope.row)"
              >
                查看
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </template>
      </me-table>
      <me-pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNo"
        :limit.sync="queryParams.pageSize"
        @pagination="fetchList"
      />
    </div>
    <!-- 通知详情对话框 -->
    <el-dialog
      title="通知详情"
      :visible.sync="detailDialogVisible"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="currentNotification" class="notification-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="通知ID"
            >{{ currentNotification.id }}
          </el-descriptions-item>
          <el-descriptions-item label="接收用户ID"
            >{{ currentNotification.userId || "未知用户" }}
          </el-descriptions-item>
          <el-descriptions-item label="消息类型">
            <el-tag
              :type="
                getElType(this.messageTypeEnum, currentNotification.messageType)
              "
              size="small"
            >
              {{ currentNotification.messageTypeDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="阅读状态">
            <el-tag
              :type="
                getElType(this.readStatusEnum, currentNotification.readStatus)
              "
              size="small"
            >
              {{ currentNotification.readStatusDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发送者"
            >{{ currentNotification.senderName || "系统" }}
          </el-descriptions-item>
          <el-descriptions-item label="发送时间"
            >{{ currentNotification.sendTime }}
          </el-descriptions-item>
          <el-descriptions-item label="通知标题" :span="2"
            >{{ currentNotification.title }}
          </el-descriptions-item>
          <el-descriptions-item label="通知内容" :span="2">
            <div class="notification-content-detail">
              {{ currentNotification.content }}
            </div>
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentNotification.relatedLink"
            label="关联链接"
            :span="2"
          >
            <a
              :href="currentNotification.relatedLink"
              target="_blank"
              class="text-blue-500"
            >
              {{ currentNotification.relatedLink }}
            </a>
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentNotification.readTime"
            label="阅读时间"
            :span="2"
          >
            {{ currentNotification.readTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
    <!-- 发送通知对话框 -->
    <el-dialog
      title="发送通知"
      :visible.sync="sendDialogVisible"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="sendForm"
        :model="sendForm"
        :rules="sendRules"
        label-width="100px"
      >
        <el-form-item label="消息类型" prop="messageType">
          <el-select
            v-model="sendForm.messageType"
            placeholder="请选择消息类型"
            class="w-full"
          >
            <el-option label="系统通知" value="system"></el-option>
            <el-option label="评论回复" value="comment_reply"></el-option>
            <el-option label="评论点赞" value="comment_like"></el-option>
            <el-option label="反馈回复" value="feedback_reply"></el-option>
            <el-option label="公告通知" value="announcement"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通知标题" prop="title">
          <el-input
            v-model="sendForm.title"
            placeholder="请输入通知标题"
          ></el-input>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="sendForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入通知内容"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="关联链接">
          <el-input
            v-model="sendForm.relatedLink"
            placeholder="请输入关联链接（可选）"
          ></el-input>
        </el-form-item>
        <el-form-item label="发送范围" prop="sendToAll">
          <el-radio-group v-model="sendForm.sendToAll">
            <el-radio :label="true">全部用户</el-radio>
            <el-radio :label="false">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!sendForm.sendToAll" label="用户ID" prop="userIds">
          <el-input
            v-model="userIdsInput"
            placeholder="请输入用户ID，多个用逗号分隔"
            @blur="handleUserIdsChange"
          ></el-input>
          <div class="text-sm text-gray-500 mt-1">
            请输入用户ID，多个用户用逗号分隔，例如：1,2,3
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="sendLoading"
          @click="handleConfirmSend"
        >
          确定发送
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { meMsgSuccess } from "@/utils/modal";
import utilMixin from "@/mixins/util";
import {
  deleteNotification,
  getNotificationDetail,
  getNotificationPage,
  sendNotification
} from "@/api/admin/base/notification";

export default {
  name: "AdminNotification",
  mixins: [utilMixin],
  data() {
    const messageTypeEnum = [
      // 消息类型选项
      { value: "system", label: "系统通知", elType: "primary" },
      { value: "comment_reply", label: "评论回复", elType: "success" },
      { value: "comment_like", label: "评论点赞", elType: "warning" },
      { value: "feedback_reply", label: "反馈回复", elType: "info" },
      { value: "announcement", label: "公告通知", elType: "danger" }
    ];
    const readStatusEnum = [
      // 阅读状态选项
      { value: 0, label: "未读", elType: "warning" },
      { value: 1, label: "已读", elType: "success" }
    ];
    return {
      // region 枚举以及其他变量
      messageTypeEnum,
      readStatusEnum,
      // endregion
      // region 搜索工具栏
      showSearch: true,
      searchParams: {
        title: undefined, // 通知标题
        messageType: undefined, // 消息类型
        readStatus: undefined // 阅读状态
      },
      searchConfig: [
        { type: "text", label: "通知标题", field: "title" },
        {
          type: "select",
          label: "消息类型",
          field: "messageType",
          options: messageTypeEnum
        },
        {
          type: "select",
          label: "阅读状态",
          field: "readStatus",
          options: readStatusEnum
        }
      ],
      sortingFields: ["read_status:asc", "id:desc"], // 排序字段
      queryParams: {
        pageNo: 1,
        pageSize: 10
      },
      // endregion
      // region 表格渲染
      selections: [], // 多选框
      total: 0, // 总条数
      loading: false, // 遮罩层
      tableData: [], // 表格数据
      tableColumns: [
        { prop: "id", label: "ID", width: "80" },
        { prop: "userId", label: "接收用户ID", width: "120" },
        { prop: "title", label: "通知标题", minWidth: "200" },
        {
          prop: "messageType",
          label: "消息类型",
          width: "120",
          render: (h, row) => {
            return h(
              "el-tag",
              {
                props: {
                  type: this.getElType(this.messageTypeEnum, row.messageType)
                }
              },
              row.messageTypeDesc || "未知"
            );
          }
        },
        {
          prop: "content",
          label: "通知内容",
          minWidth: "300",
          render: (h, row) => {
            return h(
              "el-tag",
              {
                props: {
                  class: "notification-content"
                }
              },
              row.content
            );
          }
        },
        {
          prop: "readStatus",
          label: "阅读状态",
          width: "100",
          render: (h, row) => {
            return h(
              "el-tag",
              {
                props: {
                  type: this.getElType(this.readStatusEnum, row.readStatus)
                }
              },
              row.readStatusDesc || "未知"
            );
          }
        },
        { prop: "sendTime", label: "发送时间", width: "180" },
        { slot: "operate" }
      ],
      // endregion
      // region 对话框相关
      detailDialogVisible: false, // 详情对话框显示状态
      currentNotification: null, // 当前选中的通知
      sendDialogVisible: false, // 发送对话框显示状态
      sendLoading: false, // 发送加载状态
      userIdsInput: "", // 用户ID输入框
      sendForm: {
        // 发送表单
        messageType: "",
        title: "",
        content: "",
        relatedLink: "",
        sendToAll: true,
        userIds: []
      },
      sendRules: {
        // 发送表单验证规则
        messageType: [
          { required: true, message: "请选择消息类型", trigger: "change" }
        ],
        title: [{ required: true, message: "请输入通知标题", trigger: "blur" }],
        content: [
          { required: true, message: "请输入通知内容", trigger: "blur" },
          {
            min: 1,
            max: 500,
            message: "通知内容长度在 1 到 500 个字符",
            trigger: "blur"
          }
        ],
        sendToAll: [
          { required: true, message: "请选择发送范围", trigger: "change" }
        ],
        userIds: [
          {
            validator: (rule, value, callback) => {
              if (!this.sendForm.sendToAll && (!value || value.length === 0)) {
                callback(new Error("请输入用户ID"));
              } else {
                callback();
              }
            },
            trigger: "blur"
          }
        ]
      }
      // endregion
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    async init() {
      await this.fetchList();
    },
    // 获取通知列表
    async fetchList() {
      try {
        this.loading = true;
        const data = Object.assign(
          { sortingFields: this.sortingFields },
          this.queryParams,
          this.searchParams || {}
        );
        const res = await getNotificationPage(data);
        this.tableData = res.list || [];
        this.total = res.total || 0;
      } catch (error) {
        console.error("获取通知列表失败:", error);
      } finally {
        this.loading = false;
      }
    },
    // 搜索处理
    handleSearch() {
      this.queryParams.pageNo = 1;
      this.fetchList();
    },
    // 重置搜索
    handleResetSearch() {
      this.handleSearch();
    },
    // 查看通知详情
    async handleViewDetail(row) {
      try {
        const res = await getNotificationDetail(row.id);
        this.currentNotification = res.data;
        this.detailDialogVisible = true;
      } catch (error) {
        console.error("获取通知详情失败:", error);
      }
    },
    // 添加通知
    handleAdd() {
      this.sendForm = {
        messageType: "",
        title: "",
        content: "",
        relatedLink: "",
        sendToAll: true,
        userIds: []
      };
      this.userIdsInput = "";
      this.sendDialogVisible = true;
      this.$nextTick(() => {
        this.$refs.sendForm?.clearValidate();
      });
    },
    // 用户ID输入处理
    handleUserIdsChange() {
      if (this.userIdsInput.trim()) {
        this.sendForm.userIds = this.userIdsInput
          .split(",")
          .map(id => parseInt(id.trim()))
          .filter(id => !isNaN(id));
      } else {
        this.sendForm.userIds = [];
      }
    },
    // 确认发送
    async handleConfirmSend() {
      try {
        await this.$refs.sendForm.validate();
        this.sendLoading = true;
        await sendNotification(this.sendForm);
        meMsgSuccess({ message: "通知发送成功" });
        this.sendDialogVisible = false;
        await this.fetchList();
      } catch (error) {
        if (error !== false) {
          console.error("发送通知失败:", error);
        }
      } finally {
        this.sendLoading = false;
      }
    },
    // 删除单个通知
    async handleDelete(row) {
      try {
        await this.$confirm("确认删除该通知?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        });
        await deleteNotification([row.id]);
        meMsgSuccess({ message: "删除成功" });
        await this.fetchList();
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除失败:", error);
        }
      }
    },
    // 批量删除
    async handleBatchDelete() {
      if (!this.selections.length) {
        this.$message.warning("请选择要删除的数据");
        return;
      }
      try {
        await this.$confirm(
          `确认删除选中的${this.selections.length}条数据?`,
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          }
        );
        const ids = this.selections.map(item => item.id);
        await deleteNotification(ids);
        meMsgSuccess({ message: "删除成功" });
        await this.fetchList();
      } catch (error) {
        if (error !== "cancel") {
          console.error("批量删除失败:", error);
        }
      }
    },
    // 选择变更处理
    handleSelectionChange(selection) {
      this.selections = selection;
    }
  }
};
</script>

<style scoped>
.notification-content {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-content-detail {
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  padding: 8px;
  background-color: #efefef;
  border-radius: 4px;
}

.notification-detail {
  margin: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
