package ${packages.saveDTOPackage};

import ${packages.entityPackage}.${naming.entityName};
import lombok.Data;
import me.project.common.util.validation.group.AddGroup;
import me.project.common.util.validation.group.UpdateGroup;
<#list imports as import>
import ${import};
</#list>

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;

/**
 * @author ${author}
 */
@Data
@AutoMapper(target = ${naming.entityName}.class, reverseConvertGenerate = false)
public class ${saveReqDTO} implements Serializable {
    @Null(groups = AddGroup.class, message = "ID必须为空")
    @NotNull(groups = UpdateGroup.class, message = "ID不能为空")
<#list fields as field>
    private ${field.type} ${field.name};
</#list>
}