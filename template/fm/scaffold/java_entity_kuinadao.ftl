package ${configs.rootpackagename}.${configs.entitypackagename};

import javax.persistence.Entity;
import javax.persistence.Id;

${getImports()}

@Entity
public class ${configs.table_capitalize} {

<#list mappings as mapping>
<#if mapping.isPrimaryKey() = true>
	@Id
</#if>
	private ${getJavaClassName(mapping)} ${mapping.javaFieldName};

</#list>
	public ${configs.table_capitalize}() {
	}

<#list mappings as mapping>
	public ${getJavaClassName(mapping)} get${mapping.javaFieldName?cap_first}() {
		return this.${mapping.javaFieldName};
	}

	public void set${mapping.javaFieldName?cap_first}(${getJavaClassName(mapping)} ${mapping.javaFieldName?lower_case}) {
		this.${mapping.javaFieldName} = ${mapping.javaFieldName?lower_case};
	}
</#list>
}