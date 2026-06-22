package com.mhd.boot.common.utils.object;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Java Bean之间转换工具类
 *
 * @author zhao-hao-dong
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanUtils {
    /**
     * 将源对象转换为目标类型的 JavaBean。
     * <p>如果源对象为 null，则直接返回 null。</p>
     *
     * @param <T>         目标对象的泛型类型
     * @param source      需要被转换的源对象
     * @param targetClass 目标对象的 Class 类型
     * @return 转换后的目标对象，若源对象为 null 则返回 null
     */
    public static <T> T toBean(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return BeanUtil.toBean(source, targetClass);
    }

    /**
     * 将源对象列表批量转换为目标类型的 JavaBean 列表。
     * <p>如果源列表为 null 或为空，则返回一个空的不可变列表；
     * 转换过程中会自动过滤掉转换结果为 null 的元素。</p>
     *
     * @param <S>        源对象的泛型类型
     * @param <T>        目标对象的泛型类型
     * @param source     需要被转换的源对象列表
     * @param targetType 目标对象的 Class 类型
     * @return 转换后的目标对象列表，若源列表为空或 null 则返回空列表
     */
    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        if (CollUtil.isEmpty(source)) {
            return new ArrayList<>();
        }
        return source.stream().map(s -> toBean(s, targetType)).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
