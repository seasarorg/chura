package chura.flex2.example.dao;

import chura.flex2.example.entity.Emp;

public interface EmpDao {

	public static final Class BEAN = Emp.class;

	public static final String selectById_ARGS = "ID";

	public Emp[] selectAll();

	public Emp selectById(Integer id);

	public int insert(Emp emp);

	public int update(Emp emp);

	public int delete(Emp emp);

}
