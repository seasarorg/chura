package ${configs.rootpackagename}.${configs.daopackagename};

<#if 1 &lt; countPkeys()>
import java.util.Map;

</#if>
import org.seasar.uuji.GenericDao;

public interface ${configs.table_capitalize}${configs.daosuffix} extends GenericDao {

<#if 1 &lt; countPkeys()>
	public Map find(${createPkeyMethodArgs()});
</#if>
}