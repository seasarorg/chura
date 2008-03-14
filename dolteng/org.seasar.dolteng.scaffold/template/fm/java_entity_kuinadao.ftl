package ${configs.rootpackagename}.${configs.entitypackagename};

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
<#if configs.table_rdb.equalsIgnoreCase(configs.table_capitalize) = false>
import javax.persistence.Table;
</#if>

${getImports()}

@Entity
<#if configs.table_rdb.equalsIgnoreCase(configs.table_capitalize) = false>
@Table(name="${configs.table_rdb}")
</#if>
public class ${configs.table_capitalize} {

<#list mappings as mapping>
<#if mapping.isPrimaryKey() = true>
	@Id
	@GeneratedValue
<#elseif isVersionColumn(mapping) = true>
	@Version
</#if>
<#if mapping.isDate() = true>
	@Temporal(TemporalType.DATE)
</#if>
<#if mapping.sqlColumnName.equalsIgnoreCase(mapping.javaFieldName) = false>
	@Column(name="${mapping.sqlColumnName}")
</#if>
	private ${getJavaClassName(mapping)} ${mapping.javaFieldName};

</#list>
	public ${configs.table_capitalize}() {
	}

<#list mappings as mapping>
	public ${getJavaClassName(mapping)} get${mapping.javaFieldName?cap_first}() {
		return this.${mapping.javaFieldName};
	}

	public void set${mapping.javaFieldName?cap_first}(${getJavaClassName(mapping)} ${mapping.javaFieldName?lower_case}) {
		this.${mapping.javaFieldName} = ${mapping.javaFieldName?lower_case};
	}
</#list>
}