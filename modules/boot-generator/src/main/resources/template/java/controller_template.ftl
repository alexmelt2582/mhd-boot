package ${package};

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import me.project.common.responsedata.*;
import me.project.common.util.validation.group.AddGroup;
import me.project.common.util.validation.group.UpdateGroup;
import ${packageEntity};
import ${packageService};
import ${packageQueryReqDTO};
import ${packageSaveReqDTO};
import ${packageVO};
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * @author ${author}
 * @since ${date}
 */
@RestController
@RequestMapping("/api/${entityToLower}")
@Validated
@RequiredArgsConstructor
public class ${controller} {

    private final ${service} ${serviceToLower};

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public PageResponse<${vo}> page(@Valid PageParam pageParam, @Valid ${queryReqDTO} queryReqDTO) {
        PageResponse<${entity}> ${entityToLower}List = ${serviceToLower}.meGetPage(pageParam, queryReqDTO);
        List<${vo}> ${voToLower}List = BeanUtil.copyToList(${entityToLower}List.getList(), ${vo}.class);
        return PageResultUtils.success(${voToLower}List, ${entityToLower}List.getTotal());
    }

    /**
     * 查询单个数据
     */
    @GetMapping("/get")
    public BaseResponse<${vo}> get(@NotNull(message = "ID不能为空") Long id) {
        ${entity} ${entityToLower} = ${serviceToLower}.meGetById(id);
        ${vo} ${voToLower} = BeanUtil.toBean(${entityToLower}, ${vo}.class);
        return BaseResultUtils.successOfData(${voToLower});
    }

    /**
     * 新增
     */
    @PostMapping("/create")
    public BaseResponse<Object> create(@Validated(AddGroup.class) @RequestBody ${saveReqDTO} saveReqDTO) {
        ${serviceToLower}.meAdd(saveReqDTO);
        return BaseResultUtils.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public BaseResponse<Object> update(@Validated(UpdateGroup.class) @RequestBody ${saveReqDTO} saveReqDTO) {
        ${serviceToLower}.meUpdate(saveReqDTO);
        return BaseResultUtils.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    public BaseResponse<Object> delete(@NotEmpty(message = "ID不能为空") @RequestBody Set<Long> ids) {
        ${serviceToLower}.meDel(ids);
        return BaseResultUtils.success();
    }

    /**
     * 查询所有列表
     */
    @GetMapping("/all")
    public BaseResponse<List<${vo}>> all() {
        List<${entity}> ${entityToLower}List = ${serviceToLower}.meGetAll();
        List<${vo}> ${voToLower}List = BeanUtil.copyToList(${entityToLower}List, ${vo}.class);
        return BaseResultUtils.successOfData(${voToLower}List);
    }
}
