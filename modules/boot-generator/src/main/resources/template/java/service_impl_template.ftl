package ${packages.serviceImplPackage};

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResultUtils;
import com.mhd.boot.common.mybatis.core.utils.MybatisPlusUtils;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.utils.MapstructUtils;
import ${packages.entityPackage}.${naming.entityName};
import ${packages.mapperPackage}.${naming.mapperName};
import ${packages.queryDTOPackage}.${naming.queryDTOName};
import ${packages.saveDTOPackage}.${naming.saveDTOName};
import ${packages.voPackage}.${naming.voName};
import ${packages.servicePackage}.${naming.serviceName};
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * ${moduleName} - Service实现层
 *
 * @author ${author}
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ${naming.serviceImplName} implements ${naming.serviceName} {
    private final ${naming.mapperName} baseMapper;

    @Override
    public BaseResponse<PageInfo<${naming.voName}>> selectPageList(${naming.queryDTOName} queryDTO, PageParam pageParam) {
        Page<${naming.entityName}> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<${naming.entityName}> wrapperX = buildQueryWrapper(queryDTO);
        IPage<${naming.voName}> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, ${naming.voName}.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<${naming.voName}> selectList(${naming.queryDTOName} queryDTO) {
        LambdaQueryWrapperX<${naming.entityName}> wrapperX = buildQueryWrapper(queryDTO);
        List<${naming.entityName}> list = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(list, ${naming.voName}.class);
    }

    private LambdaQueryWrapperX<${naming.entityName}> buildQueryWrapper(${naming.queryDTOName} queryDTO) {
        LambdaQueryWrapperX<${naming.entityName}> lqw = new LambdaQueryWrapperX<>();
        return lqw;
    }

    @Override
    public ${naming.voName} selectById(Long ${primaryKey}) {
        ${naming.entityName} ${naming.entityNameLower} = baseMapper.selectById(${primaryKey});
        return MapstructUtils.convert(${naming.entityNameLower}, ${naming.voName}.class);
    }

    @Override
    public int insertByDTO(${naming.saveDTOName} saveDTO) {
        validSaveDTO(saveDTO);
        ${naming.entityName} ${naming.entityNameLower} = MapstructUtils.convert(saveDTO, ${naming.entityName}.class);
        return baseMapper.insert(${naming.entityNameLower});
    }

    @Override
    public int updateByDTO(${naming.saveDTOName} saveDTO) {
        validSaveDTO(saveDTO);
        ${naming.entityName} ${naming.entityNameLower} = MapstructUtils.convert(saveDTO, ${naming.entityName}.class);
        return baseMapper.updateById(${naming.entityNameLower});
    }

    @Override
    public int deleteByIds(Long[] ${primaryKey}s) {
        return baseMapper.deleteByIds(Arrays.asList(${primaryKey}s));
    }

    private void validSaveDTO(${naming.saveDTOName} saveDTO) {
        // TODO 进行校验
    }
}
