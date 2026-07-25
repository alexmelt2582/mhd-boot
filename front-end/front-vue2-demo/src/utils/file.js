/**
 * 下载文件
 * @param headers 请求头
 * @param data 文件数据
 */
export function downloadFile({ headers, data }) {
  // 创建一个新的 Blob 对象，使用响应数据
  const url = window.URL.createObjectURL(new Blob([data]));
  // 创建一个链接元素
  const link = document.createElement("a");
  link.href = url;
  // 设置下载文件名，从响应头中获取
  const contentDisposition = headers["content-disposition"];
  const filename = contentDisposition.split("filename=")[1].split(";")[0];
  link.setAttribute("download", filename);
  // 模拟点击链接进行下载
  document.body.appendChild(link);
  link.click();
  // 清除链接元素
  document.body.removeChild(link);
}
