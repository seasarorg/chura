package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

${getImports()}
<#if isTigerResource() = true>
import org.seasar.teeda.extension.annotation.convert.DateTimeConverter;
import org.seasar.teeda.extension.annotation.takeover.TakeOver;

</#if>
import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}List${configs.pagesuffix} extends Abstract${configs.table_capitalize}${configs.pagesuffix} {
	
	public ${configs.table_capitalize}[] ${configs.table}Items;
	
	public int ${configs.table}Index;
	
	public ${configs.table_capitalize}ListPage() {
	}
	
	public Class initialize() {
		return null;
	}
	
	public Class prerender() {
		${configs.table}Items = ${configs.table}${configs.daosuffix}.selectAll();
		return null;
	}
	
	public String get${configs.table_capitalize}RowClass() {
		if (${configs.table}Index % 2 == 0) {
			return "row_even";
		}
		return "row_odd";
	}

<#if isTigerResource() = true>
	@TakeOver(properties = "crudType")
<#else>
	public static final String doCreate_TAKE_OVER = "properties='crudType'";
</#if>
	public Class doCreate() {
		crudType = CrudType.CREATE;
		return ${configs.table_capitalize}Edit${configs.pagesuffix}.class;
	}
	
<#list mappings as mapping>
<#if mapping.isDate() = true>
<#if isTigerResource() = true>
	@DateTimeConverter
<#else>
	public static final String ${mapping.javaFieldName}_TDateTimeConverter = null;
</#if>
	public ${getJavaClassName(mapping)} get${mapping.javaFieldName?cap_first}() {
		return ${mapping.javaFieldName};
	}

</#if>
</#list>
}