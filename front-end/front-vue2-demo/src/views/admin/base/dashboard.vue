<template>
  <div class="app-main-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="never" class="border-1">
          <div class="flex items-center">
            <div class="mr-4">
              <me-icon-svg name="el-icon-document" class="card-icon" />
            </div>
            <div>
              <div class="text-gray-500">文章</div>
              <me-count-to :value="articleTotalCount" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card
          shadow="never"
          :body-style="{ padding: '20px' }"
          class="border-1"
        >
          <div class="flex items-center">
            <div class="mr-4">
              <me-icon-svg name="el-icon-price-tag" class="card-icon" />
            </div>
            <div>
              <div class="text-gray-500">分类</div>
              <me-count-to :value="categoryTotalCount" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card
          shadow="never"
          :body-style="{ padding: '20px' }"
          class="border-1"
        >
          <div class="flex items-center">
            <div class="mr-4">
              <me-icon-svg name="el-icon-price-tag" class="card-icon" />
            </div>
            <div>
              <div class="text-gray-500">标签</div>
              <me-count-to :value="tagTotalCount" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card
          shadow="never"
          :body-style="{ padding: '20px' }"
          class="border-1"
        >
          <div class="flex items-center">
            <div class="mr-4">
              <me-icon-svg name="el-icon-view" class="card-icon" />
            </div>
            <div>
              <div class="text-gray-500">总浏览量</div>
              <me-count-to :value="pvTotalCount" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-5">
      <el-col :span="12">
        <el-card shadow="never" class="border-1">
          <template #header>
            <div class="flex justify-between">
              <span class="text-lg font-medium">文章分类统计</span>
            </div>
          </template>
          <me-pie-chart
            id="articleCategoryChart"
            :chart-data="articleCategoryList"
          />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="border-1">
          <template #header>
            <div class="flex justify-between">
              <span class="text-lg font-medium">文章标签统计</span>
            </div>
          </template>
          <me-bar-chart id="articleTagChart" :chart-data="articleTagList" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import MeCountTo from "@/components/MeEcharts/MeCountTo.vue";
import { getRandomInt } from "@/utils/random";
import MePieChart from "@/components/MeEcharts/MePieChart.vue";
import MeBarChart from "@/components/MeEcharts/MeBarChart.vue";

export default {
  name: "AdminDashboard",
  components: { MeBarChart, MePieChart, MeCountTo },
  data() {
    return {
      articleTotalCount: getRandomInt(10, 100),
      categoryTotalCount: getRandomInt(10, 100),
      tagTotalCount: getRandomInt(10, 50),
      pvTotalCount: getRandomInt(10, 100),
      articleCategoryList: [],
      articleTagList: []
    };
  },
  mounted() {
    this.fetchArticleCategoryData();
  },
  methods: {
    async fetchArticleCategoryData() {
      this.articleCategoryList = [
        { name: "技术", value: getRandomInt(10, 100) },
        { name: "生活", value: getRandomInt(10, 100) },
        { name: "旅行", value: getRandomInt(10, 100) },
        { name: "美食", value: getRandomInt(10, 100) },
        { name: "教育", value: getRandomInt(10, 100) },
        { name: "娱乐", value: getRandomInt(10, 100) }
      ];
      this.articleTagList = [
        { name: "Vue", value: getRandomInt(10, 100) },
        { name: "React", value: getRandomInt(10, 100) },
        { name: "JavaScript", value: getRandomInt(10, 100) },
        { name: "CSS", value: getRandomInt(10, 100) },
        { name: "HTML", value: getRandomInt(10, 100) },
        { name: "Node.js", value: getRandomInt(10, 100) }
      ];
    }
  }
};
</script>

<style lang="scss" scoped>
.card-icon {
  @apply text-gray-500 flex items-center justify-center text-xl w-10 h-10 rounded-full bg-gray-100;
}
</style>
