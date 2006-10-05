package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;

import org.seasar.teeda.extension.annotation.validator.Required;

import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}EditPage extends Abstract${configs.table_capitalize}Page {

	public ${configs.table_capitalize}EditPage() {
	}
	
	public String initialize() {
		if(getCrudType() == CrudType.UPDATE) {
			Map m = get${configs.table_capitalize}Dao().find(<#list mappings as mapping><#if mapping.isPrimaryKey() = true>get${mapping.javaFieldName?cap_first}()<#if mapping_has_next>,</#if></#if></#list>);
			get${configs.table_capitalize}Dxo().convert(m ,this);
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