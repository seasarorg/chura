package tutorial.chura.dao;

import java.util.List;
import java.util.Map;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;

import tutorial.chura.entity.Dept;

@S2Dao(bean = Dept.class)
public interface DeptDao {

	public Dept[] selectAll();

	@Arguments("ID")
	public Dept selectById(Integer id);

	@Sql("select id as value, dept_name as label from dept")
	public List<Map> selectValueLabel();

	public int insert(Dept dept);

	public int update(Dept dept);

	public int delete(Dept dept);

}
