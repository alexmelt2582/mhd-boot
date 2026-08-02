package com.mhd.boot.web.system.controller;

import com.baomidou.lock.annotation.Lock4j;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.responsedata.BaseResultUtils;
import com.mhd.boot.common.web.core.BaseController;
import com.mhd.boot.web.system.model.dto.SysOperLogDTO;
import com.mhd.boot.web.system.model.vo.SysOperLogVo;
import com.mhd.boot.web.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhao-hao-dong
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperLogController extends BaseController {
    private final SysOperLogService operLogService;

    /**
     * 分页获取操作日志记录列表
     */
    @GetMapping("/page")
    public BaseResponse<PageInfo<SysOperLogVo>> page(SysOperLogDTO operLog, PageParam pageParam) {
        return operLogService.selectPageOperLogList(operLog, pageParam);
    }

    /**
     * 查询操作日志详细
     *
     * @param operId 日志id
     */
    @GetMapping(value = "/{operId}")
    public BaseResponse<SysOperLogVo> getInfo(@PathVariable Long operId) {
        return BaseResultUtils.successOfData(operLogService.selectLogById(operId));
    }

    /**
     * 批量删除操作日志记录
     *
     * @param operIds 日志ids
     */
    @OperateLog(module = "操作日志", type = OperateTypeEnum.DELETE)
    @DeleteMapping("/{operIds}")
    public BaseResponse<Void> remove(@PathVariable Long[] operIds) {
        return toAjax(operLogService.deleteOperLogByIds(operIds));
    }

    /**
     * 清理操作日志记录
     */
    @OperateLog(module = "操作日志", type = OperateTypeEnum.DELETE)
    @Lock4j
    @DeleteMapping("/clean")
    public BaseResponse<Void> clean() {
        operLogService.cleanOperLog();
        return BaseResultUtils.success();
    }
}
