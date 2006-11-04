package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

${getImports()}
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.AbstractCrud${configs.pagesuffix};

public abstract class Abstract${configs.table_capitalize}${configs.pagesuffix} extends AbstractCrud${configs.pagesuffix} {

	private ${configs.table_capitalize}${configs.servicesuffix} ${configs.table}${configs.servicesuffix};
	
	private ${configs.table_capitalize}${configs.dxosuffix} ${configs.table}${configs.dxosuffix};
	
<#list mappings as mapping>
	private ${getJavaClassName(mapping)} ${mapping.javaFieldName};

</#list>
	public Abstract${configs.table_capitalize}${configs.pagesuffix}() {
	}

<#list mappings as mapping>
	public ${getJavaClassName(mapping)} get${mapping.javaFieldName?cap_first}() {
		return this.${mapping.javaFieldName};
	}

	public void set${mapping.javaFieldName?cap_first}(${getJavaClassName(mapping)} ${mapping.javaFieldName?lower_case}) {
		this.${mapping.javaFieldName} = ${mapping.javaFieldName?lower_case};
	}
</#list>

	public ${configs.table_capitalize}${configs.servicesuffix} get${configs.table_capitalize}${configs.servicesuffix}() {
		return this.${configs.table}${configs.servicesuffix};
	}

	public void set${configs.table_capitalize}${configs.servicesuffix}(${configs.table_capitalize}${configs.servicesuffix} ${configs.table}${configs.servicesuffix}) {
		this.${configs.table}${configs.servicesuffix} = ${configs.table}${configs.servicesuffix};
	}

	public ${configs.table_capitalize}${configs.dxosuffix} get${configs.table_capitalize}${configs.dxosuffix}() {
		return this.${configs.table}${configs.dxosuffix};
	}

	public void set${configs.table_capitalize}${configs.dxosuffix}(${configs.table_capitalize}${configs.dxosuffix} ${configs.table}${configs.dxosuffix}) {
		this.${configs.table}${configs.dxosuffix} = ${configs.table}${configs.dxosuffix};
	}
}