<template>
  <el-dialog v-model="dialogVisible" :title="title" width="500px" @close="handleClose">
    <el-form
      ref="formRef"
      :model="formData"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="字典名称" prop="dictName">
        <el-input v-model="formData.dictName" placeholder="请输入字典名称" />
      </el-form-item>
      <el-form-item label="字典类型" prop="dictType">
        <el-input v-model="formData.dictType" placeholder="请输入字典类型" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio label="0">正常</el-radio>
          <el-radio label="1">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="loading" :disabled="loading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import type { FormInstance } from 'element-plus';
import { dictTypeForm, dictTypeRules } from './data';
import {useMessage} from "@/hooks/message.ts";

const dialogVisible = ref(false);
const formRef = ref<FormInstance>();
const formData = reactive({ ...dictTypeForm });
const isEdit = ref(false);
const loading = ref(false); // 新增加载状态

const title = computed(() => (isEdit.value ? '编辑字典类型' : '新增字典类型'));

// 打开弹窗
const openDialog = (id?: string | number) => {
  dialogVisible.value = true;
  if (id) {
    isEdit.value = true;
    Object.assign(formData, { id, dictName: '测试', dictType: 'test', status: '0' });
  } else {
    isEdit.value = false;
    Object.assign(formData, dictTypeForm);
  }
};

// 关闭弹窗
const handleClose = () => {
  if (loading.value) return; // 防抖，提交中不可关闭
  dialogVisible.value = false;
  formRef.value?.resetFields();
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value || loading.value) return; // 双重校验防止重复点击
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        if (isEdit.value) {
          // await updateDictType(formData);
          useMessage().success('修改成功');
        } else {
          // await addDictType(formData);
          useMessage().success('新增成功');
        }
        dialogVisible.value = false;
        emit('refresh');
      } catch (err: any) {
        useMessage().error(err.msg || '操作失败');
      } finally {
        loading.value = false; // 无论如何都关闭加载状态
      }
    }
  });
};

// 暴露方法给父组件
defineExpose({ openDialog });
const emit = defineEmits(['refresh']);
</script>
