package com.mhd.boot.web.system.controller;

import com.mhd.boot.common.idempotent.annotation.RepeatSubmit;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.responsedata.BaseResultUtils;
import com.mhd.boot.common.service.DictService;
import com.mhd.boot.common.sse.utils.SseMessageUtils;
import com.mhd.boot.common.validate.AddGroup;
import com.mhd.boot.common.validate.EditGroup;
import com.mhd.boot.common.web.core.BaseController;
import com.mhd.boot.web.system.model.dto.SysNoticeQueryDTO;
import com.mhd.boot.web.system.model.dto.SysNoticeSaveDTO;
import com.mhd.boot.web.system.model.vo.SysNoticeVo;
import com.mhd.boot.web.system.service.SysNoticeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告 信息操作处理
 *
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
     * 分页查询通知公告列表
     *
     * @param queryDTO  查询条件
     * @param pageParam 分页参数
     * @return 公告分页结果
     */
    @GetMapping("/page")
    public BaseResponse<PageInfo<SysNoticeVo>> page(@Valid SysNoticeQueryDTO queryDTO, @Valid PageParam pageParam) {
        return noticeService.selectPageNoticeList(queryDTO, pageParam);
    }

    /**
     * 根据通知公告编号获取详细信息
     *
     * @param noticeId 公告ID
     * @return 公告详情
     */
    @GetMapping(value = "/{noticeId}")
    public BaseResponse<SysNoticeVo> getInfo(@PathVariable Long noticeId) {
        return BaseResultUtils.successOfData(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告，并向在线用户广播公告摘要
     *
     * @param saveDTO 公告参数
     * @return 操作结果
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
     *
     * @param saveDTO 公告参数
     * @return 操作结果
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
     * @return 操作结果
     */
    @OperateLog(module = "通知公告", type = OperateTypeEnum.DELETE)
    @DeleteMapping("/{noticeIds}")
    public BaseResponse<Void> remove(@NotEmpty(message = "主键不能为空")
                                         @PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
