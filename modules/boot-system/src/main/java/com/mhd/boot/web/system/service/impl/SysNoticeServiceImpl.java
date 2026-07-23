package com.mhd.boot.web.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResponse;
import com.mhd.boot.common.mybatis.core.domain.PageResultUtils;
import com.mhd.boot.common.mybatis.core.utils.MybatisPlusUtils;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.utils.MapstructUtils;
import com.mhd.boot.web.system.entity.SysNotice;
import com.mhd.boot.web.system.mapper.SysNoticeMapper;
import com.mhd.boot.web.system.model.dto.SysNoticeDTO;
import com.mhd.boot.web.system.model.vo.SysNoticeVo;
import com.mhd.boot.web.system.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhao-hao-dong
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysNoticeServiceImpl implements SysNoticeService {
    private final SysNoticeMapper baseMapper;
    @Override
    public PageResponse<SysNoticeVo> selectPageNoticeList(SysNoticeDTO dto, PageParam pageParam) {
        Page<SysNotice> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<SysNotice> wrapperX = buildQueryWrapper(dto);
        IPage<SysNoticeVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, SysNoticeVo.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<SysNoticeVo> selectNoticeList(SysNoticeDTO dto) {
        LambdaQueryWrapperX<SysNotice> wrapperX = buildQueryWrapper(dto);
        List<SysNotice> list = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(list, SysNoticeVo.class);
    }

    private LambdaQueryWrapperX<SysNotice> buildQueryWrapper(SysNoticeDTO dto) {
        LambdaQueryWrapperX<SysNotice> lqw = new LambdaQueryWrapperX<>();
        lqw.likeIfPresent(SysNotice::getNoticeTitle, dto.getNoticeTitle());
        lqw.eqIfPresent(SysNotice::getNoticeType, dto.getNoticeType());
        lqw.orderByAsc(SysNotice::getNoticeId);
        return lqw;
    }

    @Override
    public SysNoticeVo selectNoticeById(Long noticeId) {
        SysNotice sysNotice = baseMapper.selectById(noticeId);
        return MapstructUtils.convert(sysNotice, SysNoticeVo.class);
    }

    @Override
    public int insertNotice(SysNoticeDTO dto) {
        SysNotice notice = MapstructUtils.convert(dto, SysNotice.class);
        return baseMapper.insert(notice);
    }

    @Override
    public int updateNotice(SysNoticeDTO dto) {
        SysNotice notice = MapstructUtils.convert(dto, SysNotice.class);
        return baseMapper.updateById(notice);
    }

    @Override
    public int deleteNoticeById(Long noticeId) {
        return baseMapper.deleteById(noticeId);
    }

    @Override
    public int deleteNoticeByIds(Long[] noticeIds) {
        return baseMapper.deleteByIds(Arrays.asList(noticeIds));
    }
}
