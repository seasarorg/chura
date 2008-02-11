package tutorial.chura.entity;

import java.math.BigDecimal;
import java.util.Date;

import org.seasar.dao.annotation.tiger.Bean;

@Bean(table = "EMP")
public class Emp {

	public Integer id;

	public Integer empNo;

	public String empName;

	public Integer mgrId;

	public Date hiredate;

	public BigDecimal sal;

	public Integer deptId;

	public Integer versionNo;

	public Emp() {
	}
}