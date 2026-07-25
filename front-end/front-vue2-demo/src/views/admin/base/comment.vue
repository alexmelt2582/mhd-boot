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
                      正常
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="2"
                      :disabled="scope.row.status === 2"
                    >
                      审核中
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="3"
                      :disabled="scope.row.status === 3"
                    >
                      已删除
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
    <!-- 评论详情对话框 -->
    <el-dialog
      title="评论详情"
      :visible.sync="detailDialogVisible"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="currentComment" class="comment-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="评论ID"
            >{{ currentComment.id }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名"
            >{{ currentComment.userName || "未知用户" }}
          </el-descriptions-item>
          <el-descriptions-item label="目标类型">
            <el-tag
              :type="getElType(targetTypeEnum, currentComment.targetType)"
              size="small"
            >
              {{ currentComment.targetTypeDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目标ID"
            >{{ currentComment.targetId }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag
              :type="getElType(statusEnum, currentComment.status)"
              size="small"
            >
              {{ currentComment.statusDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="点赞数"
            >{{ currentComment.likeCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2"
            >{{ currentComment.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="评论内容" :span="2">
            <div class="comment-content-detail">
              {{ currentComment.content }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { meMsgSuccess } from "@/utils/modal";
import utilMixin from "@/mixins/util";
import {
  deleteComment,
  getCommentDetail,
  getCommentPage,
  updateCommentStatus
} from "@/api/admin/base/comment";

export default {
  name: "AdminComment",
  mixins: [utilMixin],
  data() {
    const targetTypeEnum = [
      // 目标类型选项
      // { value: "article", label: "文章", elType: "primary" },
      // { value: "post", label: "帖子", elType: "success" },
      // { value: "product", label: "产品", elType: "warning" },
      // { value: "video", label: "视频", elType: "info" }
      { value: "news", label: "资讯", elType: "info" }
    ];
    const statusEnum = [
      // 状态选项
      { value: 1, label: "正常", elType: "success" },
      { value: 2, label: "审核中", elType: "warning" },
      { value: 3, label: "已删除", elType: "danger" }
    ];
    return {
      // region 枚举以及其他变量
      targetTypeEnum,
      statusEnum,
      // endregion
      // region 搜索工具栏
      showSearch: true,
      searchParams: {
        content: undefined, // 评论内容
        targetType: undefined, // 目标类型
        status: undefined // 状态
      },
      searchConfig: [
        { type: "text", label: "评论内容", field: "content" },
        {
          type: "select",
          label: "目标类型",
          field: "targetType",
          options: targetTypeEnum
        },
        {
          type: "select",
          label: "状态",
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
        { prop: "userId", label: "用户ID", width: "120" },
        {
          prop: "targetType",
          label: "目标类型",
          width: "120",
          render: (h, row) => {
            return h(
              "el-tag",
              {
                props: {
                  type: this.getElType(this.targetTypeEnum, row.targetType)
                }
              },
              row.targetTypeDesc
            );
          }
        },
        { prop: "targetId", label: "目标ID", width: "100" },
        {
          prop: "content",
          label: "评论内容",
          render: (h, row) => {
            return h(
              "div",
              {
                class: "comment-content"
              },
              row.content
            );
          }
        },
        {
          prop: "status",
          label: "状态",
          width: "100",
          render: (h, row) => {
            return h(
              "el-tag",
              {
                props: {
                  type: this.getElType(this.statusEnum, row.status)
                }
              },
              row.statusDesc
            );
          }
        },
        { prop: "likeCount", label: "点赞数", width: "80" },
        { prop: "createTime", label: "创建时间", width: "180" },
        { slot: "operate", label: "操作", width: "200", fixed: "right" }
      ],
      // endregion
      // region 对话框相关
      detailDialogVisible: false, // 详情对话框显示状态
      currentComment: null // 当前选中的评论
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
    // 获取评论列表
    async fetchList() {
      try {
        this.loading = true;
        const data = Object.assign(
          { sortingFields: this.sortingFields },
          this.queryParams,
          this.searchParams || {}
        );
        const res = await getCommentPage(data);
        this.tableData = res.list || [];
        this.total = res.total || 0;
      } catch (error) {
        console.error("获取评论列表失败:", error);
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
    // 查看评论详情
    async handleViewDetail(row) {
      try {
        const res = await getCommentDetail(row.id);
        this.currentComment = res.data;
        this.detailDialogVisible = true;
      } catch (error) {
        console.error("获取评论详情失败:", error);
      }
    },
    // 状态变更
    async handleStatusChange(row, status) {
      try {
        await this.$confirm(
          `确认将评论状态修改为"${this.convertValueToLabel(
            this.statusEnum,
            Number(status)
          )}"?`,
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          }
        );
        await updateCommentStatus({
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
    // 删除单个评论
    async handleDelete(row) {
      try {
        await this.$confirm("确认删除该评论?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        });
        await deleteComment([row.id]);
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
        await deleteComment(ids);
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
.comment-content {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comment-content-detail {
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  padding: 8px;
  background-color: #efefef;
  border-radius: 4px;
}

.comment-detail {
  margin: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
