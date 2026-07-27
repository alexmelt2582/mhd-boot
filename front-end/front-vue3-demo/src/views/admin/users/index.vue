<template>
  <div>
    <PageHeader title="用户管理" description="管理系统用户，包括创建、编辑、启用/禁用和角色分配">
      <template #actions>
        <el-button @click="openImportModal">批量导入</el-button>
        <el-button type="primary" @click="openModal(null)">新增用户</el-button>
      </template>
    </PageHeader>

    <SearchBar :config="userSearchConfig" @search="handleSearch"/>

    <!-- Table -->
    <div class="rounded-xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="realName" label="姓名" width="90"/>
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="roleTagType(row.role)">{{
                RoleMap[row.role] || row.role
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户类型" width="80">
          <template #default="{ row }">
            {{ row.userType === 'STUDENT' ? '学生' : row.userType === 'TEACHER' ? '教师' : '其他' }}
          </template>
        </el-table-column>
        <el-table-column prop="college" label="学院" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="creditScore" label="信用分" width="80">
          <template #default="{ row }">
            <span
              :class="row.creditScore >= 80 ? 'text-emerald-500' : row.creditScore >= 60 ? 'text-amber-500' : 'text-red-500'"
              class="font-bold">
              {{ row.creditScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="user"/>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openModal(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link size="small"
              @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="info" link size="small" @click="openRoleAssign(row)">分配角色
            </el-button>
            <el-popconfirm title="确认删除该用户？" confirm-button-text="确定"
                           @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="list.length === 0 && !loading">
        <EmptyState description="暂无用户数据"/>
      </div>
    </div>

    <Pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      @pagination="handlePagination"
    />

    <!-- CRUD Modal -->
    <UserModal v-model="modalVisible" :editing-id="editingId" :edit-data="editData"
               @success="fetchList"/>

    <!-- Import Modal -->
    <ImportModal v-model="importVisible" @success="fetchList"/>

    <!-- Role Assign Dialog -->
    <el-dialog v-model="roleVisible" title="分配角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前用户">
          <span class="text-sm font-medium">{{ roleTarget?.realName }} ({{
              roleTarget?.username
            }})</span>
        </el-form-item>
        <el-form-item label="当前角色">
          <el-tag size="small">{{ RoleMap[roleTarget?.role] || roleTarget?.role }}</el-tag>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="assignRoleValue" class="w-full">
            <el-option v-for="o in RoleOptions.filter(e => e.value)" :key="o.value" :label="o.label"
                       :value="o.value"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!assignRoleValue" @click="handleAssignRole">确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type {UserManageVO} from '@/api/user/type'
import {assignRole, deleteUser, getUserList, updateUserStatus} from '@/api/user/api'
import {meMsgSuccess} from '@/utils/modal'
import {defaultQuery, RoleMap, RoleOptions, userSearchConfig} from './data'
import UserModal from './modal.vue'
import ImportModal from './import-modal.vue'
import {ref} from "vue";

const loading = ref(false)
const list = ref<UserManageVO[]>([])
const total = ref(0)
const query = reactive({...defaultQuery})
const page = ref(1)
const pageSize = ref(10)
let lastSearchParams: Record<string, any> = {}

const modalVisible = ref(false)
const editingId = ref<number | null>(null)
const editData = ref<UserManageVO | null>(null)

const importVisible = ref(false)

// Role assign
const roleVisible = ref(false)
const roleTarget = ref<UserManageVO | null>(null)
const assignRoleValue = ref('')

function roleTagType(role: string) {
  const map: Record<string, string> = {
    SYS_ADMIN: 'danger',
    LIB_ADMIN: 'warning',
    TEACHER: 'primary',
    STUDENT: 'success',
    OTHER: 'info',
  }
  return map[role] || 'info'
}

function openModal(row: UserManageVO | null) {
  if (row) {
    editingId.value = row.id
    editData.value = {...row}
  } else {
    editingId.value = null
    editData.value = null
  }
  modalVisible.value = true
}

function openImportModal() {
  importVisible.value = true
}

function openRoleAssign(row: UserManageVO) {
  roleTarget.value = row
  assignRoleValue.value = row.role
  roleVisible.value = true
}

async function handleAssignRole() {
  if (!roleTarget.value || !assignRoleValue.value) return
  try {
    await assignRole({userId: roleTarget.value.id, role: assignRoleValue.value})
    meMsgSuccess({message: '角色已更新'})
    roleVisible.value = false
    fetchList()
  } catch { /* handled */
  }
}

async function toggleStatus(row: UserManageVO) {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '启用' : '禁用'
  try {
    await updateUserStatus({userId: row.id, status: newStatus})
    meMsgSuccess({message: `已${label}`})
    fetchList()
  } catch { /* handled */
  }
}

async function handleDelete(id: number) {
  try {
    await deleteUser(id)
    meMsgSuccess({message: '删除成功'})
    fetchList()
  } catch { /* handled */
  }
}

function handleSearch(params: Record<string, any>) {
  lastSearchParams = params
  page.value = 1
  fetchList()
}

const handlePagination = (payload: { page: number; pageSize: number }) => {
  page.value = payload.page
  pageSize.value = payload.pageSize
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const q: any = {
      page: page.value,
      pageSize: pageSize.value,
      ...query}
    if (q.role === '') q.role = undefined
    const res = await getUserList(q)
    if (res.code === "0") {
      list.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchList())
</script>
