<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- 页面标题 -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-gray-800">我的收藏</h1>
      <p class="text-gray-600">您已收藏 {{ totalCount }} 篇文章</p>
    </div>
    <!-- 筛选和排序 -->
    <div class="mb-6 flex flex-wrap gap-4">
      <div class="flex items-center space-x-2">
        <span class="text-gray-600">分类：</span>
        <el-select
          v-model="currentTag"
          @change="handleTagChange"
          class="w-32"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option
            v-for="(tag, index) in tagEnum"
            :key="index"
            :label="tag.label"
            :value="tag.value"
          />
        </el-select>
      </div>
    </div>
    <!-- 收藏列表 -->
    <div class="space-y-6" v-loading="loading">
      <div
        v-for="item in favoriteList"
        :key="item.id"
        class="bg-white rounded-lg shadow-sm p-6"
      >
        <div class="flex items-start space-x-4">
          <el-image
            lazy
            :src="attachImageUrl(item.cover)"
            class="w-40 h-30 object-cover rounded-lg"
            fit="cover"
          />
          <div class="flex-1">
            <div class="flex justify-between items-start">
              <h2
                class="
                  text-xl
                  font-medium
                  text-gray-900
                  hover:text-blue-600
                  cursor-pointer
                "
                @click="$router.push(`/health/news/${item.id}`)"
              >
                {{ item.name }}
              </h2>
              <div
                class="text-red-500 cursor-pointer"
                @click="handleRemoveFavorite(item.id)"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-5 w-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fill-rule="evenodd"
                    d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z"
                    clip-rule="evenodd"
                  />
                </svg>
              </div>
            </div>
            <p class="mt-2 text-gray-600 line-clamp-2" v-html="item.content" />
            <div class="mt-4 flex items-center text-sm text-gray-600 space-x-4">
              <span>收藏于：{{ item.createTime }}</span>
              <span
                class="
                  px-2
                  py-1
                  bg-purple-100
                  text-purple-800 text-xs
                  rounded-full
                "
                >{{ convertValueToLabel(tagEnum, item.tagId) }}</span
              >
            </div>
          </div>
        </div>
      </div>
      <!-- 空状态 -->
      <div
        v-if="!loading && favoriteList.length === 0"
        class="text-center py-12"
      >
        <div class="text-gray-600 text-lg">暂无收藏内容</div>
      </div>
    </div>
    <!-- 分页 -->
    <me-pagination
      class="mt-8"
      v-if="total > 0"
      :total="total"
      :page.sync="queryParams.pageNo"
      :limit.sync="queryParams.pageSize"
      textAlign="center"
      layout="prev, pager, next"
      @pagination="handleCurrentChange"
    />
  </div>
</template>

<script>
import imgMixin from "@/mixins/img";
import { mapState } from "vuex";
import { meMsgSuccess } from "@/utils/modal";
import utilMixin from "@/mixins/util";
import {
  getFavoriteCountByType,
  getMyFavoritesNews,
  removeFavorite
} from "@/api/front/base/favorite";

export default {
  name: "Favorite",
  mixins: [imgMixin, utilMixin],
  data() {
    return {
      loading: false,
      totalCount: 0,
      favoriteList: [],
      tagEnum: [], // 标签列表
      currentTag: null, // 当前选中的标签
      total: 0,
      queryParams: {
        pageNo: 1,
        pageSize: 5
      }
    };
  },
  created() {
    this.initData();
  },
  computed: {
    ...mapState("user", ["userInfo"])
  },
  methods: {
    async initData() {
      await Promise.all([this.getLoadEnum(), this.fetchFavoriteData()]);
    },
    async getLoadEnum() {
      try {
        // const { data } = await getAllTags();
        // this.tagEnum = data.map(item => ({
        //   label: item.name,
        //   value: item.id,
        //   ...item
        // }));
      } catch (error) {
        console.error("获取标签列表失败:", error);
      }
      try {
        const { data } = await getFavoriteCountByType();
        this.totalCount = data.news;
      } catch (error) {
        console.error("获取收藏数量失败:", error);
      }
    },
    async fetchFavoriteData() {
      this.loading = true;
      try {
        const params = {
          ...this.queryParams,
          targetType: "news",
          tagId: this.currentTag
        };
        const { list, total } = await getMyFavoritesNews(params);
        this.favoriteList = list || [];
        this.total = total || 0;
      } catch (error) {
        console.error("获取收藏列表失败:", error);
      } finally {
        this.loading = false;
      }
    },
    async handleRemoveFavorite(id) {
      try {
        await this.$confirm("确定要取消收藏吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        });
        const data = {
          targetType: "news",
          targetId: id
        };
        await removeFavorite(data);
        meMsgSuccess({ message: "已取消收藏" });
        await this.fetchFavoriteData();
      } catch (error) {
        console.error("取消收藏失败:", error);
      }
    },
    handleTagChange(tagId) {
      this.currentTag = tagId;
      this.queryParams.pageNo = 1;
      this.fetchFavoriteData();
    },
    handleCurrentChange() {
      this.fetchFavoriteData();
    }
  }
};
</script>

<style lang="scss" scoped></style>
