package ${type.packageFragment.elementName} {
	
	[Bindable]
	[RemoteClass(alias="${type.packageFragment.elementName}.${type.elementName}")]
	public class ${type.elementName} {

<#list type.fields as field>
		public var ${field.elementName}: ${toAsType(field)};
</#list>
	}
}