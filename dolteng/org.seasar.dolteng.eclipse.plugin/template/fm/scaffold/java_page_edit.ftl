package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import org.seasar.teeda.extension.annotation.validator.Required;

import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}EditPage extends Abstract${configs.table_capitalize}Page {

	public ${configs.table_capitalize}EditPage() {
	}
	
	public String initialize() {
		if(getCrudType() == CrudType.UPDATE) {
			get${configs.table_capitalize}Dxo().convert(get${configs.table_capitalize}Dao().find(${createPkeyMethodCallArgs()}) ,this);
		}
		return null;
	}

<#list mappings as mapping>
<#if mapping.isNullable() = false>
	@Required
	public ${mapping.javaClassName} get${mapping.javaFieldName?cap_first}() {
		return super.get${mapping.javaFieldName?cap_first}();
	}
</#if>
</#list>
}