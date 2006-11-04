package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table}.${configs.implementationpackagename};

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import ${configs.rootpackagename}.${configs.daopackagename}.${configs.table_capitalize}Dao;
import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table}.${configs.table_capitalize}${configs.servicesuffix};

@Stateless
public class ${configs.table_capitalize}${configs.servicesuffix}${configs.implementationsuffix} implements ${configs.table_capitalize}${configs.servicesuffix} {

	@Resource
	private ${configs.table_capitalize}${configs.daosuffix} ${configs.table}${configs.daosuffix};

	public ${configs.table_capitalize}${configs.servicesuffix}${configs.implementationsuffix}() {
	}

	public List<${configs.table_capitalize}> findAll() {
		return get${configs.table_capitalize}${configs.daosuffix}().findAll();
	}
	
	public ${configs.table_capitalize} find(${createPkeyMethodArgs()}) {
		return get${configs.table_capitalize}${configs.daosuffix}().find(${createPkeyMethodCallArgsCopy()});
	}
	
	public void persist(${configs.table_capitalize} ${configs.table}) {
		get${configs.table_capitalize}${configs.daosuffix}().persist(${configs.table});
	}

	public ${configs.table_capitalize} merge(${configs.table_capitalize} ${configs.table}) {
		return get${configs.table_capitalize}${configs.daosuffix}().merge(${configs.table});
	}
	
	public void remove(${createPkeyMethodArgs()}) {
		${configs.table_capitalize} ${configs.table} = find(${createPkeyMethodCallArgsCopy()});
		get${configs.table_capitalize}${configs.daosuffix}().remove(${configs.table});
	}

	public boolean contains(${configs.table_capitalize} ${configs.table}) {
		return get${configs.table_capitalize}${configs.daosuffix}().contains(${configs.table});
	}

	public ${configs.table_capitalize}${configs.daosuffix} get${configs.table_capitalize}${configs.daosuffix}() {
		return this.${configs.table}${configs.daosuffix};
	}

	public void set${configs.table_capitalize}${configs.daosuffix}(${configs.table_capitalize}${configs.daosuffix} ${configs.table}${configs.daosuffix}) {
		this.${configs.table}${configs.daosuffix} = ${configs.table}${configs.daosuffix};
	}

}