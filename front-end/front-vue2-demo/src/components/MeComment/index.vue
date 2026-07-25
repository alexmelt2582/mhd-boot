<template>
  <section class="bg-white text-gray-900">
    <!-- 顶部统计 -->
    <header class="px-0 py-2 border-b border-gray-200">
      <h3 class="text-base font-bold">
        评论
        <span class="text-sm font-normal text-gray-500">{{ totalCount }}</span>
      </h3>
    </header>

    <!-- 发表评论 -->
    <div v-if="!hideInput" class="mt-4">
      <div class="flex">
        <img
          :src="attachImageUrl(userInfo?.avatar)"
          class="w-10 h-10 rounded-full mr-3 flex-shrink-0"
        />
        <div class="flex-1">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            maxlength="300"
            show-word-limit
            placeholder="发一条友善的评论吧"
            class="w-full"
          />
          <div class="flex justify-end mt-2">
            <el-button
              type="primary"
              size="small"
              :disabled="!newComment.trim()"
              @click="handleSubmitComment"
            >
              发表评论
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 一级评论 -->
    <div class="mt-4">
      <article
        v-for="comment in commentList"
        :key="comment.id"
        class="flex py-4 border-b border-gray-100 last:border-b-0"
      >
        <img
          :src="attachImageUrl(comment.userAvatar)"
          class="w-10 h-10 rounded-full mr-3 flex-shrink-0"
        />
        <div class="flex-1">
          <div class="flex items-center space-x-2 text-sm">
            <span class="font-semibold">{{ comment.userName || "匿名" }}</span>
            <span
              v-if="comment.userId === userInfo?.id"
              class="text-xs bg-blue-100 text-blue-600 px-1 rounded"
              >我</span
            >
            <span class="text-gray-400">{{ timeAgo(comment.createTime) }}</span>
          </div>

          <p class="mt-1 text-sm">{{ comment.content }}</p>

          <div class="flex items-center space-x-4 mt-2 text-sm text-gray-500">
            <button
              :class="[
                'hover:text-blue-600',
                comment.isLiked ? 'text-blue-600 font-semibold' : ''
              ]"
              @click="handleLikeComment(comment)"
            >
              <i class="el-icon-thumb mr-1" />
              {{ comment.isLiked ? "已赞" : "点赞" }}
              <span v-if="comment.likeCount">({{ comment.likeCount }})</span>
            </button>

            <button
              class="hover:text-blue-600"
              @click="handleToggleReply(comment)"
            >
              <i class="el-icon-chat-dot-round mr-1" />
              回复
            </button>

            <button
              v-if="comment.replyCount > 0"
              class="text-blue-600"
              @click="handleToggleReplies(comment)"
            >
              共{{ comment.replyCount }}条回复
            </button>

            <button
              v-if="comment.userId === userInfo?.id"
              class="hover:text-red-600"
              @click="handleDeleteComment(comment)"
            >
              <i class="el-icon-delete mr-1" />
              删除
            </button>
          </div>

          <!-- 一级回复输入框 -->
          <div v-if="comment.showReplyInput" class="mt-3 ml-13">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="2"
              :placeholder="`回复 @${replyUserName}`"
              class="w-full"
            />
            <div class="flex justify-end space-x-2 mt-2">
              <el-button size="mini" @click="handleCancelReply(comment)"
                >取消
              </el-button>
              <el-button
                size="mini"
                type="primary"
                :disabled="!replyContent.trim()"
                :loading="replyLoading"
                @click="handleSubmitReply(comment)"
              >
                回复
              </el-button>
            </div>
          </div>

          <!-- 二级评论 -->
          <section
            v-if="comment.showReplies"
            class="mt-3 pl-13 border-l border-gray-200"
          >
            <article
              v-for="reply in comment.children"
              :key="reply.id"
              class="flex py-3"
            >
              <img
                :src="attachImageUrl(reply.userAvatar)"
                class="w-7 h-7 rounded-full mr-2 flex-shrink-0"
              />
              <div class="flex-1">
                <div class="flex items-center space-x-2 text-xs">
                  <span class="font-semibold">{{
                    reply.userName || "匿名"
                  }}</span>
                  <span
                    v-if="reply.userId === userInfo?.id"
                    class="bg-blue-100 text-blue-600 px-1 rounded"
                    >我</span
                  >
                  <span
                    v-if="reply.parentId !== comment.id"
                    class="text-blue-600"
                  >
                    回复 @{{ reply.replyUserName }}
                  </span>
                  <span class="text-gray-400">{{
                    timeAgo(reply.createTime)
                  }}</span>
                </div>

                <p class="mt-1 text-sm">{{ reply.content }}</p>

                <div
                  class="flex items-center space-x-3 mt-1 text-xs text-gray-500"
                >
                  <button
                    :class="[
                      'hover:text-blue-600',
                      reply.isLiked ? 'text-blue-600 font-semibold' : ''
                    ]"
                    @click="handleLikeComment(reply)"
                  >
                    <i class="el-icon-thumb mr-1" />
                    {{ reply.isLiked ? "已赞" : "点赞" }}
                    <span v-if="reply.likeCount">({{ reply.likeCount }})</span>
                  </button>

                  <button
                    class="hover:text-blue-600"
                    @click="handleReplyToReply(comment, reply)"
                  >
                    回复
                  </button>

                  <button
                    v-if="reply.userId === userInfo?.id"
                    class="hover:text-red-600"
                    @click="handleDeleteComment(reply)"
                  >
                    删除
                  </button>
                </div>

                <!-- 二级回复输入框 -->
                <div v-if="reply.showReplyInput" class="mt-2">
                  <el-input
                    v-model="replyContent"
                    type="textarea"
                    :rows="2"
                    :placeholder="`回复 @${reply.userName}`"
                    class="w-full"
                  />
                  <div class="flex justify-end space-x-2 mt-2">
                    <el-button size="mini" @click="handleCancelReply(reply)"
                      >取消
                    </el-button>
                    <el-button
                      size="mini"
                      type="primary"
                      :disabled="!replyContent.trim()"
                      :loading="replyLoading"
                      @click="handleSubmitReply(comment, reply)"
                    >
                      回复
                    </el-button>
                  </div>
                </div>
              </div>
            </article>

            <!-- 加载更多回复 -->
            <button
              v-if="comment.hasMoreReplies"
              class="text-blue-600 text-sm mt-2"
              @click="loadMoreReplies(comment)"
            >
              加载更多
            </button>
          </section>
        </div>
      </article>

      <!-- 空状态 -->
      <div
        v-if="!queryLoading && !commentList.length"
        class="text-center text-gray-400 py-8"
      >
        暂无评论，说点什么吧～
      </div>
    </div>

    <!-- 加载更多一级评论 -->
    <div
      v-if="queryObj.pageNo * queryObj.pageSize < commentTotal"
      class="text-center py-4"
    >
      <button
        class="
          text-blue-600
          border border-blue-600
          rounded-full
          px-4
          py-1
          text-sm
          hover:bg-blue-50
        "
        @click="loadMoreComments"
      >
        加载更多
      </button>
    </div>
  </section>
</template>

<script>
import { timeAgo } from "@/utils/date";
import imgMixin from "@/mixins/img";
import { mapState } from "vuex";
import {
  countComment,
  deleteMyComment,
  getCommentsByTarget,
  getReplyComments,
  publishComment,
  replyComment,
  toggleCommentLike
} from "@/api/front/base/comment";

// 常量：默认分页大小（一级与二级回复均使用）
const DEFAULT_PAGE_SIZE = 10;

export default {
  name: "CommentSection",
  mixins: [imgMixin],
  props: {
    targetType: { type: String, required: true },
    targetId: { type: [String, Number], required: true },
    hideInput: { type: Boolean, default: false }
  },
  data() {
    return {
      queryLoading: false,
      queryObj: {
        pageNo: 1,
        pageSize: DEFAULT_PAGE_SIZE,
        targetType: "",
        targetId: undefined
      },
      replyQueries: {},
      newComment: "",
      replyContent: "",
      replyUserName: "",
      replyToParentId: null,
      replyLoading: false,
      commentList: [],
      commentTotal: 0,
      totalCount: 0
    };
  },
  computed: {
    ...mapState({ userInfo: state => state.user.userInfo })
  },
  watch: {
    targetType() {
      this.reload();
    },
    targetId() {
      this.reload();
    }
  },
  created() {
    this.reload();
  },
  methods: {
    timeAgo,
    // 规范化一级评论，补充前端状态字段
    normalizeTopLevelComments(list) {
      return (list || []).map(c => ({
        ...c,
        showReplyInput: false,
        showReplies: false,
        children: [],
        hasMoreReplies: false
      }));
    },
    // 规范化二级评论，补充前端状态字段
    normalizeReplyComments(list) {
      return (list || []).map(r => ({
        ...r,
        replyUserName: r.replyUserName || "用户",
        showReplyInput: false
      }));
    },
    // 获取或初始化某个置顶评论的回复分页对象
    getOrInitReplyPagination(commentId) {
      const existed = this.replyQueries[commentId];
      if (existed) return existed;
      const pagination = { pageNo: 1, pageSize: DEFAULT_PAGE_SIZE };
      this.$set(this.replyQueries, commentId, pagination);
      return pagination;
    },
    // 重置某个置顶评论的回复分页对象
    resetReplyPagination(commentId) {
      this.$set(this.replyQueries, commentId, {
        pageNo: 1,
        pageSize: DEFAULT_PAGE_SIZE
      });
    },
    async reload() {
      await this.loadTotalCount();
      this.loadComments();
    },
    async loadTotalCount() {
      if (!this.targetType || !this.targetId) return;
      const { data: total } = await countComment({
        targetType: this.targetType,
        targetId: this.targetId
      });
      this.totalCount = total || 0;
    },
    loadComments() {
      this.queryObj.pageNo = 1;
      this.commentList = [];
      this.commentTotal = 0;
      this.fetchComments();
    },
    async fetchComments() {
      try {
        this.queryLoading = true;
        this.queryObj.targetId = this.targetId;
        this.queryObj.targetType = this.targetType;
        const { list, total } = await getCommentsByTarget(this.queryObj);
        const normalized = this.normalizeTopLevelComments(list);
        this.commentList = [...this.commentList, ...normalized];
        this.commentTotal = total || 0;
      } finally {
        this.queryLoading = false;
      }
    },
    async loadReplies(comment, concat = false) {
      const q = this.getOrInitReplyPagination(comment.id);
      const { list, total } = await getReplyComments({
        targetType: this.targetType,
        targetId: this.targetId,
        commentId: comment.id,
        pageNo: q.pageNo,
        pageSize: q.pageSize
      });
      const children = this.normalizeReplyComments(list);
      comment.children = concat ? [...comment.children, ...children] : children;
      comment.hasMoreReplies = q.pageNo * q.pageSize < total;
    },
    async loadMoreReplies(comment) {
      const q = this.replyQueries[comment.id];
      q.pageNo += 1;
      await this.loadReplies(comment, true);
    },
    async handleToggleReplies(comment) {
      if (comment.showReplies) {
        comment.showReplies = false;
        return;
      }
      comment.showReplies = true;
      this.resetReplyPagination(comment.id);
      await this.loadReplies(comment);
    },
    handleToggleReply(comment) {
      if (comment.showReplyInput) {
        comment.showReplyInput = false;
        return;
      }
      this.closeAllReplyInputs();
      this.replyUserName = comment.userName || "用户";
      this.replyToParentId = comment.id;
      this.replyContent = "";
      comment.showReplyInput = true;
    },
    handleReplyToReply(comment, reply) {
      if (reply.showReplyInput) {
        reply.showReplyInput = false;
        return;
      }
      this.closeAllReplyInputs();
      this.replyUserName = reply.userName || "用户";
      this.replyToParentId = comment.id;
      this.replyContent = "";
      this.$set(reply, "showReplyInput", true);
    },
    closeAllReplyInputs() {
      this.commentList.forEach(c => {
        c.showReplyInput = false;
        c.children?.forEach(r => (r.showReplyInput = false));
      });
    },
    handleCancelReply(item) {
      this.$set(item, "showReplyInput", false);
      this.replyContent = "";
    },
    async handleSubmitComment() {
      const content = this.newComment.trim();
      if (!content) return;
      await publishComment({
        targetType: this.targetType,
        targetId: this.targetId,
        content
      });
      this.newComment = "";
      await this.reload();
    },
    async handleSubmitReply(parent, reply = null) {
      const content = this.replyContent.trim();
      if (!content) return;
      this.replyLoading = true;
      try {
        await replyComment({
          targetType: this.targetType,
          targetId: this.targetId,
          content: content,
          parentId: reply ? reply.id : parent.id
        });
        this.replyContent = "";
        this.closeAllReplyInputs();
        if (parent.showReplies) {
          this.$set(this.replyQueries, parent.id, { pageNo: 1, pageSize: 10 });
          await this.loadReplies(parent);
        } else {
          parent.replyCount += 1;
        }
      } finally {
        this.replyLoading = false;
      }
    },
    async handleLikeComment(item) {
      try {
        await toggleCommentLike(item.id);
        item.isLiked = !item.isLiked;
        item.likeCount = Math.max(
          0,
          (item.likeCount || 0) + (item.isLiked ? 1 : -1)
        );
      } catch (e) {
        console.error("点赞失败", e);
      }
    },
    async handleDeleteComment(item) {
      try {
        await this.$confirm("确定删除该评论吗？", "提示", { type: "warning" });
        await deleteMyComment(item.id);
        const isReply = item.parentId && item.parentId !== 0;
        if (isReply) {
          const parent = this.commentList.find(c => c.id === item.parentId);
          if (parent) {
            parent.children = parent.children.filter(r => r.id !== item.id);
            parent.replyCount = Math.max(0, parent.replyCount - 1);
          }
        } else {
          this.commentList = this.commentList.filter(c => c.id !== item.id);
          this.totalCount = Math.max(0, this.totalCount - 1);
          this.commentTotal = Math.max(0, this.commentTotal - 1);
        }
      } catch (e) {
        if (e !== "cancel") console.error("删除失败", e);
      }
    },
    async loadMoreComments() {
      if (this.queryLoading) return;
      if (this.queryObj.pageNo * this.queryObj.pageSize >= this.commentTotal)
        return;
      this.queryObj.pageNo += 1;
      await this.fetchComments();
    }
  }
};
</script>

<style lang="scss" scoped>
/* 1. 仅作用于普通 <button>，排除 .el-button */
button:not(.el-button) {
  /* 2. 消除背景色、边框、内外边距 */
  background-color: transparent;
  background-image: none;
  border: 0 solid;
  padding: 0;
  margin: 0;

  /* 3. 让字体继承父级设置 */
  font-family: inherit;
  font-feature-settings: inherit;
  font-variation-settings: inherit;
  font-size: 100%;
  font-weight: inherit;
  line-height: inherit;
  //color: inherit;

  /* 4. 其他细节重置 */
  text-transform: none;
  cursor: pointer;
}

/* 2. 防止普通 button 出现 outline */
button:not(.el-button):focus {
  outline: none;
}
</style>
