package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

${getImports()}
import ${configs.rootpackagename}.${configs.daopackagename}.${configs.table_capitalize}Dao;
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.AbstractCrudPage;

public abstract class Abstract${configs.table_capitalize}${configs.pagesuffix} extends AbstractCrud${configs.pagesuffix} {

	public ${configs.table_capitalize}${configs.daosuffix} ${configs.table}${configs.daosuffix};
	
	public ${configs.table_capitalize}${configs.dxosuffix} ${configs.table}${configs.dxosuffix};
	
<#list mappings as mapping>
	public ${getJavaClassName(mapping)} ${mapping.javaFieldName};

</#list>
	public Abstract${configs.table_capitalize}${configs.pagesuffix}() {
	}
}