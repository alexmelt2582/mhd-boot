import { Message, Notification } from "element-ui";

/** 封装任意提示类型通知，默认info */
export function meNotice({
  message,
  title = "温馨提示",
  duration = 2000,
  type = "info",
  parseHtml = false
}) {
  Notification({
    message,
    title,
    type,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}

/** 封装提示通知，默认success */
export function meNoticeSuccess({
  message,
  title = "温馨提示",
  duration = 2000,
  type = "success",
  parseHtml = false
}) {
  Notification({
    message,
    type,
    title,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}

/** 封装提示通知，默认error */
export function meNoticeError({
  message,
  title = "温馨提示",
  duration = 2000,
  type = "error",
  parseHtml = false
}) {
  Notification({
    message,
    type,
    title,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}

/** 封装提示通知，默认warning */
export function meNoticeWarning({
  message,
  title = "温馨提示",
  duration = 2000,
  type = "warning",
  parseHtml = false
}) {
  Notification({
    message,
    title,
    type,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}

/** 封装提示通知，默认info */
export function meNoticeInfo({
  message,
  title = "温馨提示",
  duration = 2000,
  type = "info",
  parseHtml = false
}) {
  Notification({
    message,
    title,
    type,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}

/** 封装提示信息，默认info */
export function meMsg({
  message,
  duration = 2000,
  type = "info",
  parseHtml = false
}) {
  Message({
    message,
    type,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}

/** 封装提示信息，默认success */
export function meMsgSuccess({
  message,
  duration = 2000,
  type = "success",
  parseHtml = false
}) {
  Message({
    message,
    type,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}

/** 封装提示信息，默认error */
export function meMsgError({
  message,
  duration = 2000,
  type = "error",
  parseHtml = false
}) {
  Message({
    message,
    type,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}

/** 封装提示信息，默认warning */
export function meMsgWarning({
  message,
  duration = 2000,
  type = "warning",
  parseHtml = false
}) {
  Message({
    message,
    type,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}

/** 封装提示信息，默认info */
export function meMsgInfo({
  message,
  duration = 2000,
  type = "info",
  parseHtml = false
}) {
  Message({
    message,
    type,
    duration: duration,
    showClose: true,
    dangerouslyUseHTMLString: parseHtml
  });
}
