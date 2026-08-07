package ${packages.voPackage};

import ${packages.entityPackage}.${naming.entityName};
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
<#list imports as imp>
import ${imp};
</#list>

/**
 * ${moduleName} - 视图对象
 *
 * @author ${author}
 */
@Data
@AutoMapper(target = ${naming.entityName}.class)
public class ${naming.voName} implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
<#list normalFields  as field>
    /**
     * ${field.comment}
     */
    private ${field.simpleType} ${field.name};
</#list>
}