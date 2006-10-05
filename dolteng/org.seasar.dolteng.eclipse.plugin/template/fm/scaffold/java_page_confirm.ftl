package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;

import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}ConfirmPage extends Abstract${configs.table_capitalize}Page {
	
	public ${configs.table_capitalize}ConfirmPage() {
	}
	
	public String initialize() {
		if(getCrudType() == CrudType.READ || getCrudType() == CrudType.DELETE) {
			Map m = get${configs.table_capitalize}Dao().find(<#list mappings as mapping><#if mapping.isPrimaryKey() = true>get${mapping.javaFieldName?cap_first}()<#if mapping_has_next>,</#if></#if></#list>);
			get${configs.table_capitalize}Dxo().convert(m ,this);
		}
		return null;
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
				get${configs.table_capitalize}Dao().delete(<#list mappings as mapping><#if mapping.isPrimaryKey() = true>get${mapping.javaFieldName?cap_first}()<#if mapping_has_next>,</#if></#if></#list>);
				break;
			default:
				break;
		}
		return "${configs.table}List";
	}
}