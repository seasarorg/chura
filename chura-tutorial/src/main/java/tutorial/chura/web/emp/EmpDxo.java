package tutorial.chura.web.emp;

import tutorial.chura.entity.Emp;

public interface EmpDxo {

	public Emp convert(AbstractEmpPage src);
	
	public void convert(Emp src, AbstractEmpPage dest);
}