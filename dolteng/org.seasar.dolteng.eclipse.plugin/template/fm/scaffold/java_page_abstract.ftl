package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

${getImports()}
import ${configs.rootpackagename}.${configs.daopackagename}.${configs.table_capitalize}Dao;
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public abstract class Abstract${configs.table_capitalize}Page {

	private ${configs.table_capitalize}Dao ${configs.table}Dao;
	
	private ${configs.table_capitalize}Dxo ${configs.table}Dxo;
	
	private int crudType = 0;

<#list mappings as mapping>
	private ${getJavaClassName(mapping)} ${mapping.javaFieldName};

</#list>
	public Abstract${configs.table_capitalize}Page() {
	}

	public int getCrudType() {
		return this.crudType;
	}
	
	public void setCrudType(int type) {
		this.crudType = type;
	}

	public boolean isCreate() {
		return getCrudType() == CrudType.CREATE;
	}
	
	public boolean isRead() {
		return getCrudType() == CrudType.READ;
	}

	public boolean isUpdate() {
		return getCrudType() == CrudType.UPDATE;
	}

	public boolean isDelete() {
		return getCrudType() == CrudType.DELETE;
	}

<#list mappings as mapping>
	public ${mapping.javaClassName} get${mapping.javaFieldName?cap_first}() {
		return this.${mapping.javaFieldName};
	}

	public void set${mapping.javaFieldName?cap_first}(${mapping.javaClassName} ${mapping.javaFieldName?lower_case}) {
		this.${mapping.javaFieldName} = ${mapping.javaFieldName?lower_case};
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

	public String getIsCreateStyle() {
		return null;
	}

	public String getIsNotCreateStyle() {
		return null;
	}
	
	public String getIsReadStyle() {
		return null;
	}

	public String getIsNotReadStyle() {
		return null;
	}

	public String getIsUpdateStyle() {
		return null;
	}

	public String getIsNotUpdateStyle() {
		return null;
	}

	public String getIsDeleteStyle() {
		return null;
	}

	public String getIsNotDeleteStyle() {
		return null;
	}

}