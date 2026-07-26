<template>
  <div
    class="flex min-h-screen items-center justify-center bg-slate-50 px-4 transition-colors duration-300 dark:bg-slate-950">
    <div
      class="pointer-events-none fixed -left-[10%] -top-[20%] z-0 h-[50%] w-[50%] rounded-full bg-secondary/10 blur-[120px] dark:bg-secondary/20"></div>
    <div
      class="pointer-events-none fixed -bottom-[20%] -right-[10%] z-0 h-[50%] w-[50%] rounded-full bg-primary/10 blur-[120px] dark:bg-primary/20"></div>

    <div class="relative z-10 w-full max-w-md">
      <div class="mb-8 text-center">
        <div
          class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-secondary shadow-xl shadow-primary/20">
          <svg class="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-slate-900 dark:text-slate-100">管理后台</h1>
        <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">图书馆管理员登录</p>
      </div>

      <el-card
        class="!border-slate-200 !bg-white/80 !shadow-xl backdrop-blur-sm dark:!border-white/5 dark:!bg-slate-900/80">
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="管理员账号" clearable>
              <template #prefix>
                <el-icon>
                  <User/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password
                      clearable>
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="w-full" :loading="loading" @click="handleLogin">
              管理员登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="text-center">
          <RouterLink to="/login" class="text-sm text-primary hover:underline">返回用户登录
          </RouterLink>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {login} from '@/api/auth/api'
import {useUserStore} from '@/store/modules/user'
import {useMessage} from "@/hooks/message.ts";

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456',
})

const rules: FormRules = {
  username: [{required: true, message: '请输入管理员账号', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const {data} = await login(form);
    const {role} = data.user;
    const res = await login(form)
    if (role !== 'SYS_ADMIN' && role !== 'LIB_ADMIN') {
      useMessage().error('非管理员账号，无法登录管理后台');
      return;
    }
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.user)
    useMessage().success('登录成功');
    router.push('/admin/dashboard')
  } catch (error: any) {
    console.error('登录失败:', error.message);
  } finally {
    loading.value = false
  }
}
</script>
