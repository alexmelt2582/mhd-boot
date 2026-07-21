package com.mhd.boot.common.web.core;

import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.common.respnsedata.BaseResultUtils;
import com.mhd.boot.common.utils.StringUtils;

/**
 * web层通用数据处理
 *
 * @author zhao-hao-dong
 **/
public class BaseController {
    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected BaseResponse<Void> toAjax(int rows) {
        return rows > 0 ? BaseResultUtils.success() : BaseResultUtils.error();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected BaseResponse<Void> toAjax(boolean result) {
        return result ? BaseResultUtils.success() : BaseResultUtils.error();
    }

    /**
     * 页面跳转
     */
    public String redirect(String url) {
        return StringUtils.format("redirect:{}", url);
    }
}
