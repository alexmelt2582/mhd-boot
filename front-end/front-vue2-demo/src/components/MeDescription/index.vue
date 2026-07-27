<template>
  <el-dialog
    :title="title"
    :visible.sync="visible"
    :width="width"
    :append-to-body="appendToBody"
  >
    <div style="margin: 20px 0">
      <el-descriptions :border="border" :column="column">
        <template v-for="(item, index) in descriptionConfig">
          <slot v-if="item.slot" :name="item.slot" :data="modelData"></slot>
          <el-descriptions-item
            v-else-if="item.render"
            :key="index"
            :label="item.label"
            :span="item.span"
          >
            <render-cell :render="item.render" :data="modelData" />
          </el-descriptions-item>
          <el-descriptions-item
            v-else-if="item.formatter"
            :key="index"
            :label="item.label"
            :span="item.span"
          >
            {{
              item.formatter(
                modelData,
                item,
                modelData[item.field] || item.defaultValue,
                index
              )
            }}
          </el-descriptions-item>
          <el-descriptions-item
            v-else
            :key="index"
            :label="item.label"
            :span="item.span"
          >
            {{ modelData[item.field] || item.defaultValue }}
          </el-descriptions-item>
        </template>
      </el-descriptions>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button @click="visible = false">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
/**
 * Props 属性：
 * - title: dialog 标题。必填
 * - open: 是否打开 dialog。默认 false
 * - width: dialog 宽度。默认 800px
 * - appendToBody: 是否将 dialog 附加到 body 上。默认 true
 * - border: 是否显示边框。默认 true
 * - column: 列数。默认 2
 * - descriptionConfig
 * - descriptionData
 */
// 辅助组件，用于渲染render函数
const RenderCell = {
  functional: true,
  props: {
    render: Function,
    data: Object
  },
  render: (h, ctx) => {
    return ctx.props.render(h, ctx.props.data);
  }
};
export default {
  name: "MeDescription",
  components: {
    RenderCell
  },
  props: {
    title: { type: String, required: true },
    open: { type: Boolean, default: false },
    width: { type: String, default: "600px" },
    appendToBody: { type: Boolean, default: true },
    border: { type: Boolean, default: true },
    column: { type: Number, default: 2 },
    descriptionConfig: { type: Array, required: true, default: () => [] },
    descriptionData: {
      required: true,
      type: Object,
      default: () => {
        return {};
      }
    }
  },
  computed: {
    visible: {
      get() {
        return this.open;
      },
      set(val) {
        this.$emit("update:open", val);
      }
    },
    modelData: {
      get() {
        return this.descriptionData;
      },
      set(value) {
        this.$emit("update:descriptionData", value);
      }
    }
  }
};
</script>

<style lang="scss">
.dialog-footer {
  text-align: right;
}
</style>
