package com.mhd.boot.web.modules.llmprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mhd.boot.web.modules.llmprovider.entity.LlmGlobalSettingEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * LLM 全局设置 Mapper 接口
 *
 * @author mhd
 */
@Mapper
public interface LlmGlobalSettingMapper extends BaseMapper<LlmGlobalSettingEntity> {
}
