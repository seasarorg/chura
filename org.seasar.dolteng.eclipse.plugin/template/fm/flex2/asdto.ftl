package ${type.packageFragment.elementName} {
	
	[Bindable]
	[RemoteClass(alias="${type.packageFragment.elementName}.${type.elementName}")]
	public class ${type.elementName} {

<#list type.fields as field>
<#if isOutputField(field) = true>
		public var ${field.elementName}: ${toAsType(field)};
</#if>
</#list>
	}
}