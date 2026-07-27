<template>
  <div class="upload-container" style="margin-left: -10%">
    <el-upload
      ref="upload"
      :drag="drag"
      :action="uploadFileUrl"
      :multiple="multiple"
      :show-file-list="false"
      :accept="accept"
      :on-change="fileChange"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :before-upload="handleBeforeUpload"
      :auto-upload="autoUpload"
      :file-list="fileList"
      :limit="limit"
      :on-exceed="handleExceed"
    >
      <div v-if="drag">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      </div>
      <div v-if="!drag">
        <el-button size="small" type="primary">点击上传</el-button>
      </div>

      <div class="el-upload__tip" slot="tip">
        <div v-if="showTip">
          请上传
          <template v-if="fileSize">
            大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b>
          </template>
          <template v-if="fileType.length > 0">
            格式为 <b style="color: #f56c6c">{{ fileType.join("/") }}</b>
          </template>
          的文件
        </div>
        <slot name="downloadFile"></slot>
        <div v-if="customTips !== ''">
          {{ customTips }}
        </div>
      </div>
    </el-upload>
    <!-- 文件列表 -->
    <transition-group
      name="el-fade-in-linear"
      class="upload-file-list"
      tag="div"
    >
      <div :key="file.timestamp" class="item" v-for="(file, index) in fileList">
        <span class="el-icon-document"> {{ file.name }}</span>
        <div style="width: 32px">
          <el-link :underline="false" @click="handleDelete(index)" type="danger"
            >删除
          </el-link>
        </div>
      </div>
    </transition-group>
  </div>
</template>
<script>
/**
 * Props 属性：
 * - uploadFileUrl: 上传地址。当选取文件后立即进行上传时有效。默认 #
 * - drag: 是否支持拖拽上传。默认 true
 * - fileSize: 限制文件大小(MB)。默认 10
 * - fileType: 限制文件类型，例如 ['png', 'jpg', 'jpeg']。默认 []
 * - isShowTip: 是否显示提示。默认 true
 * - customTips: 自定义提示信息。默认 ""
 * - multiple: 是否支持多选。默认 false
 * - fileName: 文件名称。默认 ""
 * - fileHref: 文件链接。默认 ""
 * - field: 字段名称。默认 ""
 * - autoUpload: 是否自动上传。默认 false
 * - limit: 限制上传文件个数。默认 1
 */
export default {
  name: "MeFileUpload",
  props: {
    uploadFileUrl: {
      type: String,
      default: "#"
    },
    drag: {
      type: Boolean,
      default: true
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 10
    },
    // 文件类型, 例如['png', 'jpg', 'jpeg']
    fileType: {
      type: Array,
      default: () => []
    },
    // 是否显示提示
    isShowTip: {
      type: Boolean,
      default: true
    },
    customTips: {
      type: String,
      default: ""
    },
    multiple: {
      type: Boolean,
      default: false
    },
    fileName: {
      type: String,
      default: ""
    },
    fileHref: {
      type: String,
      default: ""
    },
    field: {
      type: String,
      default: ""
    },
    autoUpload: {
      type: Boolean,
      default: false
    },
    limit: {
      type: Number,
      default: 1
    }
  },
  data() {
    return {
      fileList: []
    };
  },
  computed: {
    // 是否显示提示
    showTip() {
      return this.isShowTip && (this.fileType || this.fileSize);
    },
    accept() {
      let temp = this.fileType.map(item => {
        return "." + item;
      });
      return temp.toString();
    }
  },
  methods: {
    // eslint-disable-next-line
    fileChange(file, fileList) {
      // 当选取文件后立即进行上传情况，校验与文件走handleBeforeUpload
      if (!this.autoUpload) {
        if (this.validateFile(file.raw)) {
          // 此处时间戳为解决<transition-group> 的子节点必须有独立的 key
          file.timestamp = Date.parse(new Date());
          this.fileList.push(file);
          this.emitFun();
        }
      }
    },
    validateFile(file) {
      // 校检文件类型
      if (this.fileType.length > 0) {
        let fileExtension = "";
        if (file.name.lastIndexOf(".") > -1) {
          fileExtension = file.name.slice(file.name.lastIndexOf(".") + 1);
        }
        const isTypeOk = this.fileType.some(type => {
          if (file.type.indexOf(type) > -1) return true;
          return !!(fileExtension && fileExtension.indexOf(type) > -1);
        });
        if (!isTypeOk) {
          this.$message.error(
            `文件格式不正确, 请上传${this.fileType.join("/")}格式文件!`
          );
          this.clearFilesList();
          return false;
        }
      }
      // 校检文件大小
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize;
        if (!isLt) {
          this.$message.error(`上传文件大小不能超过 ${this.fileSize} MB!`);
          this.clearFilesList();
          return false;
        }
      }
      return true;
    },
    // 上传文件之前
    handleBeforeUpload(file) {
      return this.validateFile(file);
    },
    // 文件个数超出
    handleExceed() {
      this.$message.error(`上传文件数量不能超过 ${this.limit} 个!`);
    },
    // 删除文件
    handleDelete(index) {
      this.fileList.splice(index, 1);
      this.emitFun();
    },
    // 获取文件
    submitFiles() {
      const formData = this.fileList.filter(item => item.status === "ready");
      return {
        newfile: formData,
        delfile: []
      };
    },
    // 清空文件列表
    clearFilesList() {
      this.fileList = [];
      this.$refs.upload.clearFiles();
    },
    // 上传失败
    handleUploadError() {
      this.$message.error("上传失败, 请重试");
    },
    // 上传成功回调
    // eslint-disable-next-line
    handleUploadSuccess(res, file) {
      this.$message.success("上传成功");
      // 此处需要根据上传成功的数据返回字段 放回fileList中
      // 此处时间戳为解决<transition-group> 的子节点必须有独立的 key
      // this.fileList.push({ name: res.fileName, url: res.fileName,timestamp:Date.parse(new Date()) });
      // this.$emit("uploadSuccess", this.fileList);
    },
    emitFun() {
      const formData = this.fileList.filter(item => item.status === "ready");
      const files = {
        newfile: formData,
        delfile: []
      };
      this.$emit("change", files, this.field);
    }
  }
};
</script>
<style lang="scss" scoped>
.upload-file-list {
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  .item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: inherit;
    border: 1px solid #e4e7ed;
    margin-bottom: 5px;
    padding: 0 10px;
  }
}
</style>
