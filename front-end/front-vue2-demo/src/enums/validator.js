import { isValidEmail } from "@/utils/validate";

export const usernameValidator = (rule, value, callback) => {
  if (!value) {
    callback();
  } else if (value.length < 4 || value.length > 16) {
    callback(new Error("账号名长度必须在4到16个字符之间"));
  } else if (!/^[a-zA-Z0-9_-]{4,16}$/.test(value)) {
    callback(new Error("账号名只能包含字母、数字、下划线、减号"));
  } else {
    callback();
  }
};

export const passwordValidator = (rule, value, callback) => {
  if (!value) {
    callback();
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error("密码长度必须在6到20个字符之间"));
  } else if (!/^[a-zA-Z0-9_-]{6,16}$/.test(value)) {
    callback(new Error("密码只能包含字母、数字、下划线或减号"));
  } else {
    callback();
  }
};

export const emailValidator = (rule, value, callback) => {
  if (!value) {
    callback();
  } else if (!isValidEmail(value)) {
    callback(new Error("请输入有效的邮箱地址"));
  } else {
    callback();
  }
};

export const phoneValidator = (rule, value, callback) => {
  if (!value) {
    callback();
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error("请输入有效的手机号码"));
  } else {
    callback();
  }
};
