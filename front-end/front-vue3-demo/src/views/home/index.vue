<template>
  <div>
    <!-- Hero section -->
    <section class="relative overflow-hidden bg-gradient-to-br from-primary/5 via-secondary/5 to-primary/5 px-4 py-20 dark:from-primary/10 dark:via-secondary/10 dark:to-primary/10">
      <div class="mx-auto max-w-7xl text-center">
        <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl dark:text-slate-100">
          智慧图书馆
          <span class="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">空间预约</span>
        </h1>
        <p class="mx-auto mt-6 max-w-2xl text-lg text-slate-500 dark:text-slate-400">
          轻松预约安静室、研讨室和多媒体空间，高效利用图书馆资源，开启智慧学习新体验
        </p>
        <div class="mt-8 flex justify-center gap-4">
          <el-button type="primary" size="large" round @click="router.push('/spaces')">
            立即预约
          </el-button>
          <el-button size="large" round @click="router.push('/my-reservations')">
            我的预约
          </el-button>
        </div>
      </div>
    </section>

    <!-- Stats -->
    <section class="-mt-10 px-4">
      <div class="mx-auto max-w-5xl">
        <div class="grid grid-cols-2 gap-4 md:grid-cols-4">
          <div v-for="stat in stats" :key="stat.label" class="rounded-2xl border border-slate-200 bg-white p-6 text-center shadow-sm transition-shadow hover:shadow-md dark:border-white/5 dark:bg-slate-900">
            <div class="text-2xl font-bold text-primary">{{ stat.value }}</div>
            <div class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Floor overview -->
    <section class="px-4 py-16">
      <div class="mx-auto max-w-7xl">
        <h2 class="mb-8 text-center text-2xl font-bold text-slate-900 dark:text-slate-100">楼层分布</h2>
        <div class="grid grid-cols-2 gap-4 md:grid-cols-4">
          <div v-for="floor in floors" :key="floor.name" @click="router.push({ path: '/spaces', query: { floor: floor.name } })" class="group cursor-pointer rounded-2xl border border-slate-200 bg-white p-6 text-center transition-all hover:-translate-y-1 hover:border-primary hover:shadow-lg dark:border-white/5 dark:bg-slate-900 dark:hover:border-primary">
            <div class="text-3xl font-bold text-slate-300 transition-colors group-hover:text-primary dark:text-slate-700">{{ floor.name }}</div>
            <div class="mt-2 font-medium text-slate-700 dark:text-slate-300">{{ floor.area }}</div>
            <div class="mt-2 flex justify-center gap-1">
              <span class="inline-block h-2 w-2 rounded-full bg-emerald-500" title="空闲"></span>
              <span class="inline-block h-2 w-2 rounded-full bg-amber-500" title="拥挤"></span>
              <span class="inline-block h-2 w-2 rounded-full bg-red-500" title="已满"></span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Quick filters -->
    <section class="bg-white px-4 py-16 dark:bg-slate-900">
      <div class="mx-auto max-w-7xl">
        <h2 class="mb-8 text-center text-2xl font-bold text-slate-900 dark:text-slate-100">按类型查找空间</h2>
        <div class="grid gap-6 md:grid-cols-3">
          <div v-for="type in spaceTypes" :key="type.value" @click="router.push({ path: '/spaces', query: { spaceType: type.value } })" class="group cursor-pointer rounded-2xl border border-slate-200 p-8 text-center transition-all hover:-translate-y-1 hover:border-primary hover:shadow-lg dark:border-white/5 dark:bg-slate-950">
            <div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-xl bg-primary/10 text-primary transition-colors group-hover:bg-primary group-hover:text-white">
              <el-icon :size="28"><component :is="type.icon" /></el-icon>
            </div>
            <h3 class="text-lg font-bold text-slate-900 dark:text-slate-100">{{ type.label }}</h3>
            <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">{{ type.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Recent announcements -->
    <section class="px-4 py-16">
      <div class="mx-auto max-w-4xl">
        <h2 class="mb-6 text-center text-2xl font-bold text-slate-900 dark:text-slate-100">最新公告</h2>
        <div class="space-y-3">
          <div v-for="n in notices" :key="n.title" class="rounded-xl border border-slate-200 bg-white p-4 transition-colors hover:border-primary/30 dark:border-white/5 dark:bg-slate-900">
            <div class="flex items-center justify-between">
              <h3 class="font-medium text-slate-900 dark:text-slate-100">{{ n.title }}</h3>
              <span class="text-xs text-slate-400">{{ n.time }}</span>
            </div>
            <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ n.content }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const router = useRouter()

const stats = [
  { label: '可预约空间', value: '156' },
  { label: '今日预约', value: '89' },
  { label: '当前在馆', value: '67' },
  { label: '可用率', value: '73%' },
]

const floors = [
  { name: '1F', area: '安静学习区 / 休闲阅读区' },
  { name: '2F', area: '电子阅览区' },
  { name: '3F', area: '研讨区' },
  { name: '4F', area: '多媒体区' },
]

const spaceTypes = [
  { value: 'SEAT', label: '安静座位', desc: '单人安静学习座位，配备电源和良好照明', icon: 'Reading' },
  { value: 'ROOM', label: '研讨室', desc: '可容纳6-12人的研讨室，配备投影和白板', icon: 'UserFilled' },
  { value: 'SEAT', label: '多媒体区', desc: '配备高性能电脑和多媒体设备', icon: 'Monitor' },
]

const notices = [
  { title: '系统升级通知', time: '2026-07-24', content: '图书馆预约系统将于7月26日凌晨2:00-4:00进行维护升级，届时暂停预约服务。' },
  { title: '暑期开放时间调整', time: '2026-07-20', content: '7月25日至8月31日，图书馆开放时间调整为8:00-22:00，请合理安排学习时间。' },
  { title: '新增研讨室预约规则', time: '2026-07-15', content: '研讨室预约最低人数调整为3人，预约时需填写参与人员信息。' },
]
</script>
