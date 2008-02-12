package tutorial.chura.web.emp;

import java.util.List;
import java.util.Map;

import org.seasar.teeda.core.exception.AppFacesException;
import org.seasar.teeda.extension.annotation.validator.Required;

import tutorial.chura.entity.Emp;
import tutorial.chura.web.CrudType;

public class EmpEditPage extends AbstractEmpPage {

	public List<Map> deptIdItems;

	public EmpEditPage() {
	}

	public Class initialize() {
		if (super.crudType == CrudType.UPDATE) {
			Emp data = empDao.selectById(id);
			if (data == null) {
				throw new AppFacesException("E0000001");
			}
			empDxo.convert(data, this);
		}
		deptIdItems = deptDao.selectValueLabel();
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