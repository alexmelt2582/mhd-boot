<template>
  <div
    :class="{ hidden }"
    :style="`text-align: ${textAlign}`"
    class="pagination-container"
  >
    <el-pagination
      :background="background"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page.sync="currentPage"
      :page-sizes="pageSizes"
      :page-size.sync="pageSize"
      :layout="layout"
      :total="total"
    />
  </div>
</template>

<script>
/**
 * Props 描述：
 * - background: 显示的页数（1，2，3...）是否有背景色。默认 true
 * - hidden: 是否隐藏分页组件。默认 false
 * - page: 当前页码，使用 .sync 修饰符双向绑定。必填
 * - limit: 每页条数，使用 .sync 修饰符双向绑定。必填
 * - pageSizes: 每页条数选择器的选项设置。默认 [10, 20, 30, 40, 50]
 * - layout: 分页组件的布局。默认 "total, sizes, prev, pager, next, jumper"
 * - total: 数据总条数。必填
 * - textAlign: 分页显示位置（left、center、right）。默认 right
 */
export default {
  name: "MePagination",
  props: {
    background: { type: Boolean, default: true },
    hidden: { type: Boolean, default: false },
    page: { type: Number, required: true },
    limit: { type: Number, required: true },
    pageSizes: { type: Array, default: () => [10, 20, 30, 40, 50] },
    layout: {
      type: String,
      default: "total, sizes, prev, pager, next, jumper"
    },
    total: { type: Number, required: true },
    textAlign: { type: String, default: "right" }
  },
  computed: {
    currentPage: {
      get() {
        return this.page;
      },
      set(val) {
        this.$emit("update:page", val);
      }
    },
    pageSize: {
      get() {
        return this.limit;
      },
      set(val) {
        this.$emit("update:limit", val);
      }
    }
  },
  methods: {
    handleSizeChange(val) {
      if (this.currentPage * val > this.total) {
        this.currentPage = 1;
      }
      this.$emit("pagination", { page: this.currentPage, limit: val });
    },
    handleCurrentChange(val) {
      this.$emit("pagination", { page: val, limit: this.pageSize });
    }
  }
};
</script>

<style lang="scss" scoped>
.pagination-container {
  padding: 16px;
}

.pagination-container.hidden {
  display: none;
}
</style>
