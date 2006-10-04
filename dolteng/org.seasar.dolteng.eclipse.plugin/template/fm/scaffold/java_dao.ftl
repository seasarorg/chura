package ${configs.rootpackagename}.${configs.daopackagename};

import java.util.Map;

public interface ${configs.table_capitalize}Dao {

	public Map[] findAll();
	
	public Map find(int id);
	
	public void insert(Map ${configs.table});

	public void update(Map ${configs.table});
	
	public void delete(int id);
}