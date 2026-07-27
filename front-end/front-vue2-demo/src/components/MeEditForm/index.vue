<template>
  <el-dialog
    :title="title"
    :visible.sync="visible"
    :width="width"
    :append-to-body="appendToBody"
    :before-close="handleClose"
  >
    <el-form
      :ref="editFormRef"
      :model="modelData"
      :rules="editFormRules"
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
      <el-row :gutter="gutter">
        <template v-for="(item, index) in editConfig">
          <!-- slot 插槽 -->
          <slot
            v-if="item.slot"
            :name="item.slot"
            :show="shouldShowItem(item)"
          />
          <el-col
            v-else
            :key="index"
            :span="item.span ? item.span : 24"
            v-show="shouldShowItem(item)"
          >
            <!-- 输入框 -->
            <template v-if="item.type === 'text'">
              <el-form-item
                :key="index"
                :label="item.label"
                :prop="item.field"
                :label-width="item.labelWidth"
              >
                <el-input
                  v-model.trim="modelData[item.field]"
                  :placeholder="
                    item.placeholder ? item.placeholder : '请输入' + item.label
                  "
                  :disabled="item.disabled ? item.disabled : false"
                  :clearable="item.clearable ? item.clearable : true"
                  :show-password="item.showPassword ? item.showPassword : false"
                  :maxlength="item.maxlength"
                  :show-word-limit="
                    item.showWordLimit ? item.showWordLimit : false
                  "
                  @input="inputInput($event, index, item.field)"
                  @change="inputChange($event, index, item.field)"
                  @keyup.enter.native="editSubmit(editFormRef)"
                />
              </el-form-item>
            </template>
            <!-- 数字输入框 -->
            <template v-else-if="item.type === 'number'">
              <el-form-item
                :key="index"
                :label="item.label"
                :prop="item.field"
                :label-width="item.labelWidth"
              >
                <el-input-number
                  :style="item.style ? item.style : 'width: 100%'"
                  v-model.trim="modelData[item.field]"
                  :min="item.min ? item.min : 0"
                  :max="item.max"
                  :precision="item.precision"
                  :controls-position="item.position ? item.position : 'right'"
                  :placeholder="
                    item.placeholder ? item.placeholder : '请输入' + item.label
                  "
                  :disabled="item.disabled"
                  @input="inputInput($event, index, item.field)"
                  @change="inputChange($event, index, item.field)"
                />
              </el-form-item>
            </template>
            <!-- 文本域 -->
            <template v-else-if="item.type === 'textarea'">
              <el-form-item
                :key="index"
                :label="item.label"
                :prop="item.field"
                :label-width="item.labelWidth"
              >
                <el-input
                  v-model.trim="modelData[item.field]"
                  type="textarea"
                  :placeholder="
                    item.placeholder ? item.placeholder : '请输入' + item.label
                  "
                  :disabled="item.disabled ? item.disabled : false"
                  :autosize="{
                    minRows: item.minRows ? item.minRows : 4,
                    maxRows: item.maxRows ? item.maxRows : 6
                  }"
                />
              </el-form-item>
            </template>
            <!-- 普通下拉框 -->
            <template v-else-if="item.type === 'select'">
              <el-form-item
                :key="index"
                :label="item.label"
                :prop="item.field"
                :label-width="item.labelWidth"
              >
                <el-select
                  :style="item.style ? item.style : 'width: 100%'"
                  v-model="modelData[item.field]"
                  :multiple="item.multiple ? item.multiple : false"
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
            <!-- 普通单选框 -->
            <template v-else-if="item.type === 'radio'">
              <el-form-item
                :key="index"
                :label="item.label"
                :prop="item.field"
                :label-width="item.labelWidth"
              >
                <el-radio-group
                  :style="item.style ? item.style : 'width: 100%'"
                  v-model="modelData[item.field]"
                  @change="radioChange($event, index, item.field)"
                >
                  <template v-if="item.itemType && item.itemType === 'button'">
                    <el-radio-button
                      v-for="radioItem in item.options"
                      :key="radioItem.value"
                      :label="radioItem.value"
                      :disabled="radioItem.disabled"
                      >{{ radioItem.label }}
                    </el-radio-button>
                  </template>
                  <template v-else>
                    <el-radio
                      v-for="radioItem in item.options"
                      :key="radioItem.value"
                      :label="radioItem.value"
                      :disabled="radioItem.disabled"
                      >{{ radioItem.label }}
                    </el-radio>
                  </template>
                </el-radio-group>
              </el-form-item>
            </template>
            <!-- 日期/日期时间输入框 -->
            <template
              v-else-if="item.type === 'date' || item.type === 'datetime'"
            >
              <el-form-item
                :key="index"
                :label="item.label"
                :prop="item.field"
                :label-width="item.labelWidth"
              >
                <el-date-picker
                  :style="item.style ? item.style : 'width: 100%'"
                  :type="item.type"
                  v-model="modelData[item.field]"
                  :placeholder="
                    item.placeholder ? item.placeholder : '请选择' + item.label
                  "
                  :clearable="item.clearable ? item.clearable : true"
                  :disabled="item.disabled ? item.disabled : false"
                  :format="item.format"
                  :value-format="item.valueFormat"
                  @change="dateChange($event, index, item.field)"
                >
                </el-date-picker>
              </el-form-item>
            </template>
            <!-- 时间单选框 -->
            <template v-else-if="item.type === 'time'">
              <el-form-item
                :key="index"
                :label="item.label"
                :prop="item.field"
                :label-width="item.labelWidth"
              >
                <el-time-picker
                  :style="item.style ? item.style : 'width: 100%'"
                  v-model="modelData[item.field]"
                  :placeholder="
                    item.placeholder ? item.placeholder : '请选择' + item.label
                  "
                  :clearable="item.clearable ? item.clearable : true"
                  :disabled="item.disabled ? item.disabled : false"
                  :format="item.format"
                  :value-format="item.valueFormat"
                  @change="timeChange($event, index, item.field)"
                >
                </el-time-picker>
              </el-form-item>
            </template>
          </el-col>
        </template>
      </el-row>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="editSubmit(editFormRef)"
        >确 定
      </el-button>
      <el-button @click="resetSearch(editFormRef)">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { cloneDeep } from "lodash";

/**
 * Props 属性：
 * - title: dialog 标题。必填
 * - open: 是否打开 dialog。默认 false
 * - width: dialog 宽度。默认 800px
 * - appendToBody: 是否将 dialog 附加到 body 上。默认 true
 * - editFormRef: 表单引用名称。默认 editFormRef
 * - editFormData: 表单数据。必填。默认 {}
 * - editFormRules: 表单验证规则。默认 {}
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
 * - gutter: 栅格间隔。默认 20
 * - editConfig: 表单配置项。必填。默认 []
 */
export default {
  name: "MeEditForm",
  props: {
    title: { type: String, required: true },
    open: { type: Boolean, default: false },
    width: { type: String, default: "600px" },
    appendToBody: { type: Boolean, default: true },
    editFormRef: { type: String, default: "editFormRef" },
    editFormData: {
      required: true,
      type: Object,
      default: () => {
        return {};
      }
    },
    editFormRules: {
      type: Object,
      default: () => {
        return {};
      }
    },
    inline: { type: Boolean, default: false },
    labelPosition: { type: String, default: () => "left" },
    labelWidth: { type: String, default: () => "auto" },
    labelSuffix: { type: String, default: () => "" },
    hideRequiredAsterisk: { type: Boolean, default: false },
    showMessage: { type: Boolean, default: true },
    inlineMessage: { type: Boolean, default: false },
    statusIcon: { type: Boolean, default: false },
    validateOnRuleChange: { type: Boolean, default: true },
    size: { type: String, default: "small" },
    disabled: { type: Boolean, default: false },
    gutter: { type: Number, default: 20 },
    editConfig: { type: Array, required: true, default: () => [] }
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
        return this.editFormData;
      },
      set(value) {
        this.$emit("update:editFormData", value);
      }
    }
  },
  data() {
    return {
      defaultEditFormRules: [] // 存储默认的编辑表单规则
    };
  },
  created() {
    this.defaultEditFormRules = cloneDeep(this.editFormRules);
  },
  methods: {
    shouldShowItem(item) {
      // 如果没有依赖条件，则默认显示
      if (!item.dependency || item.dependency.length === 0) {
        if (item.field) {
          this.setValidationRules(item.field, true);
          //this.setDefaultValue(item);
        }
        return true;
      }
      // dependency 为二维数组，每个数组为一个组，组内的条件为与关系，组与组之间为或关系
      const dependencyGroups = item.dependency;
      // 遍历每个依赖组
      for (const group of dependencyGroups) {
        let groupSatisfied = true;
        // 检查组内的每个依赖项
        for (const dependency of group) {
          const fieldValue = this.modelData[dependency.field];
          // 1. 方式一：根据 dependency 中 func 字段，如果 func 是方法，则执行方法，如果返回值是 true 则显示，否则不显示
          if (dependency.func && typeof dependency.func === "function") {
            let param;
            if (!fieldValue) {
              param = undefined;
            } else {
              param = JSON.parse(JSON.stringify(fieldValue));
            }
            const funcResult = dependency.func(param);
            if (!funcResult) {
              groupSatisfied = false;
              break;
            }
            continue;
          }
          // 2. 检查字段值是否存在，方案一可能需要自行判断
          if (
            fieldValue === undefined ||
            fieldValue === null ||
            fieldValue === ""
          ) {
            groupSatisfied = false;
            break;
          }
          // 3. 方式二：根据 dependency 中 field 字段的值 是否在 values 中，如果在则显示，否则不显示
          if (
            dependency.values?.length > 0 &&
            !dependency.values.includes(fieldValue)
          ) {
            groupSatisfied = false;
            break;
          }
        }
        // 如果当前组满足则立即返回
        if (groupSatisfied) {
          // 如果 item 存在 field，则设置为必填并设置默认值
          if (item.field) {
            this.setValidationRules(item.field, true);
            this.setDefaultValue(item);
          }
          // 如果 item 存在 clearFields，则对应的字段设置为必填
          if (item.clearFields?.length > 0) {
            for (const tmpField of item.clearFields) {
              this.setValidationRules(tmpField, true);
            }
          }
          return true;
        }
      }
      // 所有组都不满足，清空字段值
      // 如果 item 存在 field，则清空字段值
      if (item.field) {
        // 只有当前字段有值时才清空，避免重复操作
        if (
          this.modelData[item.field] !== undefined &&
          this.modelData[item.field] !== null &&
          this.modelData[item.field] !== ""
        ) {
          this.modelData[item.field] = undefined;
        }
        this.setValidationRules(item.field, false);
      }
      // 如果 item 存在 clearFields，则清空字段值
      if (item.clearFields?.length > 0) {
        for (const tmpField of item.clearFields) {
          // 只有当前字段有值时才清空，避免重复操作
          if (
            this.modelData[tmpField] !== undefined &&
            this.modelData[tmpField] !== null &&
            this.modelData[tmpField] !== ""
          ) {
            this.modelData[tmpField] = undefined;
          }
          this.setValidationRules(tmpField, false);
        }
      }
      return false;
    },
    setValidationRules(field, isVisible) {
      // 根据 defaultEditFormRules 判断是否是必填，如果是，则进行动态修改，否则不修改
      const rules = this.editFormRules[field];
      if (rules === undefined) {
        return;
      }
      rules.forEach((rule, index) => {
        if (rule.required === undefined) {
          return;
        }
        let flag = this.defaultEditFormRules[field][index];
        if (flag) {
          rule.required = isVisible;
        }
      });
    },
    // 设置字段默认值
    setDefaultValue(item) {
      // 如果当前字段值为空且有默认值配置，则设置默认值
      const currentValue = this.modelData[item.field];
      // 只有在新增模式或字段值为空时才设置默认值
      if (
        currentValue === undefined ||
        currentValue === null ||
        currentValue === ""
      ) {
        if (item.defaultValue !== undefined) {
          this.modelData[item.field] = item.defaultValue;
        }
      }
    },
    handleClose() {
      this.resetSearch(this.editFormRef);
      this.$emit("handleClose");
    },
    editSubmit(formRef) {
      this.$emit("handleEdit", this.$refs[formRef]);
    },
    resetSearch(formRef) {
      this.$emit("handleReset", this.$refs[formRef]);
    },
    // input的input事件
    inputInput(value, index, field) {
      this.$emit("inputInput", value, index, field);
    },
    // input的change事件
    inputChange(value, index, field) {
      this.$emit("inputChange", value, index, field);
    },
    // select的change事件
    selectChange(value, index, field) {
      this.$emit("selectChange", value, index, field);
    },
    // radio的change事件
    radioChange(value, index, field) {
      this.$emit("radioChange", value, index, field);
    },
    // date的change事件
    dateChange(value, index, field) {
      this.$emit("dateChange", value, index, field);
    },
    // time的change事件
    timeChange(value, index, field) {
      this.$emit("timeChange", value, index, field);
    }
  }
};
</script>

<style lang="scss" scoped>
.footer-button {
  text-align: right;
}

/* 样式重置：设置非必填时前面添加空白符，保持与必填时一致 */
::v-deep .el-form-item:not(.is-required),
.el-form-item__label-wrap > .el-form-item__label:before,
::v-deep .el-form-item:not(.is-required) > .el-form-item__label:before {
  content: "\2002";
  color: transparent;
  margin-right: 4px;
}

/* 样式重置：设置必填项,但不显示*时前面添加空白符，保持与必填时一致 */
::v-deep
  .el-form-item.is-required.is-no-asterisk
  .el-form-item__label-wrap
  > .el-form-item__label:before,
::v-deep
  .el-form-item.is-required.is-no-asterisk
  > .el-form-item__label:before {
  content: "\2002";
  color: transparent;
  margin-right: 4px;
}

/* 样式重置：设置必填项，并且前面显示*时前面添加红色星号 */
::v-deep
  .el-form-item.is-required:not(.is-no-asterisk)
  .el-form-item__label-wrap
  > .el-form-item__label:before,
::v-deep
  .el-form-item.is-required:not(.is-no-asterisk)
  > .el-form-item__label:before {
  content: "*";
  color: #f56c6c;
  margin-right: 4px;
}
</style>
