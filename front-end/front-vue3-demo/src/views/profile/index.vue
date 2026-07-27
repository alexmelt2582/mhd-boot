<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <PageHeader title="个人中心" description="管理您的个人信息和安全设置" />

    <div class="flex flex-col gap-6 lg:flex-row">
      <!-- Sidebar -->
      <div class="w-full shrink-0 lg:w-72">
        <div class="sticky top-8 space-y-4">
          <!-- Profile card -->
          <div class="rounded-2xl border border-slate-200 bg-white p-6 text-center dark:border-white/5 dark:bg-slate-900">
            <el-avatar :size="80" :src="userStore.userInfo?.avatar" class="mx-auto ring-2 ring-primary/20 ring-offset-4 ring-offset-white dark:ring-offset-slate-900">
              <span class="text-2xl font-bold">{{ userStore.userInfo?.realName?.charAt(0) || 'U' }}</span>
            </el-avatar>
            <h2 class="mt-4 text-lg font-bold text-slate-900 dark:text-slate-100">
              {{ userStore.userInfo?.realName || '未设置姓名' }}
            </h2>
            <div class="mt-1">
              <el-tag size="small" :type="userStore.role === 'STUDENT' ? 'success' : 'warning'" effect="light">
                {{ roleLabel(userStore.role) }}
              </el-tag>
            </div>
            <div class="mt-2 text-xs text-slate-400 dark:text-slate-500">
              {{ userStore.userInfo?.college || '未设置学院' }}
            </div>
          </div>

          <!-- Stats card -->
          <div class="rounded-2xl border border-slate-200 bg-white p-5 dark:border-white/5 dark:bg-slate-900">
            <h3 class="mb-4 text-xs font-semibold uppercase tracking-wider text-slate-400 dark:text-slate-500">账户概览</h3>
            <div class="space-y-3">
              <div class="flex items-center justify-between">
                <span class="text-sm text-slate-500 dark:text-slate-400">信用积分</span>
                <span class="font-bold text-primary">{{ userStore.userInfo?.creditScore ?? 0 }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-sm text-slate-500 dark:text-slate-400">本月预约</span>
                <span class="font-bold text-slate-700 dark:text-slate-300">{{ monthlyReservations }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-sm text-slate-500 dark:text-slate-400">注册时间</span>
                <span class="text-xs text-slate-600 dark:text-slate-400">{{ formatDate(userStore.userInfo?.createTime, 'YYYY-MM-DD') }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Main content -->
      <div class="flex-1 rounded-2xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
        <el-tabs v-model="activeTab" class="px-6 pt-4">
          <!-- Tab 1: Personal Info -->
          <el-tab-pane label="个人信息" name="info">
            <div class="max-w-lg py-2 pb-8">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="80px"
                label-position="top"
              >
                <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="profileForm.username" disabled />
                  </el-form-item>
                  <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
                  </el-form-item>
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
                  </el-form-item>
                  <el-form-item label="电子邮箱" prop="email">
                    <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
                  </el-form-item>
                  <el-form-item label="所属学院" prop="college">
                    <el-input v-model="profileForm.college" disabled />
                  </el-form-item>
                </div>
                <el-form-item>
                  <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- Tab 2: Change Password -->
          <el-tab-pane label="修改密码" name="password">
            <div class="max-w-md py-2 pb-8">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                label-position="top"
              >
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入原密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码（至少6位）"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { updateProfile, changePassword } from '@/api/auth/api'
import { formatDate } from '@/utils/date'
import { meMsgSuccess, meMsgError } from '@/utils/modal'
import PageHeader from '@/components/PageHeader.vue'

const userStore = useUserStore()
const activeTab = ref('info')

const monthlyReservations = ref(12)

function roleLabel(role: string): string {
  const map: Record<string, string> = {
    STUDENT: '学生',
    TEACHER: '教师',
    LIB_ADMIN: '图书管理员',
    SYS_ADMIN: '系统管理员',
  }
  return map[role] || role
}

// --- Profile form ---
const profileFormRef = ref<FormInstance>()
const profileLoading = ref(false)

const profileForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  college: '',
})

const profileRules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}

function initProfileForm() {
  const info = userStore.userInfo
  if (info) {
    profileForm.username = info.username || ''
    profileForm.realName = info.realName || ''
    profileForm.phone = info.phone || ''
    profileForm.email = info.email || ''
    profileForm.college = info.college || ''
  }
}

async function handleUpdateProfile() {
  if (!profileFormRef.value) return
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  profileLoading.value = true
  try {
    const res = await updateProfile({
      realName: profileForm.realName,
      phone: profileForm.phone,
      email: profileForm.email,
    })
    if (res.code === 0) {
      if (userStore.userInfo) {
        userStore.userInfo.realName = profileForm.realName
        userStore.userInfo.phone = profileForm.phone
        userStore.userInfo.email = profileForm.email
      }
      meMsgSuccess({ message: '个人信息更新成功' })
    }
  } catch {
    meMsgError({ message: '更新失败，请稍后重试' })
  } finally {
    profileLoading.value = false
  }
}

// --- Password form ---
const passwordFormRef = ref<FormInstance>()
const passwordLoading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

async function handleChangePassword() {
  if (!passwordFormRef.value) return
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    const res = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    if (res.code === 0) {
      meMsgSuccess({ message: '密码修改成功，请妥善保管新密码' })
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordFormRef.value.resetFields()
    }
  } catch {
    meMsgError({ message: '密码修改失败，请检查原密码是否正确' })
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  initProfileForm()
})
</script>
