package ${configs.rootpackagename}.${configs.daopackagename};

import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};

public interface ${configs.table_capitalize}Dao {

	public Class BEAN = ${configs.table_capitalize}.class;

	public ${configs.table_capitalize}[] findAll();
	
<#if 1 &lt; countPkeys()>
	public String find_ARGS = ${createPkeyMethodArgNames()};
</#if>
	public ${configs.table_capitalize} find(${createPkeyMethodArgs()});
	
	public int insert(${configs.table_capitalize} ${configs.table});

	public int update(${configs.table_capitalize} ${configs.table});
	
	public int delete(${configs.table_capitalize} ${configs.table});
}