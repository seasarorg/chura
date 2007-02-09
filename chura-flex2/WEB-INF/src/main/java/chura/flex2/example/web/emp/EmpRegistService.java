package chura.flex2.example.web.emp;

import chura.flex2.example.entity.Emp;

public interface EmpRegistService {

	public Emp[] getEmployees();

	public Emp getEmp(int empNo, String empName);
}
