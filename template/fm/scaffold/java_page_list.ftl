package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;

import ${configs.rootpackagename}.${configs.daopackagename}.${configs.table_capitalize}Dao;
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}ListPage extends Abstract${configs.table_capitalize}Page {
	
	private Map[] empItems;
	
	public ${configs.table_capitalize}ListPage() {
	}
	
	public String prerender() {
		empItems = get${configs.table_capitalize}Dao().findAll();
		return null;
	}
	
	public String doCreate() {
		setCrudType(CrudType.CREATE);
		return "${configs.table}Edit";
	}
	
	public Map[] getEmpItems() {
		return this.empItems;
	}

	public void setEmpItems(Map[] items) {
		this.empItems = items;
	}
	
}