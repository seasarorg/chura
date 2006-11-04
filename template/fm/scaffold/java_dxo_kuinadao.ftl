package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};

public interface ${configs.table_capitalize}${configs.dxosuffix} {

	public ${configs.table_capitalize} convert(Abstract${configs.table_capitalize}Page src);
	
	public void convert(${configs.table_capitalize} src, Abstract${configs.table_capitalize}Page dest);
}