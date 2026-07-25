<template>
  <div class="absolute w-full h-full">
    <div class="w-full h-full flex flex-col">
      <Header class="bg-white" />
      <div class="flex-1 overflow-y-auto bg-gray-50">
        <router-view />
        <Footer />
      </div>
    </div>
  </div>
</template>

<script>
import Header from "@/layout/components/Header.vue";
import Footer from "@/layout/components/Footer.vue";

export default {
  name: "Layout",
  components: { Footer, Header }
};
</script>
