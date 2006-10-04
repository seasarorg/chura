package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import ${configs.rootpackagename}.${configs.daopackagename}.${configs.table_capitalize}Dao;
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public abstract class Abstract${configs.table_capitalize}Page {

	private ${configs.table_capitalize}Dao ${configs.table}Dao;
	
	private ${configs.table_capitalize}Dxo ${configs.table}Dxo;
	
	private int crudType = 0;

<#list fields as field>
	private ${field.declaringClassName} ${field.name};

</#list>
	public Abstract${configs.table_capitalize}Page() {
	}

	public int getCrudType() {
		return this.crudType;
	}
	
	public void setCrudType(int type) {
		this.crudType = type;
	}

<#list fields as field>
	public ${field.declaringClassName} get${field.name?cap_first}() {
		return this.${field.name};
	}

	public void set${field.name?cap_first}(${field.declaringClassName} ${field.name?lower_case}) {
		this.${field.name} = ${field.name?lower_case};
	}
</#list>

	public ${configs.table_capitalize}Dao get${configs.table_capitalize}Dao() {
		return this.${configs.table?lower_case}Dao;
	}

	public void set${configs.table_capitalize}Dao(${configs.table_capitalize}Dao ${configs.table}Dao) {
		this.${configs.table}Dao = ${configs.table}Dao;
	}

	public ${configs.table_capitalize}Dxo get${configs.table_capitalize}Dxo() {
		return this.${configs.table?lower_case}Dxo;
	}

	public void set${configs.table_capitalize}Dxo(${configs.table_capitalize}Dxo ${configs.table}Dxo) {
		this.${configs.table}Dxo = ${configs.table}Dxo;
	}
}