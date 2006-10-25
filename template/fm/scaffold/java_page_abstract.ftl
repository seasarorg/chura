package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

${getImports()}
import ${configs.rootpackagename}.${configs.daopackagename}.${configs.table_capitalize}Dao;
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.AbstractCrudPage;

public abstract class Abstract${configs.table_capitalize}Page extends AbstractCrudPage {

	private ${configs.table_capitalize}Dao ${configs.table}Dao;
	
	private ${configs.table_capitalize}Dxo ${configs.table}Dxo;
	
<#list mappings as mapping>
	private ${getJavaClassName(mapping)} ${mapping.javaFieldName};

</#list>
	public Abstract${configs.table_capitalize}Page() {
	}

<#list mappings as mapping>
	public ${getJavaClassName(mapping)} get${mapping.javaFieldName?cap_first}() {
		return this.${mapping.javaFieldName};
	}

	public void set${mapping.javaFieldName?cap_first}(${getJavaClassName(mapping)} ${mapping.javaFieldName?lower_case}) {
		this.${mapping.javaFieldName} = ${mapping.javaFieldName?lower_case};
	}
</#list>

	public ${configs.table_capitalize}Dao get${configs.table_capitalize}Dao() {
		return this.${configs.table}Dao;
	}

	public void set${configs.table_capitalize}Dao(${configs.table_capitalize}Dao ${configs.table}Dao) {
		this.${configs.table}Dao = ${configs.table}Dao;
	}

	public ${configs.table_capitalize}Dxo get${configs.table_capitalize}Dxo() {
		return this.${configs.table}Dxo;
	}

	public void set${configs.table_capitalize}Dxo(${configs.table_capitalize}Dxo ${configs.table}Dxo) {
		this.${configs.table}Dxo = ${configs.table}Dxo;
	}
}