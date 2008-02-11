package tutorial.chura.web.emp;

import java.math.BigDecimal;
import java.util.Date;

import org.seasar.teeda.extension.annotation.validator.Required;

import org.seasar.teeda.core.exception.AppFacesException;

import tutorial.chura.entity.Emp;
import tutorial.chura.web.CrudType;

public class EmpEditPage extends AbstractEmpPage {

	public EmpEditPage() {
	}
	
	public Class initialize() {
		if(super.crudType == CrudType.UPDATE) {
			Emp data = empDao.selectById(id);
			if(data == null) {
				throw new AppFacesException("E0000001");
			}
			empDxo.convert(data ,this);
		}
		return null;
	}
	
	public Class prerender() {
		return null;
	}

	@Required
	public void setId(Integer id) {
		super.id = id;
	}

	@Required
	public void setEmpNo(Integer empno) {
		super.empNo = empno;
	}

	public String getIsNotCreateStyle() {
		return super.crudType == CrudType.CREATE ? "display: none;" : null;
	}
}