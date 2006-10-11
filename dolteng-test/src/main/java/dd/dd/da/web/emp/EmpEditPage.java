package dd.dd.da.web.emp;

import org.seasar.teeda.extension.annotation.validator.Required;

import dd.dd.da.web.CrudType;

public class EmpEditPage extends AbstractEmpPage {

	public EmpEditPage() {
	}
	
	public String initialize() {
		if(getCrudType() == CrudType.UPDATE) {
			getEmpDxo().convert(getEmpDao().find(getEmpno()) ,this);
		}
		return null;
	}

	@Required
	public java.lang.Integer getEmpno() {
		return super.getEmpno();
	}
}