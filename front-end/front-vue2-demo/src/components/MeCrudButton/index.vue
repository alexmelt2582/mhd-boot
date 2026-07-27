<template>
  <div class="table-control-container">
    <!-- 左侧操作按钮组 -->
    <div class="left-buttons">
      <el-button
        v-if="showAddButton"
        plain
        type="primary"
        icon="el-icon-plus"
        @click="$emit('add')"
        >新增
      </el-button>
      <el-button
        v-if="showUpdateButton"
        plain
        type="primary"
        icon="el-icon-edit"
        :disabled="selections.length !== 1"
        @click="$emit('update', selections[0])"
      >
        编辑
      </el-button>
      <el-button
        v-if="showDeleteButton"
        plain
        type="primary"
        icon="el-icon-delete"
        :disabled="selections.length === 0"
        @click="$emit('delete', selections)"
      >
        删除
      </el-button>
      <el-button
        v-if="showUploadButton"
        plain
        type="primary"
        icon="el-icon-upload"
        @click="$emit('upload', selections)"
      >
        上传
      </el-button>
      <el-button
        v-if="showDownloadButton"
        plain
        type="primary"
        icon="el-icon-download"
        @click="$emit('download', selections)"
      >
        下载
      </el-button>
      <template v-for="item in leftButtonConfig">
        <slot v-if="item.slot" :name="item.slot" />
      </template>
    </div>

    <!-- 右侧功能按钮组 -->
    <div class="right-buttons">
      <el-tooltip
        v-if="showSearchButton"
        effect="dark"
        :content="showSearch ? '隐藏搜索' : '显示搜索'"
        placement="top"
      >
        <el-button
          circle
          icon="el-icon-search"
          @click="$emit('update:showSearch', !showSearch)"
        />
      </el-tooltip>

      <el-tooltip
        v-if="showRefreshButton"
        effect="dark"
        content="刷新"
        placement="top"
      >
        <el-button circle icon="el-icon-refresh" @click="$emit('refresh')" />
      </el-tooltip>
      <template v-for="item in rightButtonConfig">
        <slot v-if="item.slot" :name="item.slot" />
      </template>
    </div>
  </div>
</template>

<script>
/**
 * Props 属性：
 * - selections: 选中的行数据。默认 []
 * - showAddButton: 是否显示新增按钮。默认 true
 * - showUpdateButton: 是否显示编辑按钮。默认 true
 * - showDeleteButton: 是否显示删除按钮。默认 true
 * - showUploadButton: 是否显示上传按钮。默认 false
 * - showDownloadButton: 是否显示下载按钮。默认 false
 * - leftButtonConfig: 左侧按钮配置项。默认 []
 * - rightButtonConfig: 右侧按钮配置项。默认 []
 * - showSearchButton: 是否显示搜索按钮。默认 true
 * - showRefreshButton: 是否显示刷新按钮。默认 true
 * - showSearch: 是否显示搜索框（.sync修饰符）。默认 true
 */
export default {
  name: "MeCurdButton",
  props: {
    selections: { type: Array, default: () => [] },
    showAddButton: { type: Boolean, default: true },
    showUpdateButton: { type: Boolean, default: true },
    showDeleteButton: { type: Boolean, default: true },
    showUploadButton: { type: Boolean, default: false },
    showDownloadButton: { type: Boolean, default: false },
    leftButtonConfig: { type: Array, default: () => [] },
    rightButtonConfig: { type: Array, default: () => [] },
    showSearchButton: { type: Boolean, default: true },
    showRefreshButton: { type: Boolean, default: true },
    // 控制搜索框显隐（.sync修饰符）
    showSearch: { type: Boolean, default: true }
  }
};
</script>

<style scoped>
.table-control-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  margin-bottom: 12px;
}

.left-buttons {
  display: flex;
  gap: 8px;
}

.right-buttons {
  display: flex;
  gap: 8px;
}
</style>
