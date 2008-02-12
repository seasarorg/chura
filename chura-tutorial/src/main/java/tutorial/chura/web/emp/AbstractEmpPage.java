package tutorial.chura.web.emp;

import java.math.BigDecimal;
import java.util.Date;

import tutorial.chura.dao.DeptDao;
import tutorial.chura.dao.EmpDao;
import tutorial.chura.web.AbstractCrudPage;

public abstract class AbstractEmpPage extends AbstractCrudPage {

	public EmpDao empDao;

	public DeptDao deptDao;

	public EmpDxo empDxo;

	public Integer id;

	public Integer empNo;

	public String empName;

	public Integer mgrId;

	public Date hiredate;

	public BigDecimal sal;

	public Integer deptId;

	public Integer versionNo;

	public AbstractEmpPage() {
	}
}