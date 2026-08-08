package com.mhd.alert.service;

import com.mhd.alert.entity.AlertRule;
import com.mhd.alert.model.dto.AlertRuleQueryDTO;
import com.mhd.alert.model.dto.AlertRuleSaveDTO;
import com.mhd.alert.model.vo.AlertRuleVo;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.responsedata.BaseResponse;

import java.util.List;

/**
 * 告警规则 - Service层
 *
 * @author zhao-hao-dong
 */
public interface AlertRuleService {
    /**
     * 分页查询告警规则列表
     *
     * @param queryDTO  查询条件
     * @param pageParam 分页参数
     * @return 告警规则分页列表
     */
    BaseResponse<PageInfo<AlertRuleVo>> selectPageList(AlertRuleQueryDTO queryDTO, PageParam pageParam);

    /**
     * 查询告警规则列表
     *
     * @param queryDTO 查询条件
     * @return 告警规则列表
     */
    List<AlertRuleVo> selectList(AlertRuleQueryDTO queryDTO);

    /**
     * 根据类型查询告警规则
     *
     * @param type 告警规则类型
     * @return 告警规则列表
     */
    List<AlertRule> selectListByTypeAndEnableTrue(String type);

    /**
     * 查询告警规则
     *
     * @param id ID
     * @return 告警规则
     */
    AlertRuleVo selectById(Long id);

    /**
     * 新增告警规则
     *
     * @param saveDTO 告警规则
     * @return 结果
     */
    int insertByDTO(AlertRuleSaveDTO saveDTO);

    /**
     * 修改告警规则
     *
     * @param saveDTO 告警规则
     * @return 结果
     */
    int updateByDTO(AlertRuleSaveDTO saveDTO);

    /**
     * 批量删除告警规则
     *
     * @param ids 需要删除的ID串
     * @return 结果
     */
    int deleteByIds(Long[] ids);
}
