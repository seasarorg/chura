package tutorial.chura.dao;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.S2Dao;

import tutorial.chura.entity.Emp;

@S2Dao(bean = Emp.class)
public interface EmpDao {

	public Emp[] selectAll();

	@Arguments("ID")
	public Emp selectById(Integer id);

	public int insert(Emp emp);

	public int update(Emp emp);

	public int delete(Emp emp);
}