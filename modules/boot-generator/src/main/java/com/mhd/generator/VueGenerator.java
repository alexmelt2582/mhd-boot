package com.mhd.generator;

import com.mhd.generator.config.Vue2Config;

/**
 * @author zhao-hao-dong
 **/
public class VueGenerator {
    public static void main(String[] args) {
        // 只需提供 name，nameToUpper/methodName/urlPath 由对象内部派生
        Vue2Config.createDefault()
                .name("test")
                .primaryKey("id")
                .generate();
    }
}
