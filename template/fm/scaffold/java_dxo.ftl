package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;

public interface ${configs.table_capitalize}${configs.dxosuffix} {

	public Map convert(Abstract${configs.table_capitalize}Page src);
	
	public void convert(Map src, Abstract${configs.table_capitalize}Page dest);
}