package ${clazz.packageName};

import java.util.Map;

public interface ${table.name?lower_case?cap_first}Dao {

	public Map[] findAll();
}