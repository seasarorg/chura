package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;

import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}ListPage extends Abstract${configs.table_capitalize}Page {
	
	private Map[] ${configs.table}Items;
	
	public ${configs.table_capitalize}ListPage() {
	}
	
	public String prerender() {
		${configs.table}Items = get${configs.table_capitalize}Dao().findAll();
		return null;
	}
	
	public String doCreate() {
		setCrudType(CrudType.CREATE);
		return "${configs.table}Edit";
	}
	
	public Map[] get${configs.table?cap_first}Items() {
		return this.${configs.table}Items;
	}

	public void set${configs.table?cap_first}Items(Map[] items) {
		this.${configs.table}Items = items;
	}
	
}