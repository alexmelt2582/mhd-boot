<template>
  <div>
    <PageHeader title="文件管理" description="管理系统上传的文件，包括图片、文档和其他资源文件">
      <template #actions>
        <el-upload
          :show-file-list="false"
          :before-upload="handleBeforeUpload"
          :http-request="handleUpload"
          accept="image/*,.pdf,.xlsx,.docx,.txt,.zip"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            上传文件
          </el-button>
        </el-upload>
      </template>
    </PageHeader>

    <!-- Filter tabs -->
    <div class="mb-6">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane
          v-for="tab in fileTypeTabs"
          :key="tab.value"
          :label="tab.label"
          :name="tab.value"
        />
      </el-tabs>
    </div>

    <!-- Grid -->
    <div v-loading="loading" class="min-h-[300px]">
      <div v-if="list.length === 0 && !loading" class="py-16">
        <EmptyState description="暂无文件" />
      </div>

      <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        <div
          v-for="file in list"
          :key="file.id"
          class="group rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition-all hover:border-primary/30 hover:shadow-md dark:border-white/5 dark:bg-slate-900"
        >
          <!-- File icon -->
          <div class="mb-3 flex justify-center">
            <div
              :class="[
                'flex h-20 w-20 items-center justify-center rounded-xl',
                file.fileType === 'IMAGE'
                  ? 'bg-blue-50 dark:bg-blue-500/10'
                  : file.fileType === 'DOCUMENT'
                    ? 'bg-emerald-50 dark:bg-emerald-500/10'
                    : 'bg-slate-100 dark:bg-slate-800',
              ]"
            >
              <el-icon :size="36" :color="file.fileType === 'IMAGE' ? '#3b82f6' : file.fileType === 'DOCUMENT' ? '#10b981' : '#94a3b8'">
                <component :is="file.fileType === 'IMAGE' ? 'PictureFilled' : file.fileType === 'DOCUMENT' ? 'Document' : 'FolderOpened'" />
              </el-icon>
            </div>
          </div>

          <!-- File info -->
          <div class="mb-3 text-center">
            <p class="truncate text-sm font-medium text-slate-900 dark:text-slate-100" :title="file.fileName">
              {{ file.fileName }}
            </p>
            <p class="mt-1 text-xs text-slate-400 dark:text-slate-500">
              {{ formatFileSize(file.fileSize) }}
            </p>
          </div>

          <!-- Meta -->
          <div class="mb-3 flex items-center justify-center gap-2 text-xs text-slate-400 dark:text-slate-500">
            <span>上传者: {{ file.uploaderName || '-' }}</span>
            <span>|</span>
            <span>{{ formatDate(file.createTime) }}</span>
          </div>

          <!-- Actions -->
          <div class="flex items-center justify-center gap-1">
            <el-button
              v-if="file.fileType === 'IMAGE'"
              type="primary"
              link
              size="small"
              @click="openPreview(file)"
            >
              预览
            </el-button>
            <el-button type="primary" link size="small" @click="handleDownload(file.id)">
              下载
            </el-button>
            <el-popconfirm title="确定删除该文件？" confirm-button-text="删除" @confirm="handleDelete(file.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="total > 0" class="mt-6 flex justify-center">
      <el-pagination
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        background
        layout="prev, pager, next, total"
        @current-change="fetchList"
      />
    </div>

    <!-- Image preview dialog -->
    <el-dialog v-model="previewVisible" title="图片预览" width="700px" center>
      <div class="flex items-center justify-center">
        <img
          :src="previewUrl"
          :alt="previewName"
          class="max-h-[60vh] max-w-full rounded object-contain"
          @error="onImgError"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { getFiles, uploadFile, deleteFile, downloadFile } from '@/api/file/api'
import type { FileVO } from '@/api/file/type'
import { defaultFileQuery, fileTypeTabs } from './data'
import { formatFileSize } from '@/utils/numberFormat'
import { formatDate } from '@/utils/date'
import { meMsgSuccess, meMsgError } from '@/utils/modal'
import type { UploadRequestOptions } from 'element-plus'

const loading = ref(false)
const list = ref<FileVO[]>([])
const total = ref(0)
const activeTab = ref('')

const query = reactive({ ...defaultFileQuery })

// Preview
const previewVisible = ref(false)
const previewUrl = ref('')
const previewName = ref('')

function onTabChange(val: string | number) {
  query.fileType = String(val || '')
  doSearch()
}

function doSearch() {
  query.page = 1
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getFiles({
      page: query.page,
      pageSize: query.pageSize,
      fileType: query.fileType || undefined,
    })
    if (res.code === 0) {
      list.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function handleBeforeUpload(file: File) {
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    meMsgError({ message: '文件大小不能超过50MB' })
    return false
  }
  return true
}

async function handleUpload(options: UploadRequestOptions) {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await uploadFile(formData)
    if (res.code === 0) {
      meMsgSuccess({ message: '上传成功' })
      fetchList()
    }
  } catch { /* handled */ }
}

async function handleDownload(id: number) {
  try {
    const res = await downloadFile(id)
    if (res.code === 0 && res.data) {
      window.open(res.data.url, '_blank')
    }
  } catch { /* handled */ }
}

function openPreview(file: FileVO) {
  previewUrl.value = file.fileUrl
  previewName.value = file.fileName
  previewVisible.value = true
}

function onImgError() {
  meMsgError({ message: '图片加载失败' })
}

async function handleDelete(id: number) {
  try {
    const res = await deleteFile(id)
    if (res.code === 0) {
      meMsgSuccess({ message: '删除成功' })
      fetchList()
    }
  } catch { /* handled */ }
}

onMounted(() => fetchList())
</script>
