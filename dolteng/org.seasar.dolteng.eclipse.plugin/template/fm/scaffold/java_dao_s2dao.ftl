package ${configs.rootpackagename}.${configs.daopackagename};

<#if isTigerResource() = true>
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.S2Dao;
</#if>
import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};

<#if isTigerResource() = true>
@S2Dao(bean=${configs.table_capitalize}.class)
</#if>
public interface ${configs.table_capitalize}${configs.daosuffix} {

<#if isTigerResource() = false>
	public Class BEAN = ${configs.table_capitalize}.class;

</#if>
	public ${configs.table_capitalize}[] selectAll();
	
<#if 0 &lt; countPkeys()>
<#if isTigerResource() = true>
	@Arguments(${createPkeyMethodArgNames()})
<#else>
	public String selectById_ARGS = ${createPkeyMethodArgNames()};
</#if>
</#if>
	public ${configs.table_capitalize} selectById(${createPkeyMethodArgs()});
	
	public int insert(${configs.table_capitalize} ${configs.table});

	public int update(${configs.table_capitalize} ${configs.table});
	
	public int delete(${configs.table_capitalize} ${configs.table});
}