package chura.flex2.example.web.emp.impl;

import org.seasar.flex2.rpc.remoting.service.annotation.RemotingService;

import chura.flex2.example.dao.EmpDao;
import chura.flex2.example.entity.Emp;
import chura.flex2.example.web.emp.EmpRegistService;

@RemotingService
public class EmpRegistServiceImpl implements EmpRegistService {

	private EmpDao empDao;

	public Emp getEmp(int empNo, String empName) {
		return null;
	}

	public Emp[] getEmployees() {
		return getEmpDao().selectAll();
	}

	public EmpDao getEmpDao() {
		return empDao;
	}

	public void setEmpDao(EmpDao empDao) {
		this.empDao = empDao;
	}

}
