package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

${getImports()}
<#if isTigerResource() = true>
import org.seasar.teeda.extension.annotation.validator.Required;

</#if>
import org.seasar.teeda.core.exception.AppFacesException;

import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}Edit${configs.pagesuffix} extends Abstract${configs.table_capitalize}${configs.pagesuffix} {

	public ${configs.table_capitalize}Edit${configs.pagesuffix}() {
	}
	
	public Class initialize() {
		if(super.crudType == CrudType.UPDATE) {
			${configs.table_capitalize} data = ${configs.table}${configs.daosuffix}.selectById(${createPkeyMethodCallArgsCopy()});
			if(data == null) {
				throw new AppFacesException("E0000001");
			}
			${configs.table}${configs.dxosuffix}.convert(data ,this);
		}
		return null;
	}
	
	public Class prerender() {
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
		super.${mapping.javaFieldName} = ${mapping.javaFieldName?lower_case};
	}

</#if>
</#list>
	public String getIsNotCreateStyle() {
		return super.crudType == CrudType.CREATE ? "display: none;" : null;
	}
}