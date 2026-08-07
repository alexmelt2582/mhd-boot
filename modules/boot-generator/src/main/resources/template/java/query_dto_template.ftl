package ${packages.queryDTOPackage};

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
<#list imports as imp>
import ${imp};
</#list>

/**
 * ${moduleName} - QueryDTO 对象
 *
 * @author ${author}
 */
@Data
public class ${naming.queryDTOName} implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
<#list normalFields  as field>
    /**
     * ${field.comment}
     */
    private ${field.simpleType} ${field.name};
</#list>
}