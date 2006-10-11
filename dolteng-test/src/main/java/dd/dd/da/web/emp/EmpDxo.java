package dd.dd.da.web.emp;

import java.util.Map;

public interface EmpDxo {

	public Map convert(AbstractEmpPage src);
	
	public void convert(Map src, AbstractEmpPage dest);
}