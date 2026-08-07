package ${package};

import com.baomidou.mybatisplus.extension.service.IService;
import me.project.common.responsedata.PageParam;
import me.project.common.responsedata.PageResponse;
import ${packageEntity};
import ${packageQueryReqDTO};
import ${packageSaveReqDTO};

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author ${author}
 * @since ${date}
 */
public interface ${service} extends IService<${entity}> {
    /**
     * 分页查询
     */
    PageResponse<${entity}> meGetPage(PageParam pageParam, ${queryReqDTO} queryReqDTO);

    /**
     * 查询单个数据
     */
    ${entity} meGetById(Serializable id);

    /**
     * 新增
     */
    void meAdd(${saveReqDTO} saveReqDTO);

    /**
     * 修改
     */
    void meUpdate(${saveReqDTO} saveReqDTO);

    /**
     * 删除
     */
    void meDel(Set<Long> ids);

    /**
    * 查询所有数据
    */
    List<${entity}> meGetAll();
}
