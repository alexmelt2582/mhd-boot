package com.mhd.boot.web.system.controller;

import com.mhd.boot.common.idempotent.annotation.RepeatSubmit;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResponse;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;
import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.common.respnsedata.BaseResultUtils;
import com.mhd.boot.common.sse.utils.SseMessageUtils;
import com.mhd.boot.common.web.core.BaseController;
import com.mhd.boot.common.web.service.DictService;
import com.mhd.boot.web.system.model.dto.SysNoticeDTO;
import com.mhd.boot.web.system.model.vo.SysNoticeVo;
import com.mhd.boot.web.system.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhao-hao-dong
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {
    private final SysNoticeService noticeService;
    private final DictService dictService;

    /**
     * 获取通知公告列表
     */
    @GetMapping("/list")
    public PageResponse<SysNoticeVo> list(SysNoticeDTO notice, PageParam pageParam) {
        return noticeService.selectPageNoticeList(notice, pageParam);
    }

    /**
     * 根据通知公告编号获取详细信息
     *
     * @param noticeId 公告ID
     */
    @GetMapping(value = "/{noticeId}")
    public BaseResponse<SysNoticeVo> getInfo(@PathVariable Long noticeId) {
        return BaseResultUtils.successOfData(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @OperateLog(module = "通知公告", type = OperateTypeEnum.CREATE)
    @RepeatSubmit()
    @PostMapping
    public BaseResponse<Void> add(@Validated @RequestBody SysNoticeDTO notice) {
        int rows = noticeService.insertNotice(notice);
        if (rows <= 0) {
            return BaseResultUtils.error();
        }
        String type = dictService.getDictLabel("sys_notice_type", notice.getNoticeType());
        SseMessageUtils.publishAll("[" + type + "] " + notice.getNoticeTitle());
        return BaseResultUtils.success();
    }

    /**
     * 修改通知公告
     */
    @OperateLog(module = "通知公告", type = OperateTypeEnum.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public BaseResponse<Void> edit(@Validated @RequestBody SysNoticeDTO notice) {
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 删除通知公告
     *
     * @param noticeIds 公告ID串
     */
    @OperateLog(module = "通知公告", type = OperateTypeEnum.DELETE)
    @DeleteMapping("/{noticeIds}")
    public BaseResponse<Void> remove(@PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
