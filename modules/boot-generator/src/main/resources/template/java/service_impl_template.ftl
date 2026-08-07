package ${package};

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.project.common.enums.ErrorCodeEnum;
import me.project.common.exception.BusinessException;
import me.project.common.responsedata.PageParam;
import me.project.common.responsedata.PageResponse;
import me.project.common.responsedata.PageResultUtils;
import me.project.common.util.date.DateUtils;
import me.project.common.util.mybatis.LambdaQueryWrapperX;
import me.project.common.util.mybatis.LambdaUpdateWrapperX;
import me.project.common.util.mybatis.MybatisPlusUtils;
import me.project.common.util.validation.ValidationUtils;
import me.project.common.util.validation.group.AddGroup;
import me.project.common.util.validation.group.UpdateGroup;
import me.project.server.constant.GlobalConstant;
import ${packageEntity};
import ${packageService};
import ${packageMapper};
import ${packageQueryReqDTO};
import ${packageSaveReqDTO};
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author ${author}
 * @since ${date}
 */
@Service
@RequiredArgsConstructor
public class ${serviceImpl} extends ServiceImpl<${mapper}, ${entity}>
    implements ${service} {
    private final Validator validator;

    @Override
    public PageResponse<${entity}> meGetPage(PageParam pageParam, ${queryReqDTO} queryReqDTO) {
    	if (pageParam == null && queryReqDTO == null) {
            throw new BusinessException(ErrorCodeEnum.SELECT_ERROR, "查询参数不能为空");
        }
        ValidationUtils.validate(validator, pageParam);
        ValidationUtils.validate(validator, queryReqDTO);
        Page<${entity}> mpPage = MybatisPlusUtils.buildPage(pageParam, queryReqDTO);
        LambdaQueryWrapperX<${entity}> queryWrapperX = new LambdaQueryWrapperX<>();
        //queryWrapperX
        //        .likeIfPresent(${entity}::getName, queryReqDTO.getName());
        Page<${entity}> page = this.page(mpPage, queryWrapperX);
        return PageResultUtils.success(page.getRecords(), page.getTotal());
    }

    @Override
    public ${entity} meGetById(Serializable id) {
    	if (id == null) {
            throw new BusinessException(ErrorCodeEnum.SELECT_ERROR, "ID 不能为空");
        }
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void meAdd(${saveReqDTO} saveReqDTO) {
         if (saveReqDTO == null) {
            throw new BusinessException(ErrorCodeEnum.ADD_ERROR, "新增参数不能为空");
        }
        ValidationUtils.validateByGroup(validator, saveReqDTO, AddGroup.class);
        // TODO 判断其他条件
        // 添加数据
        // TODO 设置数据
        LocalDateTime currentDate = DateUtils.getCurrentDate();
        ${entity} ${entityToLower} = BeanUtil.toBean(saveReqDTO, ${entity}.class)
                .setId(null);
        this.save(${entityToLower});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void meUpdate(${saveReqDTO} saveReqDTO) {
    	if (saveReqDTO == null) {
            throw new BusinessException(ErrorCodeEnum.UPDATE_ERROR, "更新参数不能为空");
        }
        ValidationUtils.validateByGroup(validator, saveReqDTO, UpdateGroup.class);
        // 判断 ID 是否存在
        validatePrimaryExist(saveReqDTO.getId());
        // TODO 判断其他条件
        // 更新数据
        LocalDateTime currentDate = DateUtils.getCurrentDate();
        // TODO 设置数据
        LambdaUpdateWrapperX<${entity}> updateWrapper = new LambdaUpdateWrapperX<>();
        updateWrapper
                .eq(${entity}::getId, saveReqDTO.getId())
                .set(${entity}::getUpdateTime, currentDate);
        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void meDel(Set<Long> ids) {
    	if (CollUtil.isEmpty(ids)) {
            throw new BusinessException(ErrorCodeEnum.UPDATE_ERROR, "ID 不能为空");
        }
        this.removeBatchByIds(ids, GlobalConstant.DEL_BATCH_SIZE);
    }

    @Override
    public List<${entity}> meGetAll() {
        return this.list();
    }

    /**
     * 验证主键 ID 是否存在
     *
     * @param id 主键 ID
     * @return ${entity} 实体
     */
    private ${entity} validatePrimaryExist(Long id) {
    	if (id != null) {
             ${entity} tmp${entity} = this.getById(id);
            if (tmp${entity} != null) {
                return tmp${entity};
            }
        }
        throw new BusinessException(ErrorCodeEnum.UPDATE_ERROR, "ID 不存在");
    }
}
