package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;
${getImports()}
<#if isTigerResource() = true>
import org.seasar.teeda.extension.annotation.convert.DateTimeConverter;
import org.seasar.teeda.extension.annotation.takeover.TakeOver;

</#if>
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}ListPage extends Abstract${configs.table_capitalize}Page {
	
	private Map[] ${configs.table}Items;
	
	private int ${configs.table}Index;
	
	public ${configs.table_capitalize}ListPage() {
	}
	
	public String initialize() {
		return null;
	}
	
	public String prerender() {
		${configs.table}Items = get${configs.table_capitalize}Dao().findAll();
		return null;
	}
	
	public String get${configs.table_capitalize}RowClass() {
		if (get${configs.table_capitalize}Index() % 2 == 0) {
			return "row_even";
		}
		return "row_odd";
	}

<#if isTigerResource() = true>
	@TakeOver(properties = "crudType")
<#else>
	public static final String doCreate_TAKE_OVER = "properties='crudType'";
</#if>
	public String doCreate() {
		setCrudType(CrudType.CREATE);
		return "${configs.table}Edit";
	}
	
<#list mappings as mapping>
<#if mapping.isDate() = true>
<#if isTigerResource() = true>
	@Override
	@DateTimeConverter
<#else>
	public static final String ${mapping.javaFieldName}_TDateTimeConverter = null;
</#if>
	public ${getJavaClassName(mapping)} get${mapping.javaFieldName?cap_first}() {
		return super.get${mapping.javaFieldName?cap_first}();
	}

</#if>
</#list>
	public Map[] get${configs.table?cap_first}Items() {
		return this.${configs.table}Items;
	}

	public void set${configs.table?cap_first}Items(Map[] items) {
		this.${configs.table}Items = items;
	}
	
	public int get${configs.table_capitalize}Index() {
		return this.${configs.table}Index;
	}
	
	public void set${configs.table_capitalize}Index(int ${configs.table}Index) {
		this.${configs.table}Index = ${configs.table}Index;
	}
}