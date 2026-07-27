import defaultImage from "@/assets/images/default/default_avatar.png";
import { normalizeUrl } from "@/utils/url";
import { isExternal } from "@/utils/validate";

const imgMixin = {
  data() {
    return {
      urlPrefix: process.env.VUE_APP_WEB_RESOURCE_URL,
      defaultImage: defaultImage
    };
  },
  methods: {
    attachImageUrl(url) {
      if (!url) {
        return "";
      }
      if (isExternal(url) || url.startsWith("data:")) {
        return url;
      }
      return normalizeUrl(this.urlPrefix, url);
    },
    async attachImageUrlError(event) {
      let base64Image;
      if (this.defaultImage.startsWith("data:")) {
        base64Image = this.defaultImage;
      } else {
        try {
          base64Image = await this.convertImageToBase64(this.defaultImage);
        } catch (error) {
          console.error("convertImageToBase64 error", error);
          // 如果转换失败，使用原始的默认图片路径
          base64Image = this.defaultImage;
        }
      }
      event.target.src = base64Image;
    },
    // 将 图片转化为 base64
    async convertImageToBase64(url) {
      if (!url) {
        return "";
      }
      try {
        const response = await fetch(url);
        if (!response.ok) {
          console.error("convertImageToBase64 fetch error:", response.status);
        }
        const blob = await response.blob();
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onloadend = () => resolve(reader.result);
          reader.onerror = () => reject(new Error("FileReader error"));
          reader.readAsDataURL(blob);
        });
      } catch (error) {
        console.error("convertImageToBase64 error:", error);
      }
    }
  }
};

export default imgMixin;
