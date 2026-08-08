package com.mhd.alert.controller;

import com.mhd.alert.model.dto.AlertEventQueryDTO;
import com.mhd.alert.model.dto.AlertEventSaveDTO;
import com.mhd.alert.model.vo.AlertEventVo;
import com.mhd.alert.service.AlertEventService;
import com.mhd.boot.common.idempotent.annotation.RepeatSubmit;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.responsedata.BaseResultUtils;
import com.mhd.boot.common.validate.AddGroup;
import com.mhd.boot.common.validate.EditGroup;
import com.mhd.boot.common.web.core.BaseController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 告警事件 - Controller层
 *
 * @author zhao-hao-dong
 */
@RestController
@RequestMapping("/api/alert/event")
@Validated
@RequiredArgsConstructor
public class AlertEventController extends BaseController {

    private final AlertEventService alertEventService;

    /**
     * 分页查询告警事件列表
     *
     * @param queryDTO  查询条件
     * @param pageParam 分页参数
     * @return 告警事件分页结果
     */
    @GetMapping("/page")
    public BaseResponse<PageInfo<AlertEventVo>> page(@Valid AlertEventQueryDTO queryDTO, @Valid PageParam pageParam) {
        return alertEventService.selectPageList(queryDTO, pageParam);
    }

    /**
     * 根据告警事件编号获取详细信息
     *
     * @param id 主键ID
     * @return 公告详情
     */
    @GetMapping(value = "/{id}")
    public BaseResponse<AlertEventVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return BaseResultUtils.successOfData(alertEventService.selectById(id));
    }

    /**
     * 新增告警事件
     *
     * @param saveDTO 告警事件参数
     * @return 操作结果
     */
    @OperateLog(module = "告警事件", type = OperateTypeEnum.CREATE)
    @RepeatSubmit()
    @PostMapping
    public BaseResponse<Void> add(@Validated(AddGroup.class) @RequestBody AlertEventSaveDTO saveDTO) {
        return toAjax(alertEventService.insertByDTO(saveDTO));
    }

    /**
     * 修改告警事件
     *
     * @param saveDTO 告警事件参数
     * @return 操作结果
     */
    @OperateLog(module = "告警事件", type = OperateTypeEnum.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public BaseResponse<Void> edit(@Validated(EditGroup.class) @RequestBody AlertEventSaveDTO saveDTO) {
        return toAjax(alertEventService.updateByDTO(saveDTO));
    }

    /**
     * 删除告警事件
     *
     * @param ids ID串
     * @return 操作结果
     */
    @OperateLog(module = "告警事件", type = OperateTypeEnum.DELETE)
    @DeleteMapping("/{ids}")
    public BaseResponse<Void> remove(@NotEmpty(message = "主键不能为空")
                                     @PathVariable Long[] ids) {
        return toAjax(alertEventService.deleteByIds(ids));
    }
}
