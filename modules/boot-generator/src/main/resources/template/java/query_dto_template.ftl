package ${package};

import lombok.Data;
import me.project.common.responsedata.SortableParam;
<#list imports as import>
import ${import};
</#list>

import java.io.Serializable;

/**
 * @author ${author}
 * @since ${date}
 */
@Data
public class ${queryReqDTO} extends SortableParam implements Serializable {
<#list fields as field>
    private ${field.type} ${field.name};
</#list>
}