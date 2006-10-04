package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;

import ${configs.rootpackagename}.${configs.daopackagename}.${configs.table_capitalize}Dao;
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}ConfirmPage extends Abstract${configs.table_capitalize}Page {
	
	public ${configs.table_capitalize}ConfirmPage() {
	}
	
	public String initialize() {
		if(getCrudType() == CrudType.READ || getCrudType() == CrudType.DELETE) {
			Map m = get${configs.table_capitalize}Dao().find(getId());
			get${configs.table_capitalize}Dxo().convert(m ,this);
		}
	}
	
	public String execute() {
		Map m = get${configs.table_capitalize}Dxo().convert(this);
		switch(getCrudType()) {
			case CrudType.CREATE:
				get${configs.table_capitalize}Dao().insert(m);
				break;
			case CrudType.UPDATE:
				get${configs.table_capitalize}Dao().update(m);
				break;
			case CrudType.DELETE:
				get${configs.table_capitalize}Dao().delete(m);
				break;
			default:
				break;
		}
		return "${configs.table}List";
	}
}