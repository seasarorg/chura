package ${configs.rootpackagename}.${configs.daopackagename};

import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};

public interface ${configs.table_capitalize}${configs.daosuffix} {

	public Class BEAN = ${configs.table_capitalize}.class;

	public ${configs.table_capitalize}[] selectAll();
	
<#if 0 &lt; countPkeys()>
	public String selectById_ARGS = ${createPkeyMethodArgNames()};
</#if>
	public ${configs.table_capitalize} selectById(${createPkeyMethodArgs()});
	
	public int insert(${configs.table_capitalize} ${configs.table});

	public int update(${configs.table_capitalize} ${configs.table});
	
	public int delete(${configs.table_capitalize} ${configs.table});
}