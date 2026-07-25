<template>
  <el-form
    class="search-form-container"
    v-if="showSearch"
    :ref="searchFormRef"
    :model="searchParams"
    :rules="searchRules"
    :inline="inline"
    :label-position="labelPosition"
    :label-width="labelWidth"
    :label-suffix="labelSuffix"
    :hide-required-asterisk="hideRequiredAsterisk"
    :show-message="showMessage"
    :inline-message="inlineMessage"
    :status-icon="statusIcon"
    :validate-on-rule-change="validateOnRuleChange"
    :size="size"
    :disabled="disabled"
    @submit.native.prevent
  >
    <template v-for="(item, index) in searchConfig">
      <!-- 设置slot插槽 -->
      <slot v-if="item.slot" :name="item.slot" />
      <!-- 输入框 -->
      <template v-else-if="item.type === 'text'">
        <el-form-item
          :key="index"
          :label="item.label"
          :label-width="item.labelWidth"
        >
          <el-input
            v-model.trim="modelData[item.field]"
            :placeholder="
              item.placeholder ? item.placeholder : '请输入' + item.label
            "
            :clearable="item.clearable ? item.clearable : true"
            @keyup.enter.native="searchSubmit"
          />
        </el-form-item>
      </template>
      <!-- 普通下拉框 -->
      <template v-else-if="item.type === 'select'">
        <el-form-item
          :key="index"
          :label="item.label"
          :label-width="item.labelWidth"
        >
          <el-select
            v-model="modelData[item.field]"
            :placeholder="
              item.placeholder ? item.placeholder : '请选择' + item.label
            "
            :clearable="item.clearable ? item.clearable : true"
            :filterable="item.filterable ? item.filterable : false"
            @change="selectChange($event, index, item.field)"
          >
            <el-option
              v-for="(option, optionIndex) in item.options"
              :key="optionIndex"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </template>
      <!-- 日期范围 -->
      <template v-else-if="item.type === 'dateRange'">
        <el-form-item
          :key="index"
          :label="item.label"
          :label-width="item.labelWidth"
        >
          <el-date-picker
            v-model="modelData[item.field[0]]"
            :type="item.datePickerType ? item.datePickerType : 'date'"
            :value-format="item.valueFormat"
            :format="item.labelFormat"
            :placeholder="item.placeholder ? item.placeholder : '开始时间'"
            :picker-options="beginDateRange(item)"
          >
          </el-date-picker>
          <span class="linkContent">-</span>
          <el-date-picker
            v-model="modelData[item.field[1]]"
            :type="item.datePickerType ? item.datePickerType : 'date'"
            :value-format="item.valueFormat"
            :format="item.labelFormat"
            :placeholder="item.placeholder ? item.placeholder : '结束时间'"
            :picker-options="endDateRange(item)"
          >
          </el-date-picker>
        </el-form-item>
      </template>
    </template>
    <el-form-item>
      <el-button
        type="primary"
        icon="el-icon-search"
        @click="searchSubmit"
        v-if="showSearchButton"
        >搜索
      </el-button>
      <el-button
        icon="el-icon-refresh"
        @click="searchReset(searchFormRef)"
        v-if="showResetButton"
        >重置
      </el-button>
      <template v-for="item in buttonConfig">
        <!-- 设置slot插槽 -->
        <slot v-if="item.slot" :name="item.slot" />
      </template>
    </el-form-item>
  </el-form>
</template>

<script>
import { cloneDeep } from "lodash";

/**
 * 组件描述：
 *  submit.native.prevent 防止只有一个input时，点击回车触发页面整体刷新
 * Props 属性：
 * - showSearch: 是否显示搜索框。默认 true
 * - searchFormRef: 表单引用名称。默认 "searchFormRef"
 * - searchParams: 搜索参数。必填。默认 {}
 * - searchRules: 表单验证规则。默认 {}
 * - inline: 是否为行内表单。默认 false
 * - labelPosition: 标签位置（right/left/top）。默认 "right"
 * - labelWidth: 标签宽度（例如：80px）。默认 "auto"
 * - labelSuffix: 标签后缀。默认 ""
 * - hideRequiredAsterisk: 是否隐藏必填星号。默认 false
 * - showMessage: 是否显示校验错误信息。默认 true
 * - inlineMessage: 是否以行内方式显示校验错误信息。默认 false
 * - statusIcon: 是否在输入框中显示校验结果反馈图标。默认 false
 * - validateOnRuleChange: 是否在规则改变后立即触发校验。默认 true
 * - size: 表单内组件的尺寸（medium/small/mini）。默认 "small"
 * - disabled: 是否禁用表单内的所有组件。默认 false
 * - searchConfig: 搜索配置项。必填。默认 []
 * - buttonConfig: 按钮配置项。默认 []
 * - showSearchButton: 是否显示搜索按钮。默认 true
 * - showResetButton: 是否显示重置按钮。默认 true
 */
export default {
  name: "MeSearchForm",
  props: {
    showSearch: { type: Boolean, default: true },
    searchFormRef: { type: String, default: "searchFormRef" },
    searchParams: {
      type: Object,
      required: true,
      default: () => {}
    },
    searchRules: {
      type: Object,
      default: () => {}
    },
    inline: { type: Boolean, default: true },
    labelPosition: {
      type: String,
      default: () => "right"
    },
    labelWidth: {
      type: String,
      default: () => "auto"
    },
    labelSuffix: {
      type: String,
      default: () => ""
    },
    hideRequiredAsterisk: {
      type: Boolean,
      default: false
    },
    showMessage: {
      type: Boolean,
      default: true
    },
    inlineMessage: {
      type: Boolean,
      default: false
    },
    statusIcon: {
      type: Boolean,
      default: false
    },
    validateOnRuleChange: {
      type: Boolean,
      default: true
    },
    size: { type: String, default: "small" },
    disabled: {
      type: Boolean,
      default: false
    },
    searchConfig: {
      type: Array,
      default: () => []
    },
    buttonConfig: {
      type: Array,
      default: () => {}
    },
    showSearchButton: {
      type: Boolean,
      default: true
    },
    showResetButton: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    modelData: {
      get() {
        return this.searchParams;
      },
      set(value) {
        this.$emit("update:searchParams", value);
      }
    }
  },
  data() {
    return {
      defaultSearchParams: {} // 默认搜索参数
    };
  },
  created() {
    this.defaultSearchParams = cloneDeep(this.searchParams);
  },
  methods: {
    searchSubmit() {
      this.$emit("handleSearch");
    },
    searchReset(formRef) {
      this.$refs[formRef].resetFields();
      Object.keys(this.modelData).forEach(key => {
        this.modelData[key] = this.defaultSearchParams[key];
      });
      this.$emit("handleReset", this.$refs[formRef]);
    },
    selectChange(value, index, field) {
      this.$emit("selectChange", value, index, field);
    },
    // 时间范围开始设置disabled
    beginDateRange(item) {
      const _self = this;
      return {
        disabledDate(time) {
          if (_self.modelData[item.field[1]]) {
            // 如果结束时间不为空，则小于结束时间
            return (
              new Date(_self.modelData[item.field[1]]).getTime() <
              time.getTime()
            );
          }
        }
      };
    },
    // 时间范围结束设置disabled
    endDateRange(item) {
      const _self = this;
      return {
        disabledDate(time) {
          if (_self.modelData[item.field[0]]) {
            // 如果开始时间不为空，则结束时间大于开始时间
            return (
              new Date(_self.modelData[item.field[0]]).getTime() >
              time.getTime()
            );
          }
        }
      };
    }
  }
};
</script>

<style scoped>
.search-form-container {
  padding: 0 12px 0 12px;
}
</style>
