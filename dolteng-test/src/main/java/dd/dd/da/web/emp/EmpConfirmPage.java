package dd.dd.da.web.emp;

import org.seasar.teeda.extension.annotation.takeover.TakeOver;
import org.seasar.teeda.extension.annotation.takeover.TakeOverType;

import dd.dd.da.web.CrudType;

public class EmpConfirmPage extends AbstractEmpPage {
	
	public EmpConfirmPage() {
	}
	
	public String initialize() {
		if(isComeFromList()) {
			getEmpDxo().convert(getEmpDao().find(getEmpno()) ,this);
		}
		return null;
	}
	
	@TakeOver(type = TakeOverType.NEVER)
	public String doFinish() {
		switch(getCrudType()) {
			case CrudType.CREATE:
				getEmpDao().insert(getEmpDxo().convert(this));
				break;
			case CrudType.UPDATE:
				getEmpDao().update(getEmpDxo().convert(this));
				break;
			case CrudType.DELETE:
				getEmpDao().delete(getEmpDxo().convert(this));
				break;
			default:
				break;
		}
		return "empList";
	}
	
	public boolean isComeFromList() {
		return getCrudType() == CrudType.READ || getCrudType() == CrudType.DELETE;
	}

	public String getIsComeFromListStyle() {
		return null;
	}

	public String getIsNotComeFromListStyle() {
		return null;
	}
}