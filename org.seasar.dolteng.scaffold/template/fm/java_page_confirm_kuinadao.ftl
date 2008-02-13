package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

${getImports()}
import org.seasar.teeda.extension.annotation.convert.DateTimeConverter;
import org.seasar.teeda.extension.annotation.takeover.TakeOver;
import org.seasar.teeda.extension.annotation.takeover.TakeOverType;
import org.seasar.teeda.extension.annotation.validator.Required;
import org.seasar.teeda.core.exception.AppFacesException;
import org.seasar.teeda.extension.util.LabelHelper;

import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};
import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}Confirm${configs.pagesuffix} extends Abstract${configs.table_capitalize}${configs.pagesuffix} {
	
	private LabelHelper labelHelper;
	
	public ${configs.table_capitalize}Confirm${configs.pagesuffix}() {
	}
	
	public Class initialize() {
		if(isComeFromList()) {
			${configs.table_capitalize} data = get${configs.table_capitalize}${configs.servicesuffix}().find(${createPkeyMethodCallArgs(true)});
			if(data == null) {
				throw new AppFacesException("E0000001");
			}
			get${configs.table_capitalize}${configs.dxosuffix}().convert(data ,this);
		}
		return null;
	}
	
	public Class prerender() {
		return null;
	}

	@TakeOver(type = TakeOverType.NEVER)
	public Class doCreate() {
		get${configs.table_capitalize}${configs.servicesuffix}().persist(this);
		return ${configs.table_capitalize}List${configs.pagesuffix}.class;
	}

	@TakeOver(type = TakeOverType.NEVER)
	public Class doUpdate() {
		switch(getCrudType()) {
			case CrudType.UPDATE:
				get${configs.table_capitalize}${configs.servicesuffix}().update(this);
				break;
			case CrudType.DELETE:
				get${configs.table_capitalize}${configs.servicesuffix}().remove(${createPkeyMethodCallArgs(true)});
				break;
			default:
				break;
		}
		return ${configs.table_capitalize}List${configs.pagesuffix}.class;
	}
	
	public boolean isComeFromList() {
		return getCrudType() == CrudType.READ || getCrudType() == CrudType.DELETE;
	}

<#list mappings as mapping>
<#if mapping.isNullable() = false || isVersionColumn(mapping) = true>
	@Override
<#if mapping.isPrimaryKey() = true || isVersionColumn(mapping) = true>
	@Required(target = "doUpdate")
<#else>
	@Required
</#if>
	public void set${mapping.javaFieldName?cap_first}(${getJavaClassName(mapping)} ${mapping.javaFieldName?lower_case}) {
		super.set${mapping.javaFieldName?cap_first}(${mapping.javaFieldName?lower_case});
	}

</#if>
<#if mapping.isDate() = true>
	@Override
	@DateTimeConverter
	public ${getJavaClassName(mapping)} get${mapping.javaFieldName?cap_first}() {
		return super.get${mapping.javaFieldName?cap_first}();
	}

</#if>
</#list>
	public void setLabelHelper(LabelHelper labelHelper) {
		this.labelHelper = labelHelper;
	}
	
	public LabelHelper getLabelHelper() {
		return this.labelHelper;
	}
	
	public String getDoFinishValue() {
		return getLabelHelper().getLabelValue(CrudType.toString(getCrudType()));
	}

	public String getJump${configs.table_capitalize}EditStyle() {
		return isComeFromList() ? "display: none;" : "";
	}

	public String getDoUpdateStyle() {
		return isCreate() ? "display: none;" : "";
	}
}