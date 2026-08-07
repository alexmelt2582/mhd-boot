package ${packages.saveDTOPackage};

import ${packages.entityPackage}.${naming.entityName};
import lombok.Data;
import me.project.common.util.validation.group.AddGroup;
import me.project.common.util.validation.group.UpdateGroup;
<#list imports as imp>
import ${imp};
</#list>

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;

/**
 * ${moduleName} - SaveDTO 对象
 *
 * @author ${author}
 */
@Data
@AutoMapper(target = ${naming.entityName}.class, reverseConvertGenerate = false)
public class ${naming.saveDTOName} implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
<#list normalFields  as field>
    /**
     * ${field.comment}
     */
    private ${field.simpleType} ${field.name};
</#list>
}