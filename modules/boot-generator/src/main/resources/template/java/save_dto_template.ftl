package ${packages.saveDTOPackage};

import ${packages.entityPackage}.${naming.entityName};
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
<#list imports as imp>
import ${imp};
</#list>

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