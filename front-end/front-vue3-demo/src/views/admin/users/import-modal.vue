<template>
  <el-dialog v-model="visible" title="批量导入用户" width="650px" :close-on-click-modal="false" @closed="handleClosed">
    <div class="mb-4">
      <p class="mb-3 text-sm text-slate-500">请上传 Excel(.xlsx) 或 CSV 文件，文件需包含以下列：用户名、密码、真实姓名、角色、用户类型、手机号、邮箱、学院</p>
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.csv"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        drag
      >
        <el-icon class="text-3xl text-slate-400"><UploadFilled /></el-icon>
        <div class="mt-2 text-sm text-slate-500">
          将文件拖到此处，或<em class="text-primary">点击上传</em>
        </div>
        <template #tip>
          <div class="mt-1 text-xs text-slate-400">仅支持 .xlsx 和 .csv 格式</div>
        </template>
      </el-upload>
    </div>

    <!-- Preview table placeholder -->
    <div v-if="previewList.length > 0" class="mb-4">
      <p class="mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">
        预览数据（共 {{ previewList.length }} 条）
      </p>
      <div class="max-h-60 overflow-auto rounded-lg border">
        <el-table :data="previewList.slice(0, 10)" size="small" stripe>
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="80" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">{{ RoleMap[row.role] || row.role }}</template>
          </el-table-column>
          <el-table-column prop="userType" label="类型" width="80" />
          <el-table-column prop="college" label="学院" min-width="120" />
        </el-table>
      </div>
      <p v-if="previewList.length > 10" class="mt-1 text-xs text-slate-400">仅显示前 10 条，共 {{ previewList.length }} 条</p>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="importing" :disabled="previewList.length === 0" @click="handleImport">
        确认导入
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { UploadInstance } from 'element-plus'
import { batchImportUsers } from '@/api/user/api'
import { meMsgSuccess } from '@/utils/modal'
import { RoleMap } from './data'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const uploadRef = ref<UploadInstance>()
const importing = ref(false)
const previewList = ref<any[]>([])

function handleFileChange(file: any) {
  const raw = file.raw as File
  if (!raw) return

  // Simple CSV/Excel parsing simulation
  // In production, use xlsx library to parse
  const reader = new FileReader()
  reader.onload = (e) => {
    const text = e.target?.result as string
    const lines = text.split('\n').filter((l) => l.trim())
    if (lines.length < 2) return

    const headers = lines[0].split(',').map((h) => h.trim())
    previewList.value = lines.slice(1).map((line) => {
      const values = line.split(',').map((v) => v.trim().replace(/"/g, ''))
      const obj: any = {}
      headers.forEach((h, i) => {
        obj[h] = values[i] || ''
      })
      return {
        username: obj['用户名'] || obj['username'] || '',
        password: obj['密码'] || obj['password'] || '123456',
        realName: obj['真实姓名'] || obj['realName'] || obj['姓名'] || '',
        role: obj['角色'] || obj['role'] || 'STUDENT',
        userType: obj['用户类型'] || obj['userType'] || 'STUDENT',
        phone: obj['手机号'] || obj['phone'] || '',
        email: obj['邮箱'] || obj['email'] || '',
        college: obj['学院'] || obj['college'] || '',
      }
    }).filter((item) => item.username)
  }
  reader.readAsText(raw)
}

function handleFileRemove() {
  previewList.value = []
}

async function handleImport() {
  if (previewList.value.length === 0) return
  importing.value = true
  try {
    const res = await batchImportUsers({ users: previewList.value })
    if (res.code === 0) {
      meMsgSuccess({ message: `成功导入 ${res.data.success} 条，失败 ${res.data.fail} 条` })
      visible.value = false
      emit('success')
    }
  } catch { /* handled */ } finally {
    importing.value = false
  }
}

function handleClosed() {
  previewList.value = []
}
</script>
