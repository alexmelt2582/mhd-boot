<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑用户' : '新增用户'" width="580px" :close-on-click-modal="false" @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="pr-4">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="用户名/学工号" :disabled="isEdit" maxlength="30" />
      </el-form-item>
      <el-form-item v-if="!isEdit" label="密码" prop="password">
        <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password maxlength="30" />
      </el-form-item>
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="20" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="角色" prop="role">
            <el-select v-model="form.role" class="w-full">
              <el-option v-for="o in RoleOptions.filter(e => e.value)" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用户类型" prop="userType">
            <el-select v-model="form.userType" class="w-full">
              <el-option v-for="o in UserTypeOptions.filter(e => e.value)" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="手机号" maxlength="11" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="邮箱地址" maxlength="50" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="学院">
        <el-select v-model="form.college" class="w-full" placeholder="请选择学院" clearable filterable>
          <el-option v-for="o in CollegeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { createUser, updateUser } from '@/api/user/api'
import { meMsgSuccess } from '@/utils/modal'
import { RoleOptions, UserTypeOptions, CollegeOptions, defaultForm } from './data'

const props = defineProps<{
  modelValue: boolean
  editingId: number | null
  editData: any
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const isEdit = computed(() => !!props.editingId)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({ ...defaultForm })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
}

watch(
  () => props.editData,
  (val) => {
    if (val) {
      form.username = val.username || ''
      form.password = ''
      form.realName = val.realName || ''
      form.role = val.role || 'STUDENT'
      form.userType = val.userType || 'STUDENT'
      form.phone = val.phone || ''
      form.email = val.email || ''
      form.college = val.college || ''
    } else {
      Object.assign(form, defaultForm)
    }
  },
  { immediate: true },
)

function handleClosed() {
  formRef.value?.resetFields()
  Object.assign(form, defaultForm)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser({
        id: props.editingId!,
        realName: form.realName.trim(),
        role: form.role,
        userType: form.userType,
        phone: form.phone.trim() || undefined,
        email: form.email.trim() || undefined,
        college: form.college || undefined,
      })
      meMsgSuccess({ message: '更新成功' })
    } else {
      await createUser({
        username: form.username.trim(),
        password: form.password,
        realName: form.realName.trim(),
        role: form.role,
        userType: form.userType,
        phone: form.phone.trim() || undefined,
        email: form.email.trim() || undefined,
        college: form.college || undefined,
      })
      meMsgSuccess({ message: '创建成功' })
    }
    visible.value = false
    emit('success')
  } catch { /* handled */ } finally {
    submitting.value = false
  }
}
</script>
