package com.mhd.boot.common.mybatis.core.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.responsedata.BaseResultUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果工具类
 *
 * @author zhao-hao-dong
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageResultUtils {

    /**
     * 根据列表数据、总记录数构建表格分页数据对象
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public static <T> BaseResponse<PageInfo<T>> build(List<T> list, long total) {
        return BaseResultUtils.successOfData(new PageInfo<T>(list, total));
    }

    /**
     * 根据分页对象构建表格分页数据对象
     *
     * @param page 分页对象
     */
    public static <T> BaseResponse<PageInfo<T>> build(IPage<T> page) {
        return BaseResultUtils.successOfData(new PageInfo<T>(page.getRecords(), page.getTotal()));
    }
}
