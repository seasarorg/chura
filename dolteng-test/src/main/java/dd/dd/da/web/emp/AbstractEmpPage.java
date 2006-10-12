package dd.dd.da.web.emp;

import java.math.BigDecimal;
import java.util.Date;

import dd.dd.da.dao.EmpDao;
import dd.dd.da.web.CrudType;

public abstract class AbstractEmpPage {

	private EmpDao empDao;
	
	private EmpDxo empDxo;
	
	private int crudType = 0;

	private Integer empno;

	private String ename;

	private Integer mgrid;

	private Date hiredate;

	private BigDecimal sal;

	private Integer deptid;

	public AbstractEmpPage() {
	}

	public int getCrudType() {
		return this.crudType;
	}
	
	public void setCrudType(int type) {
		this.crudType = type;
	}

	public boolean isCreate() {
		return getCrudType() == CrudType.CREATE;
	}
	
	public boolean isRead() {
		return getCrudType() == CrudType.READ;
	}

	public boolean isUpdate() {
		return getCrudType() == CrudType.UPDATE;
	}

	public boolean isDelete() {
		return getCrudType() == CrudType.DELETE;
	}

	public Integer getEmpno() {
		return this.empno;
	}

	public void setEmpno(Integer empno) {
		this.empno = empno;
	}
	public String getEname() {
		return this.ename;
	}

	public void setEname(String ename) {
		this.ename = ename;
	}
	public Integer getMgrid() {
		return this.mgrid;
	}

	public void setMgrid(Integer mgrid) {
		this.mgrid = mgrid;
	}
	public Date getHiredate() {
		return this.hiredate;
	}

	public void setHiredate(Date hiredate) {
		this.hiredate = hiredate;
	}
	public BigDecimal getSal() {
		return this.sal;
	}

	public void setSal(BigDecimal sal) {
		this.sal = sal;
	}
	public Integer getDeptid() {
		return this.deptid;
	}

	public void setDeptid(Integer deptid) {
		this.deptid = deptid;
	}

	public EmpDao getEmpDao() {
		return this.empDao;
	}

	public void setEmpDao(EmpDao empDao) {
		this.empDao = empDao;
	}

	public EmpDxo getEmpDxo() {
		return this.empDxo;
	}

	public void setEmpDxo(EmpDxo empDxo) {
		this.empDxo = empDxo;
	}

	public String getIsCreateStyle() {
		return null;
	}

	public String getIsNotCreateStyle() {
		return null;
	}
	
	public String getIsReadStyle() {
		return null;
	}

	public String getIsNotReadStyle() {
		return null;
	}

	public String getIsUpdateStyle() {
		return null;
	}

	public String getIsNotUpdateStyle() {
		return null;
	}

	public String getIsDeleteStyle() {
		return null;
	}

	public String getIsNotDeleteStyle() {
		return null;
	}

}