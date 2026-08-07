package com.mhd.generator.handler;

import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.type.ITypeConvertHandler;
import com.baomidou.mybatisplus.generator.type.TypeRegistry;
import org.apache.ibatis.type.JdbcType;

/**
 * @author zhao-hao-dong
 * @since 2025-06-13
 **/
public class CustomMysqlTypeConvert implements ITypeConvertHandler {
    @Override
    public IColumnType convert(GlobalConfig globalConfig, TypeRegistry typeRegistry, TableField.MetaInfo metaInfo) {
        // 如果是 tinyint 类型，默认转化为 Integer
        if (metaInfo.getJdbcType().TYPE_CODE == JdbcType.TINYINT.TYPE_CODE) {
            return DbColumnType.INTEGER;
        }
        return typeRegistry.getColumnType(metaInfo);
    }
}
