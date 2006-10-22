package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

<#if isTigerResource() = true>
import org.seasar.teeda.extension.annotation.validator.Required;

</#if>
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
<#if isTigerResource() = true>
	@Required
<#else>
	public static final String ${mapping.javaFieldName}_TRequiredValidator = null;
</#if>
	public void set${mapping.javaFieldName?cap_first}(${getJavaClassName(mapping)} ${mapping.javaFieldName?lower_case}) {
		super.set${mapping.javaFieldName?cap_first}(${mapping.javaFieldName?lower_case});
	}
</#if>
</#list>
}