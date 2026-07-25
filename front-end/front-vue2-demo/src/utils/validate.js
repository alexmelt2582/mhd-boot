/**
 * 判断链接是否是外部链接
 * @param path 链接
 * @returns {boolean} 是否是外部链接
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path);
}

/**
 * 判断是否是手机号码
 * @param phone
 * @returns {boolean}
 */
export function isValidPhone(phone) {
  const reg = /^1([38][0-9]|4[014-9]|[59][0-35-9]|6[2567]|7[0-8])\d{8}$/;
  return reg.test(phone);
}

/**
 * 判断是否是邮箱
 * @param email
 * @returns {boolean}
 */
export function isValidEmail(email) {
  return /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/.test(email);
}
