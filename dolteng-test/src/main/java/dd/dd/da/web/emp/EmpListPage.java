package dd.dd.da.web.emp;

import java.util.Map;

import dd.dd.da.web.CrudType;

public class EmpListPage extends AbstractEmpPage {
	
	private Map[] empItems;
	
	private int empIndex;
	
	public EmpListPage() {
	}
	
	public String prerender() {
		empItems = getEmpDao().findAll();
		return null;
	}
	
	public String getEmpRowStyleClass() {
		if (getEmpIndex() % 2 == 0) {
			return "row_even";
		}
		return "row_odd";
	}

	public String doCreate() {
		setCrudType(CrudType.CREATE);
		return "empEdit";
	}
	
	public Map[] getEmpItems() {
		return this.empItems;
	}

	public void setEmpItems(Map[] items) {
		this.empItems = items;
	}
	
	public int getEmpIndex() {
		return this.empIndex;
	}
	
	public void setEmpIndex(int empIndex) {
		this.empIndex = empIndex;
	}
}