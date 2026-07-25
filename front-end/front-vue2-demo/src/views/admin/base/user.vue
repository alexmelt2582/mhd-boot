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
        :show-update-button="false"
        :left-button-config="leftButtonConfig"
        @refresh="getList"
        @add="handleAdd"
        @delete="handleDelete"
      >
        <template #resetPwd>
          <el-button
            type="primary"
            plain
            icon="el-icon-refresh-left"
            :disabled="selections.length === 0"
            @click="resetPwd(selections)"
            >重置密码
          </el-button>
        </template>
      </me-crud-button>
      <me-edit-form
        :title="editTitle"
        :open.sync="editOpen"
        :edit-config="editConfig"
        :edit-form-data="editForm"
        :edit-form-rules="editRules"
        :edit-form-ref="editFormRef"
        @handleEdit="submitForm"
        @handleReset="handleResetEdit"
      />
      <me-table
        :loading="loading"
        row-key="id"
        :showSelection="true"
        :table-data="tableData"
        :table-columns="tableColumns"
        @handleSelectChange="handleSelectionChange"
      >
        <template #avatar>
          <el-table-column prop="avatar" label="头像" width="80">
            <template v-slot="scope">
              <el-avatar :size="50" :src="attachImageUrl(scope.row.avatar)" />
            </template>
          </el-table-column>
        </template>
        <template #status>
          <el-table-column prop="status" label="状态" width="80">
            <template v-slot="scope">
              <el-switch
                v-model="scope.row.status"
                :active-value="0"
                :inactive-value="1"
                @change="handleStatusChange(scope.row)"
              />
            </template>
          </el-table-column>
        </template>
        <template #operate>
          <el-table-column
            label="操作"
            fixed="right"
            align="center"
            width="80"
            class-name="small-padding fixed-width"
          >
            <template v-slot="scope">
              <el-button
                type="text"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
                >删除
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
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script>
import {
  addBaseUser,
  delBaseUser,
  pageBaseUser,
  resetPwd,
  updateStatus
} from "@/api/admin/base/user";
import { meMsgSuccess } from "@/utils/modal";
import utilMixin from "@/mixins/util";
import imgMixin from "@/mixins/img";
import { roleTypeEnum } from "@/enums/role";

export default {
  name: "AdminUser",
  mixins: [utilMixin, imgMixin],
  data() {
    const statusEnum = [
      { label: "开启", value: 0 },
      { label: "关闭", value: 1 }
    ];
    return {
      // region 枚举以及其他变量
      statusEnum,
      roleTypeEnum,
      // endregion
      // region 搜索工具栏
      showSearch: true,
      searchParams: {
        username: undefined,
        email: undefined,
        status: undefined,
        role: undefined
      },
      searchConfig: [
        { type: "text", label: "用户账号", field: "username" },
        { type: "text", label: "用户邮箱", field: "email" },
        {
          type: "select",
          label: "用户角色",
          field: "role",
          options: roleTypeEnum
        },
        {
          type: "select",
          label: "用户状态",
          field: "status",
          options: statusEnum
        }
      ],
      sortingFields: ["id:desc"],
      queryParams: {
        pageNo: 1,
        pageSize: 10
      },
      // endregion
      // region 编辑表格
      editTitle: "", // 编辑表格标题，不能设置为 undefined
      editOpen: false,
      defaultEditForm: {
        username: undefined,
        mobile: undefined,
        email: undefined,
        introduction: undefined,
        role: undefined
      },
      editFormRef: "editFormRef",
      editForm: {},
      editConfig: [
        { type: "text", label: "用户账号", field: "username" },
        {
          type: "radio",
          label: "角色",
          field: "role",
          options: roleTypeEnum
        },
        { type: "text", label: "电话", field: "mobile" },
        { type: "text", label: "邮箱", field: "email" },
        { type: "textarea", label: "简介", field: "introduction" }
      ],
      // 表单校验
      editRules: {
        username: [{ required: true, message: "请输入账号", trigger: "blur" }],
        role: [{ required: true, message: "请选择角色", trigger: "blur" }]
      },
      // endregion
      // region 表格渲染
      leftButtonConfig: [{ slot: "resetPwd" }],
      selections: [], // 多选框
      total: 0, // 总条数
      loading: false, // 遮罩层
      tableData: [], // 表格数据
      tableColumns: [
        { slot: "avatar" },
        { label: "账号", prop: "username" },
        { label: "手机号码", prop: "mobile", width: "150" },
        { label: "邮箱", prop: "email", width: "200" },
        {
          label: "角色",
          prop: "role",
          width: "100",
          render: (h, row, index) => {
            return h(
              "el-tag",
              { props: {} },
              this.convertValueToLabel(this.roleTypeEnum, row.role)
            );
          }
        },
        { slot: "status" },
        { label: "最后登录IP", prop: "loginIp", width: "100" },
        { label: "简介", prop: "introduction", minWidth: "200" },
        { prop: "loginTime", label: "最后登录时间", width: "180" },
        { prop: "createTime", label: "创建时间", width: "180" },
        { prop: "updateTime", label: "修改时间", width: "180" },
        { slot: "operate" }
      ]
      // endregion
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    async init() {
      await this.getList();
    },
    async getList() {
      const that = this;
      this.loading = true;
      const data = Object.assign(
        { sortingFields: this.sortingFields },
        this.queryParams,
        this.searchParams ? this.searchParams : {}
      );
      pageBaseUser(data)
        .then(res => {
          that.tableData = res.list;
          that.total = res.total;
        })
        .finally(() => {
          that.loading = false;
        });
    },
    handleSearch() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    handleResetSearch() {
      this.handleSearch();
    },
    resetEditForm() {
      this.editForm = JSON.parse(JSON.stringify(this.defaultEditForm));
    },
    submitForm(formRef) {
      const that = this;
      formRef.validate(valid => {
        if (valid) {
          // 提交
          addBaseUser(that.editForm).then(() => {
            meMsgSuccess({
              message: "添加成功"
            });
            that.editOpen = false;
            that.getList();
          });
        }
      });
    },
    handleResetEdit(formRef) {
      this.editOpen = false;
      formRef.resetFields();
      formRef.clearValidate();
      this.resetEditForm();
      this.getList();
    },
    handleAdd() {
      this.resetEditForm();
      this.editOpen = true;
      this.editTitle = "添加用户";
    },
    handleDelete(dataList) {
      const that = this;
      const ids = [];
      if (dataList instanceof Array) {
        dataList.forEach(val => {
          ids.push(val.id);
        });
      } else {
        ids.push(dataList.id);
      }
      this.$confirm(`确认删除选中的${ids.length}条数据?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          delBaseUser(ids).then(() => {
            meMsgSuccess({
              message: "删除成功"
            });
            that.getList();
          });
        })
        .catch(() => {});
    },
    handleSelectionChange(selection) {
      this.selections = selection;
    },
    handleStatusChange(row) {
      const that = this;
      const text = row.status === 0 ? "启用" : "停用";
      this.$confirm('确认要"' + text + '""' + row.username + '"用户吗?').then(
        () => {
          const forData = new FormData();
          forData.append("id", row.id);
          forData.append("status", row.status);
          updateStatus(forData)
            .then(() => {
              meMsgSuccess({
                message: text + "成功"
              });
            })
            .finally(() => {
              that.getList();
            });
        }
      );
    },
    resetPwd(dataList) {
      const ids = [];
      dataList.forEach(data => {
        ids.push(data.id);
      });
      resetPwd(ids).then(() => {
        meMsgSuccess({
          message: "密码重置成功"
        });
      });
    }
  }
};
</script>
