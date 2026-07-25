<template>
  <div class="me-box">
    <div class="me-top"></div>
    <div class="me-bottom">
      <div class="me-text1">404</div>
      <div class="me-text2">您想看的页面不存在哟🤐</div>
      <div style="height: 20px"></div>
      <el-button type="primary" plain @click="handleHomePage"
        >返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
const router = useRouter()
function handleHomePage() {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.me-box {
  overflow: hidden;

  .me-top {
    background: url("@/assets/images/_core/error/404.png") no-repeat;
    background-size: 100%;
    width: 600px;
    height: 500px;
    margin: 60px auto 0 auto;
  }

  .me-bottom {
    height: 300px;
    margin-top: 20px;
    text-align: center;

    .me-text1 {
      font-size: 46px;
      font-weight: bold;
    }

    .me-text2 {
      padding-top: 30px;
      font-family: "YouYuan", sans-serif;
      font-size: 24px;
      font-weight: 600;
    }
  }
}
</style>
