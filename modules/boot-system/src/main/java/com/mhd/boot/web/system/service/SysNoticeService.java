package com.mhd.boot.web.system.service;

import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.web.system.model.dto.SysNoticeDTO;
import com.mhd.boot.web.system.model.vo.SysNoticeVo;

import java.util.List;

/**
 * 公告服务层
 *
 * @author zhao-hao-dong
 */
public interface SysNoticeService {
    /**
     * 分页查询通知公告列表
     *
     * @param dto       查询条件
     * @param pageParam 分页参数
     * @return 通知公告分页列表
     */
    BaseResponse<PageInfo<SysNoticeVo>> selectPageNoticeList(SysNoticeDTO dto, PageParam pageParam);

    /**
     * 查询公告列表
     *
     * @param dto 公告信息
     * @return 公告集合
     */
    List<SysNoticeVo> selectNoticeList(SysNoticeDTO dto);

    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    SysNoticeVo selectNoticeById(Long noticeId);

    /**
     * 新增公告
     *
     * @param dto 公告信息
     * @return 结果
     */
    int insertNotice(SysNoticeDTO dto);

    /**
     * 修改公告
     *
     * @param dto 公告信息
     * @return 结果
     */
    int updateNotice(SysNoticeDTO dto);

    /**
     * 删除公告信息
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    int deleteNoticeById(Long noticeId);

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    int deleteNoticeByIds(Long[] noticeIds);
}
