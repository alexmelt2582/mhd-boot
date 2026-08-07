package ${packages.controllerPackage};

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
import ${packages.queryDTOPackage}.${naming.queryDTOName};
import ${packages.saveDTOPackage}.${naming.saveDTOName};
import ${packages.voPackage}.${naming.voName};
import ${packages.servicePackage}.${naming.serviceName};
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ${moduleName} - Controller层
 *
 * @author ${author}
 */
@RestController
@RequestMapping("/api/${naming.entityNameLower}")
@Validated
@RequiredArgsConstructor
public class ${naming.controllerName} extends BaseController{

    private final ${naming.serviceName} ${naming.serviceNameLower};

    /**
     * 分页查询${moduleName}列表
     *
     * @param queryDTO  查询条件
     * @param pageParam 分页参数
     * @return ${moduleName}分页结果
     */
    @GetMapping("/page")
    public BaseResponse<PageInfo<${naming.voName}>> page(@Valid ${naming.queryDTOName} queryDTO, @Valid PageParam pageParam) {
        return ${naming.serviceNameLower}.selectPageList(queryDTO, pageParam);
    }

    /**
     * 根据${moduleName}编号获取详细信息
     *
     * @param ${primaryKey} 主键ID
     * @return 公告详情
     */
    @GetMapping(value = "/{${primaryKey}}")
    public BaseResponse<${naming.voName}> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long ${primaryKey}) {
        return BaseResultUtils.successOfData(${naming.serviceNameLower}.selectById(${primaryKey}));
    }

    /**
     * 新增${moduleName}
     *
     * @param saveDTO ${moduleName}参数
     * @return 操作结果
     */
    @OperateLog(module = "${moduleName}", type = OperateTypeEnum.CREATE)
    @RepeatSubmit()
    @PostMapping
    public BaseResponse<Void> add(@Validated(AddGroup.class) @RequestBody ${naming.saveDTOName} saveDTO) {
        return toAjax(${naming.serviceNameLower}.insertByDTO(saveDTO));
    }

    /**
     * 修改${moduleName}
     *
     * @param saveDTO ${moduleName}参数
     * @return 操作结果
     */
    @OperateLog(module = "${moduleName}", type = OperateTypeEnum.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public BaseResponse<Void> edit(@Validated(EditGroup.class) @RequestBody ${naming.saveDTOName} saveDTO) {
        return toAjax(${naming.serviceNameLower}.updateByDTO(saveDTO));
    }

    /**
     * 删除${moduleName}
     *
     * @param noticeIds ID串
     * @return 操作结果
     */
    @OperateLog(module = "${moduleName}", type = OperateTypeEnum.DELETE)
    @DeleteMapping("/{noticeIds}")
    public BaseResponse<Void> remove(@NotEmpty(message = "主键不能为空")
                                         @PathVariable Long[] noticeIds) {
        return toAjax(${naming.serviceNameLower}.deleteByIds(noticeIds));
    }
}
