package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.List;

import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};

public interface ${configs.table_capitalize}${configs.servicesuffix} {

	public List<${configs.table_capitalize}> findAll();
	
	public ${configs.table_capitalize} find(${createPkeyMethodArgs()});
	
	public void persist(${configs.table_capitalize} ${configs.table});

	public ${configs.table_capitalize} merge(${configs.table_capitalize} ${configs.table});
	
	public void remove(${createPkeyMethodArgs()});

	public boolean contains(${configs.table_capitalize} ${configs.table});

}