package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.List;

import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};

public interface ${configs.table_capitalize}${configs.servicesuffix} {

	public List<${configs.table_capitalize}> findAll();
	
	public ${configs.table_capitalize} find(${createPkeyMethodArgs(true)});
	
	public void persist(Abstract${configs.table_capitalize}${configs.pagesuffix} page);

	public void update(Abstract${configs.table_capitalize}${configs.pagesuffix} page);
	
	public void remove(${createPkeyMethodArgs(true)});

	public boolean contains(${configs.table_capitalize} ${configs.table});

}