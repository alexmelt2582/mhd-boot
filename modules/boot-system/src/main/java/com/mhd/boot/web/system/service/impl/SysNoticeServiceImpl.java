package com.mhd.boot.web.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResultUtils;
import com.mhd.boot.common.mybatis.core.utils.MybatisPlusUtils;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.utils.MapstructUtils;
import com.mhd.boot.web.system.entity.SysNotice;
import com.mhd.boot.web.system.mapper.SysNoticeMapper;
import com.mhd.boot.web.system.model.dto.SysNoticeQueryDTO;
import com.mhd.boot.web.system.model.dto.SysNoticeSaveDTO;
import com.mhd.boot.web.system.model.vo.SysNoticeVo;
import com.mhd.boot.web.system.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 公告 服务层实现
 *
 * @author zhao-hao-dong
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysNoticeServiceImpl implements SysNoticeService {
    private final SysNoticeMapper baseMapper;

    @Override
    public BaseResponse<PageInfo<SysNoticeVo>> selectPageNoticeList(SysNoticeQueryDTO queryDTO, PageParam pageParam) {
        Page<SysNotice> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<SysNotice> wrapperX = buildQueryWrapper(queryDTO);
        IPage<SysNoticeVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, SysNoticeVo.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<SysNoticeVo> selectNoticeList(SysNoticeQueryDTO queryDTO) {
        LambdaQueryWrapperX<SysNotice> wrapperX = buildQueryWrapper(queryDTO);
        List<SysNotice> list = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(list, SysNoticeVo.class);
    }

    private LambdaQueryWrapperX<SysNotice> buildQueryWrapper(SysNoticeQueryDTO queryDTO) {
        LambdaQueryWrapperX<SysNotice> lqw = new LambdaQueryWrapperX<>();
        lqw.likeIfPresent(SysNotice::getNoticeTitle, queryDTO.getNoticeTitle());
        lqw.eqIfPresent(SysNotice::getNoticeType, queryDTO.getNoticeType());
        lqw.orderByAsc(SysNotice::getNoticeId);
        return lqw;
    }

    @Override
    public SysNoticeVo selectNoticeById(Long noticeId) {
        SysNotice sysNotice = baseMapper.selectById(noticeId);
        return MapstructUtils.convert(sysNotice, SysNoticeVo.class);
    }

    @Override
    public int insertNotice(SysNoticeSaveDTO saveDTO) {
        validSaveDTO(saveDTO);
        SysNotice notice = MapstructUtils.convert(saveDTO, SysNotice.class);
        return baseMapper.insert(notice);
    }

    @Override
    public int updateNotice(SysNoticeSaveDTO saveDTO) {
        validSaveDTO(saveDTO);
        SysNotice notice = MapstructUtils.convert(saveDTO, SysNotice.class);
        return baseMapper.updateById(notice);
    }

    @Override
    public int deleteNoticeByIds(Long[] noticeIds) {
        return baseMapper.deleteByIds(Arrays.asList(noticeIds));
    }

    private void validSaveDTO(SysNoticeSaveDTO saveDTO) {
        // 进行校验
    }
}
