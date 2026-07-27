<template>
  <el-dialog v-model="dialogVisible" :title="title" width="500px" @close="handleClose">
    <el-form
      ref="formRef"
      :model="formData"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="字典类型" prop="dictType">
        <el-input v-model="formData.dictType" disabled/>
      </el-form-item>
      <el-form-item label="字典标签" prop="dictLabel">
        <el-input v-model="formData.dictLabel" placeholder="请输入字典标签"/>
      </el-form-item>
      <el-form-item label="字典键值" prop="dictValue">
        <el-input v-model="formData.dictValue" placeholder="请输入字典键值"/>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" controls-position="right"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio label="0">正常</el-radio>
          <el-radio label="1">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注"/>
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
import {computed, reactive, ref} from 'vue';
import type {FormInstance} from 'element-plus';
import {dictItemForm} from './data';
import {useMessage} from "@/hooks/message.ts";
// import {addDictItem, updateDictItem} from '@/api/admin/system/dict/api';

const dialogVisible = ref(false);
const formRef = ref<FormInstance>();
const formData = reactive({...dictItemForm});
const isEdit = ref(false);
const loading = ref(false); // 新增加载状态

const title = computed(() => (isEdit.value ? '编辑字典数据' : '新增字典数据'));

// 打开弹窗
const openDialog = (data?: any) => {
  dialogVisible.value = true;
  if (data && data.id) {
    isEdit.value = true;
    Object.assign(formData, data);
  } else {
    isEdit.value = false;
    Object.assign(formData, dictItemForm);
    formData.dictType = data?.dictType || '';
  }
};

const handleClose = () => {
  if (loading.value) return;
  dialogVisible.value = false;
  formRef.value?.resetFields();
};

const submitForm = async () => {
  if (!formRef.value || loading.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        if (isEdit.value) {
          // await updateDictItem(formData);
          useMessage().success('修改成功');
        } else {
          // await addDictItem(formData);
          useMessage().success('新增成功');
        }
        dialogVisible.value = false;
        emit('refresh');
      } catch (err: any) {
        useMessage().error(err.msg || '操作失败');
      } finally {
        loading.value = false;
      }
    }
  });
};

defineExpose({openDialog});
const emit = defineEmits(['refresh']);
</script>
