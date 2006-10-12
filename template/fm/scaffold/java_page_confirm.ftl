package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}ConfirmPage extends Abstract${configs.table_capitalize}Page {
	
	public ${configs.table_capitalize}ConfirmPage() {
	}
	
	public String initialize() {
		if(isComeFromList()) {
			get${configs.table_capitalize}Dxo().convert(get${configs.table_capitalize}Dao().find(${createPkeyMethodCallArgs()}) ,this);
		}
		return null;
	}
	
	public String doFinish() {
		switch(getCrudType()) {
			case CrudType.CREATE:
				get${configs.table_capitalize}Dao().insert(get${configs.table_capitalize}Dxo().convert(this));
				break;
			case CrudType.UPDATE:
				get${configs.table_capitalize}Dao().update(get${configs.table_capitalize}Dxo().convert(this));
				break;
			case CrudType.DELETE:
				get${configs.table_capitalize}Dao().delete(get${configs.table_capitalize}Dxo().convert(this));
				break;
			default:
				break;
		}
		return "${configs.table}List";
	}
	
	public boolean isComeFromList() {
		return getCrudType() == CrudType.READ || getCrudType() == CrudType.DELETE;
	}

	public String getIsComeFromListStyle() {
		return null;
	}

	public String getIsNotComeFromListStyle() {
		return null;
	}
}