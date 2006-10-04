package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;

public interface ${configs.table_capitalize}Dxo {

	Map convert(Abstract${configs.table_capitalize}Page src);
	
	void convert(Map src, Abstract${configs.table_capitalize}Page dest);
}