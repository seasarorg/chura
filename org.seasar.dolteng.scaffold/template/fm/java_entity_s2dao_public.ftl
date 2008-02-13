package ${configs.rootpackagename}.${configs.entitypackagename};

<#if isTigerResource() = true>
import org.seasar.dao.annotation.tiger.Bean;
</#if>
${getImports()}

<#if isTigerResource() = true>
@Bean(table="${configs.table_rdb}")
</#if>
public class ${configs.table_capitalize} {

<#if isTigerResource() = false>
	public static final String TABLE = "${configs.table_rdb}";

</#if>
<#list mappings as mapping>
	public ${getJavaClassName(mapping)} ${mapping.javaFieldName};

</#list>
	public ${configs.table_capitalize}() {
	}
}