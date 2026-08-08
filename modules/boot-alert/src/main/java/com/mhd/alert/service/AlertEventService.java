package com.mhd.alert.service;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.model.dto.AlertEventQueryDTO;
import com.mhd.alert.model.dto.AlertEventSaveDTO;
import com.mhd.alert.model.vo.AlertEventVo;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.responsedata.BaseResponse;

import java.util.List;

/**
 * 告警事件 - Service层
 *
 * @author zhao-hao-dong
 */
public interface AlertEventService {
    /**
     * 分页查询告警事件列表
     *
     * @param queryDTO  查询条件
     * @param pageParam 分页参数
     * @return 告警事件分页列表
     */
    BaseResponse<PageInfo<AlertEventVo>> selectPageList(AlertEventQueryDTO queryDTO, PageParam pageParam);

    /**
     * 查询告警事件列表
     *
     * @param queryDTO 查询条件
     * @return 告警事件列表
     */
    List<AlertEventVo> selectList(AlertEventQueryDTO queryDTO);

    /**
     * 查询告警中的告警事件列表
     *
     * @param status 告警状态
     * @return 告警事件列表
     */
    List<AlertEvent> selectListByStatus(String status);

    /**
     * 查询告警事件
     *
     * @param id ID
     * @return 告警事件
     */
    AlertEventVo selectById(Long id);

    /**
     * 查询指纹查找告警事件
     *
     * @param fingerprint 指纹
     * @return 告警事件
     */
    AlertEvent selectByFingerprint(String fingerprint);

    /**
     * 新增告警事件
     *
     * @param saveDTO 告警事件
     * @return 结果
     */
    int insertByDTO(AlertEventSaveDTO saveDTO);

    /**
     * 修改告警事件
     *
     * @param saveDTO 告警事件
     * @return 结果
     */
    int updateByDTO(AlertEventSaveDTO saveDTO);

    /**
     * 批量删除告警事件
     *
     * @param ids 需要删除的ID串
     * @return 结果
     */
    int deleteByIds(Long[] ids);
}
