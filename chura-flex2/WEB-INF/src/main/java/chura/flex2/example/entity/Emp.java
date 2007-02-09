package chura.flex2.example.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Emp {

	private Integer id;

	private Integer empNo;

	public static final String empNo_COLUMN = "EMP_NO";

	private String empName;

	public static final String empName_COLUMN = "EMP_NAME";

	private Integer mgrId;

	public static final String mgrId_COLUMN = "MGR_ID";

	private Date hiredate;

	private BigDecimal sal;

	private Integer deptId;

	public static final String deptId_COLUMN = "DEPT_ID";

	private BigDecimal versionNo;

	public static final String versionNo_COLUMN = "VERSION_NO";

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEmpNo() {
		return empNo;
	}

	public void setEmpNo(Integer empNo) {
		this.empNo = empNo;
	}

	public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	public Integer getMgrId() {
		return mgrId;
	}

	public void setMgrId(Integer mgrId) {
		this.mgrId = mgrId;
	}

	public Date getHiredate() {
		return hiredate;
	}

	public void setHiredate(Date hiredate) {
		this.hiredate = hiredate;
	}

	public BigDecimal getSal() {
		return sal;
	}

	public void setSal(BigDecimal sal) {
		this.sal = sal;
	}

	public Integer getDeptId() {
		return deptId;
	}

	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}

	public BigDecimal getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(BigDecimal versionNo) {
		this.versionNo = versionNo;
	}

}
