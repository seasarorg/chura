package tutorial.chura.web.emp;

import java.math.BigDecimal;
import java.util.Date;

import org.seasar.teeda.extension.annotation.convert.DateTimeConverter;
import org.seasar.teeda.extension.annotation.takeover.TakeOver;

import tutorial.chura.dto.EmpDto;
import tutorial.chura.web.CrudType;

public class EmpListPage extends AbstractEmpPage {

	public EmpDto[] empItems;

	public int empIndex;

	public BigDecimal minSal;

	public BigDecimal maxSal;

	public EmpListPage() {
	}

	public Class initialize() {
		return null;
	}

	public Class prerender() {
		empItems = empDao.selectWithDept(minSal, maxSal);
		return null;
	}

	public String getEmpRowClass() {
		if (empIndex % 2 == 0) {
			return "row_even";
		}
		return "row_odd";
	}

	@TakeOver(properties = "crudType")
	public Class doCreate() {
		crudType = CrudType.CREATE;
		return EmpEditPage.class;
	}

	@DateTimeConverter
	public Date getHiredate() {
		return hiredate;
	}

}