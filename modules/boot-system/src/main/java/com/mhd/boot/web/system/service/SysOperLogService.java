package com.mhd.boot.web.system.service;

import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.web.system.model.dto.SysOperLogDTO;
import com.mhd.boot.web.system.model.vo.SysOperLogVo;

import java.util.List;

/**
 * 操作日志服务层
 *
 * @author zhao-hao-dong
 */
public interface SysOperLogService {
    /**
     * 分页查询操作日志列表
     *
     * @param dto       查询条件
     * @param pageParam 分页参数
     * @return 操作日志分页列表
     */
    BaseResponse<PageInfo<SysOperLogVo>> selectPageOperLogList(SysOperLogDTO dto, PageParam pageParam);

    /**
     * 查询系统操作日志集合
     *
     * @param dto 操作日志对象
     * @return 操作日志集合
     */
    List<SysOperLogVo> selectOperLogList(SysOperLogDTO dto);

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    SysOperLogVo selectLogById(Long operId);

    /**
     * 新增操作日志
     *
     * @param dto 操作日志对象
     */
    void insertOperLog(SysOperLogDTO dto);

    /**
     * 批量删除系统操作日志
     *
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    int deleteOperLogByIds(Long[] operIds);

    /**
     * 清空操作日志
     */
    void cleanOperLog();
}
