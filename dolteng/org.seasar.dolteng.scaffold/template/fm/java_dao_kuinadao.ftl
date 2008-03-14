package ${configs.rootpackagename}.${configs.daopackagename};

import java.util.List;

import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};

public interface ${configs.table_capitalize}${configs.daosuffix} {

	public List<${configs.table_capitalize}> findAll();
	
	public ${configs.table_capitalize} find(${createPkeyMethodArgs(false)});

	public ${configs.table_capitalize} find(${createPkeyMethodArgs(true)});
	
	public void persist(${configs.table_capitalize} ${configs.table});

	public ${configs.table_capitalize} merge(${configs.table_capitalize} ${configs.table});
	
	public void remove(${configs.table_capitalize} ${configs.table});

	public boolean contains(${configs.table_capitalize} ${configs.table});

	public void refresh(${configs.table_capitalize} ${configs.table});

	public void readLock(${configs.table_capitalize} ${configs.table});

	public void writeLock(${configs.table_capitalize} ${configs.table});
}