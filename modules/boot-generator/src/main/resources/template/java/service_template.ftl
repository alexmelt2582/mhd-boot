package ${packages.servicePackage};

import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.responsedata.BaseResponse;
import ${packages.queryDTOPackage}.${naming.queryDTOName};
import ${packages.saveDTOPackage}.${naming.saveDTOName};
import ${packages.voPackage}.${naming.voName};

import java.util.List;

/**
 * ${moduleName} - Service层
 *
 * @author ${author}
 */
public interface ${naming.serviceName} {
    /**
     * 分页查询${moduleName}列表
     *
     * @param queryDTO  查询条件
     * @param pageParam 分页参数
     * @return ${moduleName}分页列表
     */
    BaseResponse<PageInfo<${naming.voName}>> selectPageList(${naming.queryDTOName} queryDTO, PageParam pageParam);

    /**
     * 查询${moduleName}列表
     *
     * @param queryDTO 查询条件
     * @return ${moduleName}列表
     */
    List<${naming.voName}> selectList(${naming.queryDTOName} queryDTO);

    /**
     * 查询${moduleName}
     *
     * @param ${primaryKey} ID
     * @return ${moduleName}
     */
    ${naming.voName} selectById(Long ${primaryKey});

    /**
     * 新增${moduleName}
     *
     * @param saveDTO ${moduleName}
     * @return 结果
     */
    int insertByDTO(${naming.saveDTOName} saveDTO);

    /**
     * 修改${moduleName}
     *
     * @param saveDTO ${moduleName}
     * @return 结果
     */
    int updateByDTO(${naming.saveDTOName} saveDTO);

    /**
     * 批量删除${moduleName}
     *
     * @param ${primaryKey}s 需要删除的ID串
     * @return 结果
     */
    int deleteByIds(Long[] ${primaryKey}s);
}
