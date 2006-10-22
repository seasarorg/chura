package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

<#if isTigerResource() = true>
import org.seasar.teeda.extension.annotation.takeover.TakeOver;

</#if>
import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}ListPage extends Abstract${configs.table_capitalize}Page {
	
	private ${configs.table_capitalize}[] ${configs.table}Items;
	
	private int ${configs.table}Index;
	
	public ${configs.table_capitalize}ListPage() {
	}
	
	public String prerender() {
		${configs.table}Items = get${configs.table_capitalize}Dao().findAll();
		return null;
	}
	
	public String get${configs.table_capitalize}RowStyleClass() {
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
	
	public ${configs.table_capitalize}[] get${configs.table?cap_first}Items() {
		return this.${configs.table}Items;
	}

	public void set${configs.table?cap_first}Items(${configs.table_capitalize}[] items) {
		this.${configs.table}Items = items;
	}
	
	public int get${configs.table_capitalize}Index() {
		return this.${configs.table}Index;
	}
	
	public void set${configs.table_capitalize}Index(int ${configs.table}Index) {
		this.${configs.table}Index = ${configs.table}Index;
	}
}