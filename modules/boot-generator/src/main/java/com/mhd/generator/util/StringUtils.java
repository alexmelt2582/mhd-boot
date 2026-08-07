package com.mhd.generator.util;

import cn.hutool.core.util.ArrayUtil;
import com.mhd.generator.constant.GlobalConstant;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhao-hao-dong
 * @since 2025-03-19
 **/
public class StringUtils {
    private StringUtils() {
    }

    public static String resolveClassPath(String... classPaths) {
        if (ArrayUtil.isEmpty(classPaths)) {
            return "";
        }
        return String.join(GlobalConstant.PACKAGE_CONNECT, classPaths);
    }

    public static String convertPackageToPath(Object packagePath) {
        String newPackagePath = packagePath.toString().substring(0, packagePath.toString().lastIndexOf(GlobalConstant.PACKAGE_CONNECT));
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
     * 从文本内容中查找${...}中的内容，忽略${r'${...}'}和${r"${...}"}
     *
     * @param content 文本内容
     * @return 参数列表
     */
    public static Set<String> findParams(String content) {
        Set<String> result = new HashSet<>();
        // 匹配所有 ${...}
        String regex = "\\$\\{[^\\}]*\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String match = matcher.group();
            // 排除 ${r'${...}'} 和 ${r"${...}"}
            if (!match.matches("\\$\\{r['\"]\\$\\{[^\\}]*\\}['\"]\\}")) {
                String tmpParam = match.substring(2, match.length() - 1);
                result.add(tmpParam);
            }
        }
        return result;
    }

    /**
     * 将 - 或者 _ 分隔的字符串转换为大驼峰字符串
     * 例如：user_name -> UserName
     *
     * @param str 字符串
     * @return 大驼峰字符串
     */
    public static String toUpperCamelCase(String str) {
        return splitAndProcessWords(str, false);
    }

    /**
     * 将字符串的首字母小写
     *
     * @param var 字符串
     * @return 首字母小写的字符串
     */
    public static String toFirstLower(String var) {
        return var.substring(0, 1).toLowerCase() + var.substring(1);
    }

    /**
     * 将 - 或者 _ 分隔的字符串转换为小驼峰字符串
     * 例如：user_name -> userName
     *
     * @param str 字符串
     * @return 小驼峰字符串
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

    public static void main(String[] args) {
        System.out.println(findParams("E:\\idea-workspace\\mine\\zz\\zz-code-generate\\src\\main\\resources\\template\\default_vue2_js_template.ftl"));
        System.out.println(findParams("E:\\idea-workspace\\mine\\zz\\zz-code-generate\\src\\main\\resources\\template\\default_vue2_js_template.ftl"));
    }
}
