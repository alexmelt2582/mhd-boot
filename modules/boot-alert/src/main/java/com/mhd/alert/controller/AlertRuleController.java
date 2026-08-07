package com.mhd.alert.controller;

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
import com.mhd.alert.model.dto.AlertRuleQueryDTO;
import com.mhd.alert.model.dto.AlertRuleSaveDTO;
import com.mhd.alert.model.vo.AlertRuleVo;
import com.mhd.alert.service.AlertRuleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 告警规则 - Controller层
 *
 * @author zhao-hao-dong
 */
@RestController
@RequestMapping("/api/alertRule")
@Validated
@RequiredArgsConstructor
public class AlertRuleController extends BaseController{

    private final AlertRuleService alertRuleService;

    /**
     * 分页查询告警规则列表
     *
     * @param queryDTO  查询条件
     * @param pageParam 分页参数
     * @return 告警规则分页结果
     */
    @GetMapping("/page")
    public BaseResponse<PageInfo<AlertRuleVo>> page(@Valid AlertRuleQueryDTO queryDTO, @Valid PageParam pageParam) {
        return alertRuleService.selectPageList(queryDTO, pageParam);
    }

    /**
     * 根据告警规则编号获取详细信息
     *
     * @param id 主键ID
     * @return 公告详情
     */
    @GetMapping(value = "/{id}")
    public BaseResponse<AlertRuleVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return BaseResultUtils.successOfData(alertRuleService.selectById(id));
    }

    /**
     * 新增告警规则
     *
     * @param saveDTO 告警规则参数
     * @return 操作结果
     */
    @OperateLog(module = "告警规则", type = OperateTypeEnum.CREATE)
    @RepeatSubmit()
    @PostMapping
    public BaseResponse<Void> add(@Validated(AddGroup.class) @RequestBody AlertRuleSaveDTO saveDTO) {
        return toAjax(alertRuleService.insertByDTO(saveDTO));
    }

    /**
     * 修改告警规则
     *
     * @param saveDTO 告警规则参数
     * @return 操作结果
     */
    @OperateLog(module = "告警规则", type = OperateTypeEnum.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public BaseResponse<Void> edit(@Validated(EditGroup.class) @RequestBody AlertRuleSaveDTO saveDTO) {
        return toAjax(alertRuleService.updateByDTO(saveDTO));
    }

    /**
     * 删除告警规则
     *
     * @param noticeIds ID串
     * @return 操作结果
     */
    @OperateLog(module = "告警规则", type = OperateTypeEnum.DELETE)
    @DeleteMapping("/{noticeIds}")
    public BaseResponse<Void> remove(@NotEmpty(message = "主键不能为空")
                                         @PathVariable Long[] noticeIds) {
        return toAjax(alertRuleService.deleteByIds(noticeIds));
    }
}
