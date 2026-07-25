<template>
  <el-table
    v-bind="$attrs"
    :ref="tableRef"
    v-loading="loading"
    :row-key="rowKey"
    :data="tableData"
    :default-expand-all="expandAll"
    :stripe="stripe"
    :border="border"
    :height="height"
    :size="size"
    :fit="fit"
    :show-header="showHeader"
    :header-cell-style="headerCellStyle"
    @select-all="handleSelectAll"
    @select="handleSelectChange"
    @selection-change="handleSelectChange"
    @row-click="handleRowClick"
  >
    <el-table-column
      v-if="showSelection"
      :fixed="selectionFixed"
      type="selection"
      width="55"
      align="center"
    />
    <template v-for="(item, index) in tableColumns">
      <slot v-if="item.slot" :name="item.slot"></slot>
      <!-- 使用render函数的自定义列 -->
      <el-table-column
        v-else-if="item.render"
        :key="index"
        :prop="item.prop"
        :label="item.label"
        :width="item.width"
        :min-width="item.minWidth"
        :align="item.align ? item.align : 'left'"
        :show-overflow-tooltip="
          item.showOverflowTooltip ? item.showOverflowTooltip : false
        "
      >
        <template v-slot="scope">
          <render-cell
            :render="item.render"
            :row="scope.row"
            :index="scope.$index"
          />
        </template>
      </el-table-column>
      <!-- 使用formatter函数的列 -->
      <el-table-column
        v-else-if="item.formatter"
        :key="index"
        :prop="item.prop"
        :label="item.label"
        :width="item.width"
        :min-width="item.minWidth"
        :align="item.align ? item.align : 'left'"
        :show-overflow-tooltip="
          item.showOverflowTooltip ? item.showOverflowTooltip : false
        "
      >
        <template v-slot="scope">
          {{
            item.formatter(scope.row, item, scope.row[item.prop], scope.$index)
          }}
        </template>
      </el-table-column>
      <el-table-column
        v-else
        :key="index"
        :prop="item.prop"
        :label="item.label"
        :width="item.width"
        :min-width="item.minWidth"
        :align="item.align ? item.align : 'left'"
        :show-overflow-tooltip="
          item.showOverflowTooltip ? item.showOverflowTooltip : false
        "
      />
    </template>
  </el-table>
</template>

<script>
/**
 * Props 属性：
 * - tableRef: 表格的引用名称。默认 tableRef
 * - loading: 是否显示加载状态。默认 false
 * - rowKey: 表格行的唯一标识字段。
 * - tableData: 表格数据。默认 []
 * - height: 表格高度。
 * - maxHeight: 表格最大高度。
 * - expandAll: 是否默认展开所有行。默认 false
 * - stripe: 是否使用斑马纹。默认 false
 * - border: 是否显示边框。默认 true
 * - size: 表格尺寸（medium/small/mini）。
 * - fit: 是否自适应宽度。默认 true
 * - showHeader: 是否显示表头。默认 true
 * - headerCellStyle: 表头单元格样式。默认 { color: "#333", backgroundColor: "#eee" }
 * - showSelection: 是否显示选择框。默认 false
 * - selectionFixed: 选择框是否固定在左侧。默认 true
 * - tableColumns: 表格列配置。默认 []
 *
 * render 和 formatter 使用示例：
 * {
 *   prop: "status",
 *   label: "处理状态",
 *   width: "120",
 *   render: (h, row, index) => { // 不采用 function(h, row, index) {} 方法中 this指向当前组件
 *     return h("el-tag", { props: {} }, row.statusDesc || "未知");
 *   }
 * },
 * {
 *   prop: "feedbackType",
 *   label: "反馈类型",
 *   width: "120",
 *   formatter: row => {
 *     return row.feedbackTypeDesc || "未知";
 *   }
 * },
 */

// 辅助组件，用于渲染render函数
const RenderCell = {
  functional: true,
  props: {
    render: Function,
    row: Object,
    index: Number
  },
  render: (h, ctx) => {
    return ctx.props.render(h, ctx.props.row, ctx.props.index);
  }
};
export default {
  name: "MeTable",
  components: {
    RenderCell
  },
  props: {
    tableRef: {
      type: String,
      default: () => "tableRef"
    },
    loading: { type: Boolean, default: false },
    rowKey: { type: String },
    tableData: { type: Array, default: () => [] },
    height: {},
    maxHeight: {},
    expandAll: {
      type: Boolean,
      default: false
    },
    stripe: {
      type: Boolean,
      default: false
    },
    border: {
      type: Boolean,
      default: true
    },
    size: {
      type: String
    },
    fit: {
      type: Boolean,
      default: true
    },
    showHeader: {
      type: Boolean,
      default: true
    },
    headerCellStyle: {
      default() {
        return { color: "#333", backgroundColor: "#eee" };
      }
    },
    showSelection: { type: Boolean, default: false },
    selectionFixed: {
      type: Boolean,
      default: true
    },
    tableColumns: { type: Array, default: () => [] }
  },
  methods: {
    handleSelectAll(selection) {
      this.$emit("handleSelectAll", selection);
    },
    handleSelectChange(val, row) {
      this.$emit("handleSelectChange", val, row);
    },
    handleRowClick(row, column, event) {
      // 添加空值检查，避免访问未定义的属性
      if (!row || !column || !column.label || column.label === "操作") {
        return;
      }
      this.$refs[this.tableRef].toggleRowSelection(row);
    }
    // // 获取所有的复选框选项
    // getAllSelections() {
    //   return this.$refs[this.tableRef].selection;
    // },
    // // 用于多选表格默认选中
    // toggleSelection(selectList, field) {
    //   if (selectList.length > 0) {
    //     selectList.forEach((item) => {
    //       this.tableData.forEach((row) => {
    //         if (item === row[field]) {
    //           this.$refs[this.tableRef].toggleRowSelection(row);
    //         }
    //       });
    //     });
    //   } else {
    //     this.$refs.table.clearSelection();
    //   }
    // },
  }
};
</script>
