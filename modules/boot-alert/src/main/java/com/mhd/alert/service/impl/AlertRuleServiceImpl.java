package com.mhd.alert.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.alert.entity.AlertRule;
import com.mhd.alert.enums.EnableEnum;
import com.mhd.alert.mapper.AlertRuleMapper;
import com.mhd.alert.model.dto.AlertRuleQueryDTO;
import com.mhd.alert.model.dto.AlertRuleSaveDTO;
import com.mhd.alert.model.vo.AlertRuleVo;
import com.mhd.alert.service.AlertRuleService;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResultUtils;
import com.mhd.boot.common.mybatis.core.utils.MybatisPlusUtils;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 告警规则 - Service实现层
 *
 * @author zhao-hao-dong
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertRuleServiceImpl implements AlertRuleService {
    private final AlertRuleMapper baseMapper;

    @Override
    public BaseResponse<PageInfo<AlertRuleVo>> selectPageList(AlertRuleQueryDTO queryDTO, PageParam pageParam) {
        Page<AlertRule> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<AlertRule> wrapperX = buildQueryWrapper(queryDTO);
        IPage<AlertRuleVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, AlertRuleVo.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<AlertRuleVo> selectList(AlertRuleQueryDTO queryDTO) {
        LambdaQueryWrapperX<AlertRule> wrapperX = buildQueryWrapper(queryDTO);
        List<AlertRule> list = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(list, AlertRuleVo.class);
    }

    @Override
    public List<AlertRule> selectListByTypeAndEnableTrue(String type) {
        LambdaQueryWrapperX<AlertRule> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(AlertRule::getType, type);
        queryWrapper.eq(AlertRule::getEnable, EnableEnum.ENABLE.getCode());
        return baseMapper.selectList(queryWrapper);
    }

    private LambdaQueryWrapperX<AlertRule> buildQueryWrapper(AlertRuleQueryDTO queryDTO) {
        LambdaQueryWrapperX<AlertRule> lqw = new LambdaQueryWrapperX<>();
        return lqw;
    }

    @Override
    public AlertRuleVo selectById(Long id) {
        AlertRule alertRule = baseMapper.selectById(id);
        return MapstructUtils.convert(alertRule, AlertRuleVo.class);
    }

    @Override
    public int insertByDTO(AlertRuleSaveDTO saveDTO) {
        validSaveDTO(saveDTO);
        AlertRule alertRule = MapstructUtils.convert(saveDTO, AlertRule.class);
        return baseMapper.insert(alertRule);
    }

    @Override
    public int updateByDTO(AlertRuleSaveDTO saveDTO) {
        validSaveDTO(saveDTO);
        AlertRule alertRule = MapstructUtils.convert(saveDTO, AlertRule.class);
        return baseMapper.updateById(alertRule);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    private void validSaveDTO(AlertRuleSaveDTO saveDTO) {
        // TODO 进行校验
    }
}
