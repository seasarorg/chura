package ${configs.rootpackagename}.${configs.entitypackagename};

${getImports()}

public class ${configs.table_capitalize} {

<#list mappings as mapping>
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