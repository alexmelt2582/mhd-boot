package com.mhd.boot.web.system.controller;

import com.mhd.boot.common.idempotent.annotation.RepeatSubmit;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.responsedata.BaseResultUtils;
import com.mhd.boot.common.sse.utils.SseMessageUtils;
import com.mhd.boot.common.validate.AddGroup;
import com.mhd.boot.common.validate.EditGroup;
import com.mhd.boot.common.web.core.BaseController;
import com.mhd.boot.common.service.DictService;
import com.mhd.boot.web.system.model.dto.SysNoticeQueryDTO;
import com.mhd.boot.web.system.model.dto.SysNoticeSaveDTO;
import com.mhd.boot.web.system.model.vo.SysNoticeVo;
import com.mhd.boot.web.system.service.SysNoticeService;
import jakarta.validation.Valid;
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
     * 分页获取通知公告列表
     */
    @GetMapping("/page")
    public BaseResponse<PageInfo<SysNoticeVo>> page(@Valid SysNoticeQueryDTO queryDTO, @Valid PageParam pageParam) {
        return noticeService.selectPageNoticeList(queryDTO, pageParam);
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
    public BaseResponse<Void> add(@Validated(AddGroup.class) @RequestBody SysNoticeSaveDTO saveDTO) {
        int rows = noticeService.insertNotice(saveDTO);
        if (rows <= 0) {
            return BaseResultUtils.error();
        }
        String type = dictService.getDictLabel("sys_notice_type", saveDTO.getNoticeType());
        SseMessageUtils.publishAll("[" + type + "] " + saveDTO.getNoticeTitle());
        return BaseResultUtils.success();
    }

    /**
     * 修改通知公告
     */
    @OperateLog(module = "通知公告", type = OperateTypeEnum.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public BaseResponse<Void> edit(@Validated(EditGroup.class) @RequestBody SysNoticeSaveDTO saveDTO) {
        return toAjax(noticeService.updateNotice(saveDTO));
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
