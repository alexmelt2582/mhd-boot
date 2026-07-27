/**
 * 浏览器下载 Blob 文件工具函数
 * @param blobData - 后端返回的 Blob 流数据，或者含有 data/headers 的响应对象
 * @param fileName - 下载后的文件名（含后缀），如果为空且无法从响应头解析，则使用默认时间戳命名
 */
export function downBlobFile(blobData: Blob | Response | any, fileName?: string) {
  // 1. 数据预处理：如果传入的是 fetch 或 axios 的原始 Response 对象，提取 Blob
  let blob: Blob;
  let contentDisposition: string | null = null;

  // 判断是不是标准的 Response 对象
  if (blobData instanceof Response) {
    contentDisposition = blobData.headers.get('content-disposition');
    // 因为可能在拦截器里 Promise 没处理直接传了原 Response，这里顺手转一下
    blob = blobData.body ? (blobData as any) : blobData;
    // 注意：Response.body 是一个 ReadableStream，这里建议外层调用时保证传的是 await response.blob() 或是拦截器返回的纯 Blob
    // 更稳妥的写法是要求调用方传入 Blob，但在工具里兜底处理，我们可以单纯判断是不是 Blob 实例。
  }

  if (blobData instanceof Blob) {
    blob = blobData;
  } else if (blobData && blobData.data && blobData.data instanceof Blob) {
    // 兼容意外传入原始 axios 响应对象的情况
    blob = blobData.data;
    contentDisposition = blobData.headers?.['content-disposition'] || null;
  } else {
    console.error('downBlobFile 失败：参数不是 Blob 类型', blobData);
    return;
  }

  // 2. 安全校验：防止后端异常时返回了 JSON 错误流，导致下载乱码文件
  if (blob.type === 'application/json') {
    // 如果拦截器没拦住 JSON 错误，这里把流读出来转换成文字提示
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const json = JSON.parse(e.target?.result as string);
        console.error('下载失败（后端返回错误JSON）:', json.msg || '未知错误');
        // 这里可以用你的 useMessage() 弹窗
        // useMessage().error(json.msg || '文件下载失败');
      } catch {
        console.error('下载失败，文件格式异常');
      }
    };
    reader.readAsText(blob);
    return; // 停止下载流程
  }

  // 3. 确定文件名：优先使用传入的 fileName，其次从响应头 content-disposition 解析
  let finalFileName = fileName;
  if (!finalFileName && contentDisposition) {
    // 匹配类似 attachment; filename="test.xlsx" 或 filename=test.xlsx
    const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    if (match && match[1]) {
      // 去除前后引号并解码
      finalFileName = decodeURIComponent(match[1].replace(/['"]/g, ''));
    }
  }
  // 如果还是没有文件名，生成一个默认文件名
  if (!finalFileName) {
    const date = new Date();
    const timeStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}_${String(date.getHours()).padStart(2, '0')}-${String(date.getMinutes()).padStart(2, '0')}`;
    finalFileName = `downloaded_${timeStr}.xlsx`; // 默认后缀，可以根据业务需求修改
  }

  // 4. 核心逻辑：创建临时 a 标签触发下载
  const link = document.createElement('a');
  link.href = window.URL.createObjectURL(blob); // 生成临时内存 URL
  link.download = finalFileName; // 设置下载的文件名

  // 将 link 加入文档（为了兼容某些旧浏览器，虽然现代浏览器可以不添加）
  document.body.appendChild(link);
  link.click(); // 触发点击下载

  // 5. 释放内存与清理 DOM
  setTimeout(() => {
    document.body.removeChild(link);
    window.URL.revokeObjectURL(link.href); // 释放浏览器的内存占用
  }, 150); // 延迟 150ms 确保浏览器已经在下载这个流，避免过早释放导致下载中断
}
