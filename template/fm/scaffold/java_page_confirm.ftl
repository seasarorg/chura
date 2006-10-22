package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table};

<#if isTigerResource() = true>
import org.seasar.teeda.extension.annotation.takeover.TakeOver;
import org.seasar.teeda.extension.annotation.takeover.TakeOverType;
import org.seasar.teeda.extension.annotation.validator.Required;
</#if>
import org.seasar.teeda.extension.util.LabelHelper;

import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.CrudType;

public class ${configs.table_capitalize}ConfirmPage extends Abstract${configs.table_capitalize}Page {
	
	private LabelHelper labelHelper;
	
	public ${configs.table_capitalize}ConfirmPage() {
	}
	
	public String initialize() {
		if(isComeFromList()) {
			get${configs.table_capitalize}Dxo().convert(get${configs.table_capitalize}Dao().find(${createPkeyMethodCallArgs()}) ,this);
		}
		return null;
	}
	
<#if isTigerResource() = true>
	@TakeOver(type = TakeOverType.NEVER)
<#else>
	public static final String doFinish_TAKE_OVER = "type=never";
</#if>
	public String doFinish() {
		switch(getCrudType()) {
			case CrudType.CREATE:
				get${configs.table_capitalize}Dao().insert(get${configs.table_capitalize}Dxo().convert(this));
				break;
			case CrudType.UPDATE:
				get${configs.table_capitalize}Dao().update(get${configs.table_capitalize}Dxo().convert(this));
				break;
			case CrudType.DELETE:
				get${configs.table_capitalize}Dao().delete(get${configs.table_capitalize}Dxo().convert(this));
				break;
			default:
				break;
		}
		return "${configs.table}List";
	}
	
	public boolean isComeFromList() {
		return getCrudType() == CrudType.READ || getCrudType() == CrudType.DELETE;
	}

<#list mappings as mapping>
<#if mapping.isNullable() = false>
<#if isTigerResource() = true>
	@Required
<#else>
	public static final String ${mapping.javaFieldName}_TRequiredValidator = null;
</#if>
	public void set${mapping.javaFieldName?cap_first}(${getJavaClassName(mapping)} ${mapping.javaFieldName?lower_case}) {
		super.set${mapping.javaFieldName?cap_first}(${mapping.javaFieldName?lower_case});
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
}