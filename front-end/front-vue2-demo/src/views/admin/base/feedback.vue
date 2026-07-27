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
        :show-add-button="false"
        :show-update-button="false"
        @refresh="fetchList"
        @delete="handleBatchDelete"
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
                @click="handleReply(scope.row)"
              >
                回复
              </el-button>
              <el-dropdown
                @command="command => handleStatusChange(scope.row, command)"
              >
                <el-button type="text" size="small">
                  状态<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      command="1"
                      :disabled="scope.row.status === 1"
                    >
                      待处理
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="2"
                      :disabled="scope.row.status === 2"
                    >
                      处理中
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="3"
                      :disabled="scope.row.status === 3"
                    >
                      已解决
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
    <!-- 反馈详情对话框 -->
    <el-dialog
      title="反馈详情"
      :visible.sync="detailDialogVisible"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="currentFeedback" class="feedback-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="反馈ID"
            >{{ currentFeedback.id }}
          </el-descriptions-item>
          <el-descriptions-item label="反馈类型">
            <el-tag
              :type="
                getElType(this.feedbackTypeEnum, currentFeedback.feedbackType)
              "
              size="small"
            >
              {{ currentFeedback.feedbackTypeDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag
              :type="getElType(this.statusEnum, currentFeedback.status)"
              size="small"
            >
              {{ currentFeedback.statusDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间"
            >{{ currentFeedback.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="反馈标题" :span="2"
            >{{ currentFeedback.title }}
          </el-descriptions-item>
          <el-descriptions-item label="联系方式" :span="2"
            >{{ currentFeedback.contactInfo || "未提供" }}
          </el-descriptions-item>
          <el-descriptions-item label="反馈内容" :span="2">
            <div class="feedback-content">{{ currentFeedback.content }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleReply(currentFeedback)"
          >回复
        </el-button>
      </div>
    </el-dialog>
    <!-- 回复对话框 -->
    <el-dialog
      title="回复反馈"
      :visible.sync="replyDialogVisible"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="replyForm"
        :model="replyForm"
        :rules="replyRules"
        label-width="80px"
      >
        <el-form-item label="反馈标题">
          <span>{{ replyForm.title }}</span>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="replyForm.replyContent"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="replyLoading"
          @click="handleSubmitReply"
        >
          确定回复
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { meMsgSuccess } from "@/utils/modal";
import utilMixin from "@/mixins/util";
import {
  deleteFeedback,
  getFeedbackDetail,
  getFeedbackPage,
  replyFeedback,
  updateFeedbackStatus
} from "@/api/admin/base/feedback";

export default {
  name: "AdminFeedback",
  mixins: [utilMixin],
  data() {
    const feedbackTypeEnum = [
      // 反馈类型选项
      { value: 1, label: "建议", elType: "info" },
      { value: 2, label: "Bug反馈", elType: "warning" },
      { value: 3, label: "举报", elType: "danger" }
    ];
    const statusEnum = [
      // 状态选项
      { value: 1, label: "待处理", elType: "warning" },
      { value: 2, label: "处理中", elType: "primary" },
      { value: 3, label: "已解决", elType: "success" }
    ];
    return {
      // region 枚举以及其他变量
      feedbackTypeEnum,
      statusEnum,
      // endregion
      // region 搜索工具栏
      showSearch: true,
      searchParams: {
        title: undefined, // 反馈标题
        feedbackType: undefined, // 反馈类型
        status: undefined // 处理状态
      },
      searchConfig: [
        { type: "text", label: "反馈标题", field: "title" },
        {
          type: "select",
          label: "反馈类型",
          field: "feedbackType",
          options: feedbackTypeEnum
        },
        {
          type: "select",
          label: "处理状态",
          field: "status",
          options: statusEnum
        }
      ],
      sortingFields: ["status:asc", "id:desc"], // 排序字段
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
        { prop: "title", label: "反馈标题" },
        {
          prop: "feedbackType",
          label: "反馈类型",
          render: (h, row) => {
            return h(
              "el-tag",
              {
                props: {
                  type: this.getElType(this.feedbackTypeEnum, row.feedbackType)
                }
              },
              row.feedbackTypeDesc || "未知"
            );
          }
        },
        {
          prop: "status",
          label: "处理状态",
          width: "120",
          render: (h, row) => {
            return h(
              "el-tag",
              {
                props: {
                  type: this.getElType(this.statusEnum, row.status)
                }
              },
              row.statusDesc || "未知"
            );
          }
        },
        { prop: "contactInfo", label: "联系方式", width: "150" },
        { prop: "createTime", label: "提交时间", width: "180" },
        { slot: "operate" }
      ],
      // endregion
      // region 对话框相关
      detailDialogVisible: false, // 详情对话框显示状态
      replyDialogVisible: false, // 回复对话框显示状态
      currentFeedback: null, // 当前选中的反馈
      replyLoading: false, // 回复提交加载状态
      replyForm: {
        // 回复表单
        feedbackId: null,
        title: "",
        replyContent: ""
      },
      replyRules: {
        // 回复表单验证规则
        replyContent: [
          { required: true, message: "请输入回复内容", trigger: "blur" },
          {
            min: 1,
            max: 500,
            message: "回复内容长度在 1 到 500 个字符",
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
    // 获取反馈列表
    async fetchList() {
      try {
        this.loading = true;
        const data = Object.assign(
          { sortingFields: this.sortingFields },
          this.queryParams,
          this.searchParams || {}
        );
        const res = await getFeedbackPage(data);
        this.tableData = res.list || [];
        this.total = res.total || 0;
      } catch (error) {
        console.error("获取反馈列表失败:", error);
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
    // 查看反馈详情
    async handleViewDetail(row) {
      try {
        const res = await getFeedbackDetail(row.id);
        this.currentFeedback = res.data;
        this.detailDialogVisible = true;
      } catch (error) {
        console.error("获取反馈详情失败:", error);
      }
    },
    // 回复反馈
    handleReply(row) {
      this.replyForm = {
        feedbackId: row.id,
        title: row.title,
        replyContent: ""
      };
      this.replyDialogVisible = true;
      this.$nextTick(() => {
        this.$refs.replyForm?.clearValidate();
      });
    },
    // 提交回复
    async handleSubmitReply() {
      this.$refs.replyForm.validate(async valid => {
        if (valid) {
          try {
            this.replyLoading = true;
            await replyFeedback({
              feedbackId: this.replyForm.feedbackId,
              replyContent: this.replyForm.replyContent
            });
            meMsgSuccess({ message: "回复成功" });
            this.replyDialogVisible = false;
            await this.fetchList();
          } catch (error) {
            if (error !== false) {
              // 表单验证失败时不显示错误
              console.error("回复失败:", error);
            }
          } finally {
            this.replyLoading = false;
          }
        }
      });
    },
    // 状态变更
    async handleStatusChange(row, status) {
      try {
        await this.$confirm(
          `确认将反馈状态修改为"${this.convertValueToLabel(
            this.statusEnum,
            status
          )}"?`,
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          }
        );
        await updateFeedbackStatus({
          id: row.id,
          status: parseInt(status)
        });
        meMsgSuccess({ message: "状态更新成功" });
        await this.fetchList();
      } catch (error) {
        if (error !== "cancel") {
          console.error("状态更新失败:", error);
        }
      }
    },
    // 删除单个反馈
    async handleDelete(row) {
      try {
        await this.$confirm("确认删除该反馈?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        });
        await deleteFeedback([row.id]);
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
        await deleteFeedback(ids);
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
.feedback-detail {
  margin: 20px 0;
}

.feedback-content {
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  padding: 8px;
  background-color: #efefef;
  border-radius: 4px;
}

.dialog-footer {
  text-align: right;
}
</style>
