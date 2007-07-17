package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

import java.util.Map;
${getImports()}
<#if isTigerResource() = true>
import org.seasar.teeda.extension.annotation.convert.DateTimeConverter;
import org.seasar.teeda.extension.annotation.takeover.TakeOver;
import org.seasar.teeda.extension.annotation.takeover.TakeOverType;
import org.seasar.teeda.extension.annotation.validator.Required;
</#if>
import org.seasar.teeda.core.exception.AppFacesException;
import org.seasar.teeda.extension.util.LabelHelper;

import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}Confirm${configs.pagesuffix} extends Abstract${configs.table_capitalize}${configs.pagesuffix} {
	
	private LabelHelper labelHelper;
	
	public ${configs.table_capitalize}Confirm${configs.pagesuffix}() {
	}
	
	public Class initialize() {
		if(isComeFromList()) {
			Map data = get${configs.table_capitalize}${configs.daosuffix}().find(${createPkeyMethodCallArgs()});
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

<#if isTigerResource() = true>
	@TakeOver(type = TakeOverType.NEVER)
<#else>
	public static final String doFinish_TAKE_OVER = "type=never";
</#if>
	public Class doFinish() {
		switch(getCrudType()) {
			case CrudType.CREATE:
				get${configs.table_capitalize}${configs.daosuffix}().insert(get${configs.table_capitalize}${configs.dxosuffix}().convert(this));
				break;
			case CrudType.UPDATE:
				get${configs.table_capitalize}${configs.daosuffix}().update(get${configs.table_capitalize}${configs.dxosuffix}().convert(this));
				break;
			case CrudType.DELETE:
				get${configs.table_capitalize}${configs.daosuffix}().delete(get${configs.table_capitalize}${configs.dxosuffix}().convert(this));
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
<#if mapping.isNullable() = false>
<#if isTigerResource() = true>
	@Override
	@Required
<#else>
	public static final String ${mapping.javaFieldName}_TRequiredValidator = null;
</#if>
	public void set${mapping.javaFieldName?cap_first}(${getJavaClassName(mapping)} ${mapping.javaFieldName?lower_case}) {
		super.set${mapping.javaFieldName?cap_first}(${mapping.javaFieldName?lower_case});
	}

</#if>
<#if mapping.isDate() = true>
<#if isTigerResource() = true>
	@Override
	@DateTimeConverter
<#else>
	public static final String ${mapping.javaFieldName}_TDateTimeConverter = null;
</#if>
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

	public String getJumpDeptEditStyle() {
		return isComeFromList() ? "display: none;" : "";
	}
}