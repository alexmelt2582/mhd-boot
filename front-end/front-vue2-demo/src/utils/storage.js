export const meLocalStorage = {
  /**
   * 设置数据
   * @param key 存储的 key
   * @param value 存储的值
   * @param expireTime 过期时间（单位：毫秒），默认为 null 表示不设置过期时间
   */
  handleSetItem(key, value, expireTime = null) {
    try {
      const data = {
        value: value,
        expireTime: expireTime ? Date.now() + expireTime : null
      };
      const serializedData = JSON.stringify(data);
      localStorage.setItem(key, serializedData);
    } catch (error) {
      console.error("Storage.setItem error:", error);
    }
  },
  /**
   * 获取数据
   * @returns {*|null}
   */
  handleGetItem(key) {
    try {
      const data = localStorage.getItem(key);
      if (!data) return null;
      const parsedData = JSON.parse(data);
      if (parsedData.expireTime && Date.now() > parsedData.expireTime) {
        meLocalStorage.handleRemoveItem(key); // 数据已过期，删除
        return null;
      }
      return parsedData.value;
    } catch (error) {
      console.error("Storage.getItem error:", error);
      return null;
    }
  },

  /**
   * 删除数据
   */
  handleRemoveItem(key) {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error("Storage.removeItem error:", error);
    }
  }
};

export const meSessionStorage = {
  /**
   * 设置数据
   * @param key 存储的 key
   * @param value 存储的值
   * @param expireTime 过期时间（单位：毫秒），默认为 null 表示不设置过期时间
   */
  handleSetItem(key, value, expireTime = null) {
    try {
      const data = {
        value: value,
        expireTime: expireTime ? Date.now() + expireTime : null
      };
      const serializedData = JSON.stringify(data);
      sessionStorage.setItem(key, serializedData);
    } catch (error) {
      console.error("Storage.setItem error:", error);
    }
  },
  /**
   * 获取数据
   * @returns {*|null}
   */
  handleGetItem(key) {
    try {
      const data = sessionStorage.getItem(key);
      if (!data) return null;
      const parsedData = JSON.parse(data);
      if (parsedData.expireTime && Date.now() > parsedData.expireTime) {
        meSessionStorage.handleRemoveItem(key); // 数据已过期，删除
        return null;
      }
      return parsedData.value;
    } catch (error) {
      console.error("Storage.getItem error:", error);
      return null;
    }
  },

  /**
   * 删除数据
   */
  handleRemoveItem(key) {
    try {
      sessionStorage.removeItem(key);
    } catch (error) {
      console.error("Storage.removeItem error:", error);
    }
  }
};
