package ${clazz.packageName};

import java.util.Map;

import ${namingConvention.RootPackageNames[0]}.${namingConvention.DaoPackageName}.${table?lower_case?cap_first}Dao;

public class ${clazz.name} extends ${clazz.superClass.name} {
	
	private ${table.name?lower_case?cap_first}Dao ${table.name?lower_case}Dao;
	
	private Map[] empItems;
	
	public ${clazz.name}() {
	}
	
	public String prerender() {
		empItems = get${table.name?lower_case?cap_first}Dao().findAll();
		return null;
	}
	
	public Map[] getEmpItems() {
		return this.empItems;
	}

	public void setEmpItems(Map[] items) {
		this.empItems = items;
	}
	
	public ${table?lower_case?cap_first}Dao get${table?lower_case?cap_first}Dao() {
		return this.${table?lower_case}Dao;
	}

	public void set${table?lower_case?cap_first}Dao(${table?lower_case?cap_first}Dao ${table?lower_case}Dao) {
		return this.${table?lower_case}Dao = ${table?lower_case}Dao;
	}
}