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
        @refresh="getList"
        @add="handleAdd"
        @update="handleUpdate"
        @delete="handleDelete"
      />
      <el-dialog
        v-if="editOpen"
        :title="editTitle"
        :visible.sync="editOpen"
        width="800px"
        :append-to-body="true"
        :before-close="handleResetEdit"
      >
        <me-cover-image
          :custom-options="{
            autoCropWidth: 1080,
            // autoCropHeight: 420,
            fixed: true,
            fixedNumber: [1080, 420],
            full: true,
            infoTrue: true
          }"
          :initial-img="
            editForm.bannerUrl ? attachImageUrl(editForm.bannerUrl) : ''
          "
          :show-preview="false"
          @handleFile="handleFile"
          @close="handleResetEdit"
        />
      </el-dialog>
      <me-table
        :loading="loading"
        :border="false"
        :stripe="true"
        row-key="id"
        :showSelection="true"
        :table-data="tableData"
        :table-columns="tableColumns"
        @handleSelectChange="handleSelectionChange"
      >
        <template #banner>
          <el-table-column label="轮播图" min-width="500" align="center">
            <template v-slot="scope">
              <el-image
                fit="cover"
                :src="attachImageUrl(scope.row.bannerUrl)"
                @error="attachImageUrlError"
                :preview-src-list="[attachImageUrl(scope.row.bannerUrl)]"
                style="
                  width: 500px;
                  height: 200px;
                  border-radius: 0.5rem;
                  vertical-align: middle;
                "
              />
            </template>
          </el-table-column>
        </template>
        <template #status>
          <el-table-column width="100" label="状态">
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
            width="200"
            align="center"
            class-name="small-padding fixed-width"
          >
            <template v-slot="scope">
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
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script>
import {
  addBaseBanner,
  changeBannerStatus,
  delBaseBanner,
  getBaseBanner,
  pageBaseBanner,
  updateBaseBanner
} from "@/api/admin/base/banner";
import { meMsgSuccess } from "@/utils/modal";
import utilMixin from "@/mixins/util";
import imgMixin from "@/mixins/img";

export default {
  name: "AdminBanner",
  mixins: [utilMixin, imgMixin],
  data() {
    const statusEnum = [
      { label: "启用", value: 0 },
      { label: "禁用", value: 1 }
    ];
    return {
      // region 枚举以及其他变量
      statusEnum,
      // endregion
      // region 搜索工具栏
      showSearch: true,
      searchParams: {
        status: undefined
      },
      searchConfig: [
        { type: "select", label: "状态", field: "status", options: statusEnum }
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
        id: undefined,
        bannerUrl: "" // 不能设置为 undefined，因为传递必须是字符串
      },
      editFormRef: "editFormRef",
      editForm: {},
      editConfig: [],
      // 表单校验
      editRules: {},
      // endregion
      // region 表格渲染
      selections: [], // 多选框
      total: 0, // 总条数
      loading: false, // 遮罩层
      tableData: [], // 表格数据
      tableColumns: [
        { label: "编号", prop: "id", width: "200" },
        { slot: "banner" },
        { slot: "status" },
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
      pageBaseBanner(data)
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
    reset() {
      this.editForm = JSON.parse(JSON.stringify(this.defaultEditForm));
    },
    handleResetEdit() {
      this.editOpen = false;
      this.reset();
      this.getList();
    },
    handleAdd() {
      this.reset();
      this.editOpen = true;
      this.editTitle = "添加轮播图";
    },
    handleUpdate(row) {
      const that = this;
      this.reset();
      getBaseBanner(row.id).then(res => {
        that.editForm = res.data;
        that.editOpen = true;
        that.editTitle = "修改轮播图";
      });
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
          delBaseBanner(ids).then(() => {
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
      this.$confirm('确认要"' + text + '"轮播图吗?').then(() => {
        const forData = new FormData();
        forData.append("id", row.id);
        forData.append("status", row.status);
        changeBannerStatus(forData)
          .then(() => {
            meMsgSuccess({
              message: text + "成功"
            });
          })
          .finally(() => {
            that.getList();
          });
      });
    },
    async handleFile(blob) {
      const that = this;
      const formData = new FormData();
      formData.append("file", blob, "banner.jpg");
      try {
        if (this.editForm.id) {
          formData.append("id", this.editForm.id);
          await updateBaseBanner(formData);
        } else {
          await addBaseBanner(formData);
        }
        meMsgSuccess({
          message: "上传成功"
        });
      } catch (e) {
        console.error(e);
      } finally {
        that.editOpen = false;
        await that.getList();
      }
    }
  }
};
</script>
