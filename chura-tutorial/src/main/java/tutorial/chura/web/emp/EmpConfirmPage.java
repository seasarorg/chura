package tutorial.chura.web.emp;

import java.math.BigDecimal;
import java.util.Date;

import org.seasar.teeda.extension.annotation.convert.DateTimeConverter;
import org.seasar.teeda.extension.annotation.takeover.TakeOver;
import org.seasar.teeda.extension.annotation.takeover.TakeOverType;
import org.seasar.teeda.extension.annotation.validator.Required;
import org.seasar.teeda.core.exception.AppFacesException;
import org.seasar.teeda.extension.util.LabelHelper;

import tutorial.chura.entity.Emp;
import tutorial.chura.web.CrudType;

public class EmpConfirmPage extends AbstractEmpPage {
	
	public LabelHelper labelHelper;
	
	public EmpConfirmPage() {
	}
	
	public Class initialize() {
		if(isComeFromList()) {
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

	@TakeOver(type = TakeOverType.NEVER)
	public Class doFinish() {
		switch(super.crudType) {
			case CrudType.CREATE:
				empDao.insert(empDxo.convert(this));
				break;
			case CrudType.UPDATE:
				empDao.update(empDxo.convert(this));
				break;
			case CrudType.DELETE:
				empDao.delete(empDxo.convert(this));
				break;
			default:
				break;
		}
		return EmpListPage.class;
	}
	
	public boolean isComeFromList() {
		return super.crudType == CrudType.READ || super.crudType == CrudType.DELETE;
	}

	@Required
	public void setId(Integer id) {
		super.id = id;
	}

	@Required
	public void setEmpNo(Integer empno) {
		super.empNo = empno;
	}

	@DateTimeConverter
	public Date getHiredate() {
		return hiredate;
	}

	public String getJumpEmpEditStyle() {
		return isComeFromList() ? "display: none;" : "";
	}

	public String getDoFinishValue() {
		return labelHelper.getLabelValue(CrudType.toString(super.crudType));
	}
}