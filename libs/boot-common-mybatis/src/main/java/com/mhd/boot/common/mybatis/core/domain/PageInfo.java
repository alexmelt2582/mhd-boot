package com.mhd.boot.common.mybatis.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhao-hao-dong
 */
@Data
@AllArgsConstructor
public class PageInfo<T> implements Serializable {
    /**
     * 分页数据
     */
    private List<T> list;
    /**
     * 分页中数据总数
     */
    private Long total;
}
