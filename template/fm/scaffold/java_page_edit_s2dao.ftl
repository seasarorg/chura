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
	
	public String initialize() {
		if(getCrudType() == CrudType.UPDATE) {
			${configs.table_capitalize} data = get${configs.table_capitalize}${configs.daosuffix}().selectById(${createPkeyMethodCallArgs()});
			if(data == null) {
				throw new AppFacesException("E0000001");
			}
			get${configs.table_capitalize}${configs.dxosuffix}().convert(data ,this);
		}
		return null;
	}
	
	public String prerender() {
		return null;
	}

<#list mappings as mapping>
<#if mapping.isNullable() = false>
<#if isTigerResource() = true>
	@Override
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