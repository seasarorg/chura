package dd.dd.da.dao;

import java.util.Map;

public interface EmpDao {

	public Map[] findAll();
	
	public Map find(Integer empno);
	
	public void insert(Map emp);

	public void update(Map emp);
	
	public void delete(Map emp);
}