package dd.dd.da.web.emp;

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
	
	public String doExecute() {
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

}