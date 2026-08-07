<template>
  <div class="app-container">
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
        @refresh="fetchList"
        @add="handleAdd"
        @update="handleUpdate"
        @delete="handleDelete"
      />
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
        row-key="${primaryKey}"
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
                icon="el-icon-document-copy"
                @click="handleCopy(scope.row)"
              >复制
              </el-button>
              <el-button
                type="text"
                icon="el-icon-edit"
                @click="handleUpdate(scope.row)"
              >修改
              </el-button>
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
        @pagination="fetchList"
      />
    </div>
  </div>
</template>

<script>
import {
  add${methodName},
  del${methodName},
  get${methodName},
  page${methodName},
  update${methodName}
} from "@/api/${urlPath}";
import { meMsgSuccess } from "@/utils/modal";

export default {
  name: "${nameToUpper}",
  data() {
    return {
      // region 枚举以及其他变量
      // endregion
      // region 搜索工具栏
      showSearch: true,
      searchParams: {
        // name: undefined,
      },
      searchConfig: [
        // {type: "text", label: "名称", field: "name"},
      ],
      sortingFields: ["${primaryKey}:desc"],
      queryParams: {
        pageNo: 1,
        pageSize: 10
      },
      // endregion
      // region 编辑表格
      editTitle: "", // 编辑表格标题，不能设置为 undefined
      editOpen: false,
      defaultEditForm: {},
      editFormRef: "editFormRef",
      editForm: {
        // id: undefined,
        // name: undefined,
      },
      editConfig: [
        // {type: 'text', label: '名称', field: 'name'},
      ],
      // 表单校验
      editRules: {
        // name: [
        //  {required: true, message: '请输入名称', trigger: 'blur'}
        // ]
      },
      // endregion
      // region 表格渲染
      selections: [], // 多选框
      total: 0, // 总条数
      loading: false, // 遮罩层
      tableData: [], // 表格数据
      tableColumns: [
        { prop: "name", label: "名称" },
        { prop: "createTime", label: "创建时间", width: "180" },
        { prop: "updateTime", label: "修改时间", width: "180" },
        { slot: "operate" }
      ]
      // endregion
    };
  },
  created() {
    this.defaultEditForm = this.$deepCopy(this.editForm);
  },
  mounted() {
    this.init();
  },
  methods: {
    async init() {
      await this.getLoadEnum();
      await this.setEditConfig();
      await this.fetchList();
    },
    async getLoadEnum() {
      // let tagRes = await allTag();
      // this.tagEnum = tagRes.data.map(item => ({
      //   label: item.name,
      //   value: item.id,
      //   ...item
      // }))
    },
    async setEditConfig() {
      const configMapping = {
        // fileSeparator: this.fileSeparatorEnum,
      };
      this.editConfig.forEach((config) => {
        if (configMapping[config.field]) {
          config.options = configMapping[config.field];
        }
      });
    },
    async fetchList() {
      this.loading = true;
      try {
        const params = Object.assign(
          { sortingFields: this.sortingFields },
          this.queryParams,
          this.searchParams ? this.searchParams : {}
        );
        const { list, total } = await  page${methodName}(params);
        this.tableData = list || [];
        this.total = total || 0;
      } catch (error) {
        console.error("获取数据失败:", error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.queryParams.pageNo = 1;
      this.fetchList();
    },
    handleResetSearch() {
      this.handleSearch();
    },
    resetEditForm() {
      this.editForm = this.$deepCopy(this.defaultEditForm);
    },
    submitForm(formRef) {
      const that = this;
      formRef.validate(valid => {
        if (valid) {
          // 提交
          if (that.editForm.${primaryKey} !== undefined) {
            update${methodName}(that.editForm).then(() => {
              meMsgSuccess({
                message: "修改成功"
              });
              that.editOpen = false;
              that.fetchList();
            });
          } else {
            add${methodName}(that.editForm).then(() => {
              meMsgSuccess({
                message: "添加成功"
              });
              that.editOpen = false;
              that.fetchList();
            });
          }
        }
      });
    },
    handleResetEdit(formRef) {
      this.editOpen = false;
      formRef.resetFields();
      formRef.clearValidate();
      this.resetEditForm();
      this.fetchList();
    },
    handleAdd() {
      this.resetEditForm();
      this.editOpen = true;
      this.editTitle = "添加";
    },
    handleUpdate(row) {
      const that = this;
      this.resetEditForm();
      get${methodName}(row.${primaryKey}).then(res => {
        Object.keys(this.editForm).forEach(key => {
          if (res.data[key]) {
            that.editForm[key] = res.data[key];
          }
        });
        that.editForm = res.data;
        that.editOpen = true;
        that.editTitle = "修改";
      });
    },
    handleCopy(row) {
      const that = this;
      this.resetEditForm();
      get${methodName}(row.${primaryKey}).then(res => {
        Object.keys(this.editForm).forEach(key => {
          if (res.data[key]) {
            that.editForm[key] = res.data[key];
          }
        });
        that.editForm.${primaryKey} = undefined;
        that.editOpen = true;
        that.editTitle = "复制";
      });
    },
    handleDelete(dataList) {
      const that = this;
      const ids = [];
      if (dataList instanceof Array) {
        dataList.forEach(val => {
          ids.push(val.${primaryKey});
        });
      } else {
        ids.push(dataList.${primaryKey});
      }
      this.$confirm(`确认删除选中的${r'${ids.length}'}条数据?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          del${methodName}(ids).then(() => {
            meMsgSuccess({
              message: "删除成功"
            });
            that.fetchList();
          });
        })
        .catch(() => {});
    },
    handleSelectionChange(selection) {
      this.selections = selection;
    }
  }
};
</script>
