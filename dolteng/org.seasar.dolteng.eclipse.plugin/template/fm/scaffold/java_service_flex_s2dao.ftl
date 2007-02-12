package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};

public interface ${configs.table_capitalize}${configs.servicesuffix} {

	public ${configs.table_capitalize}[] selectAll();
	
	public ${configs.table_capitalize} selectById(${createPkeyMethodArgs()});
	
	public void insert(${configs.table_capitalize} ${configs.table});

	public int update(${configs.table_capitalize} ${configs.table});
	
	public void remove(${createPkeyMethodArgs()});

}