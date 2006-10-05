package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;

import ${configs.rootpackagename}.${configs.daopackagename}.${configs.table_capitalize}Dao;
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}EditPage extends Abstract${configs.table_capitalize}Page {

	public ${configs.table_capitalize}EditPage() {
	}
	
	public String initialize() {
		if(getCrudType() == CrudType.UPDATE) {
			Map m = get${configs.table_capitalize}Dao().find(getId());
			get${configs.table_capitalize}Dxo().convert(m ,this);
		}
		return null;
	}
}