package com.mhd.generator.util;

import cn.hutool.core.util.ArrayUtil;
import com.mhd.generator.constant.GeneratorConstant;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 字符串与包路径处理工具。
 *
 * @author zhao-hao-dong
 * @since 2025-03-19
 **/
public class StringUtils {
    private StringUtils() {
    }

    /**
     * 将多个包路径片段拼接为完整包名。
     */
    public static String resolveClassPath(String... classPaths) {
        if (ArrayUtil.isEmpty(classPaths)) {
            return "";
        }
        return String.join(GeneratorConstant.PACKAGE_CONNECT, classPaths);
    }

    /**
     * 将去掉类名后的父包名转换为目录路径。
     * 例如 com.mhd.zz.mapper.UserMapper -> com/mhd/zz/mapper
     */
    public static String convertPackageToPath(Object packagePath) {
        String newPackagePath = packagePath.toString().substring(0, packagePath.toString().lastIndexOf(GeneratorConstant.PACKAGE_CONNECT));
        String[] strings = newPackagePath.split("\\.");
        Path tmpPath = null;
        for (int i = 0; i < strings.length; i++) {
            if (i == 0) {
                tmpPath = Paths.get(strings[i]);
            } else {
                tmpPath = tmpPath.resolve(strings[i]);
            }
        }
        assert tmpPath != null;
        return tmpPath.toString();
    }

    /**
     * 将 - 或者 _ 分隔的字符串转换为大驼峰字符串
     * 例如：user_name -> UserName
     */
    public static String toUpperCamelCase(String str) {
        return splitAndProcessWords(str, false);
    }

    /**
     * 将字符串的首字母小写
     */
    public static String toFirstLower(String var) {
        return var.substring(0, 1).toLowerCase() + var.substring(1);
    }

    /**
     * 将 - 或者 _ 分隔的字符串转换为小驼峰字符串
     * 例如：user_name -> userName
     */
    public static String toLowerCamelCase(String str) {
        return splitAndProcessWords(str, true);
    }

    private static String splitAndProcessWords(String str, boolean firstWordToLower) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        // 替换下划线或连字符为一个空格
        str = str.replaceAll("[-_]", " ");
        // 将字符串分割为单词数组
        String[] words = str.split(" ");
        StringBuilder camelCaseStr = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                if (i == 0 && firstWordToLower) {
                    // 第一个单词全部小写
                    camelCaseStr.append(word.toLowerCase());
                } else {
                    // 其余单词首字母大写，其余部分小写
                    camelCaseStr.append(Character.toUpperCase(word.charAt(0)));
                    camelCaseStr.append(word.substring(1).toLowerCase());
                }
            }
        }
        return camelCaseStr.toString();
    }
}
