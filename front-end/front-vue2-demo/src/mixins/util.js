const utilMixin = {
  methods: {
    /**
     * 根据传入的列表和值转换为对应的 key
     * @param {Array} list - 选项列表
     * @param {string|number} value - 需要转换的值
     * @returns {string} - 转换后的标签文本
     */
    convertValueToLabel(list, value) {
      const defaultValue = "未知";
      // 如果列表为空或不是数组，直接返回默认值
      if (!Array.isArray(list) || list.length === 0) {
        return defaultValue;
      }
      // 如果值未定义或为null，返回默认值
      if (value === undefined || value === null) {
        return defaultValue;
      }
      const item = list.find(item => item.value === value);
      return item ? item.label : defaultValue;
    },
    /**
     * 根据传入的列表和值转换为对应的 elType
     * @param list
     * @param value
     * @returns {string|*|string}
     */
    getElType(list, value) {
      const defaultType = "info";
      // 如果列表为空或不是数组，直接返回默认值
      if (!Array.isArray(list) || list.length === 0) {
        return defaultType;
      }
      // 如果值未定义或为null，返回默认值
      if (value === undefined || value === null) {
        return defaultType;
      }
      const item = list.find(item => item.value === value);
      return item ? item.elType : defaultType;
    }
  }
};

export default utilMixin;
