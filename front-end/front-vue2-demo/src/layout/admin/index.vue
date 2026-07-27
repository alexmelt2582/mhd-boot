<template>
  <div
    class="absolute w-full h-full"
    :class="$store.state.settings.isCollapse ? 'hideSidebar' : ''"
  >
    <admin-aside class="sidebar-container" />
    <div class="main-container flex flex-col">
      <admin-header />
      <div class="app-main flex-1">
        <transition name="fade-transform" mode="out-in">
          <router-view v-slot="{ Component }">
            <keep-alive :max="10">
              <component :is="Component"></component>
            </keep-alive>
          </router-view>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
import AdminHeader from "@/layout/admin/components/AdminHeader.vue";
import AdminFooter from "@/layout/admin/components/AdminFooter.vue";
import AdminAside from "@/layout/admin/components/AdminAside.vue";

export default {
  name: "AdminLayout",
  components: { AdminAside, AdminFooter, AdminHeader }
};
</script>
